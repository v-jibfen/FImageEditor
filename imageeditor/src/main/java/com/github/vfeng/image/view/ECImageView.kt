package com.github.vfeng.image.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.github.vfeng.image.ECImageMode
import com.github.vfeng.image.anim.ECRecoverAnimator
import com.github.vfeng.image.frame.ECFramePortrait
import com.github.vfeng.image.frame.ECText
import com.github.vfeng.image.frame.IECFrame
import com.github.vfeng.image.model.*
import com.github.vfeng.image.model.ECDoodle.Companion.BASE_DOODLE_WIDTH
import com.github.vfeng.image.model.ECMosaic.Companion.BASE_MOSAIC_WIDTH
import com.github.vfeng.image.utils.LogUtils
import com.github.vfeng.image.view.frame.IECFrameTextView

class ECImageView: FrameLayout, ValueAnimator.AnimatorUpdateListener,
        Animator.AnimatorListener, ECFramePortrait.Callback, ECImage.ImageSizeChangeListener, Runnable {

    companion object {
        const val TAG = "ECImageView"
    }

    private val DEBUG = false
    private val image: ECImage = ECImage()//图片对象
    private val doodle: ECDoodle = ECDoodle()//涂鸦对象
    private val mosaic: ECMosaic = ECMosaic()//马赛克对象
    private val frame: ECFrame = ECFrame()//磁块对象

    private var gestureDetector: GestureDetector?= null//手势辅助类
    private var scaleGestureDetector: ScaleGestureDetector? = null//缩放手势辅助类
    private var mRecoverAnimator: ECRecoverAnimator?= null//图片还原动画

    private var tempPath = ECPath() //临时路径
    private var mHistoryMode: ECImageMode = ECImageMode.NONE //过去的编辑模式，用以回退

    private var pointerCount = 0//手势手指数量值

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    fun init(context: Context?, attrs: AttributeSet?, defStyleAttr: Int){
        setWillNotDraw(false);
        gestureDetector = GestureDetector(context, onGestureListener)
        scaleGestureDetector = ScaleGestureDetector(context, onScaleGestureListener)

        image.init()
        doodle.init()
        mosaic.init()
        frame.init()
        image.setImageSizeChangeListener(this)

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            LogUtils.d(TAG, "onLayout true")
            image.onWindowChanged((right - left) * 1f, (bottom - top) * 1f)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        onDrawImage(canvas)
    }

    private fun onDrawImage(canvas: Canvas?) {
        if (image.bitmap == null) return

        canvas?.save()

        // clip 中心旋转
        val clipFrame: RectF = image.clipFrame
        canvas?.rotate(image.getRotate(), clipFrame.centerX(), clipFrame.centerY())

        //第一步，先画原始image
        image.onDrawOriginalBitmap(canvas)

        //根据mode，判断当前需要画的
        onDrawMosaic(canvas)
        onDrawDoodle(canvas)

        if (image.isFreezing()) {
            //画磁力片
            frame.onDrawFrames(canvas)
        }

        image.onDrawShade(canvas)

        canvas?.restore()

        if (!image.isFreezing()) {
            //画磁力片
            image.onDrawFrameClip(canvas)
            frame.onDrawFrames(canvas)
        }

        // 裁剪
        if (image.mode == ECImageMode.CLIP) {
            canvas?.save()
            canvas?.translate(scrollX.toFloat(), scrollY.toFloat())
            image.onDrawClip(canvas, scrollX.toFloat(), scrollY.toFloat())
            canvas?.restore()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (ev!!.actionMasked == MotionEvent.ACTION_DOWN) {
            onInterceptTouch(ev) || super.onInterceptTouchEvent(ev)
        } else super.onInterceptTouchEvent(ev)
    }

    fun onInterceptTouch(event: MotionEvent?): Boolean {
        if (isRecovering()) {
            stopRecover()
            return true
        } else if (image.mode == ECImageMode.CLIP) {
            return true
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.actionMasked) {
            MotionEvent.ACTION_DOWN -> removeCallbacks(this)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> postDelayed(this, 1200)
        }
        return handleOnTouchEvent(event)
    }

    private fun handleOnTouchEvent(event: MotionEvent?) : Boolean{
        if (isRecovering()) {
            //正在执行动画
            return false
        }
        var handle = scaleGestureDetector?.onTouchEvent(event)
        pointerCount = event!!.pointerCount

        val mode = image.mode

        if (mode == ECImageMode.NONE || mode === ECImageMode.CLIP) {
            handle = onTouchNONE(event)
        } else if (mode == ECImageMode.DOODLE || mode == ECImageMode.MOSAIC) {
            handle = onTouchPath(event)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                image.onTouchDown(event.x, event.y)
                frame.stickAll()
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                image.onTouchUp(scrollX.toFloat(), scrollY.toFloat())
                handleRecover()
            }
        }

        return handle!!
    }

    private fun onTouchNONE(event: MotionEvent?): Boolean {
        return gestureDetector?.onTouchEvent(event)!!
    }

    private fun onTouchPath(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                return onPathBegin(event)
            }
            MotionEvent.ACTION_MOVE -> return onPathMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> return tempPath.isIdentity(event.getPointerId(0)) && event.pointerCount == 1  && onPathDone()
        }
        return false
    }

    //进行涂鸦绘制
    private fun onDrawDoodle(canvas: Canvas?) {
        canvas?.save()
        val scale: Float = image.getScale()
        canvas?.translate(image.bitmapFrame.left, image.bitmapFrame.top)
        canvas?.scale(scale, scale)
        doodle.drawPaths(canvas)
        canvas?.restore()
        if (image.mode == ECImageMode.DOODLE && tempPath != null && !tempPath.isEmpty()) {
            doodle.getDoodlePaint().color = tempPath.color
            doodle.getDoodlePaint().strokeWidth = BASE_DOODLE_WIDTH * image.getScale()

            canvas?.save()
            val frame: RectF = image.clipFrame
            canvas?.rotate(-image.getRotate(), frame.centerX(), frame.centerY())
            canvas?.translate(scrollX.toFloat(), scrollY.toFloat())
            tempPath.path?.let {
                canvas?.drawPath(it, doodle.getDoodlePaint())
            }

            canvas?.restore()
        }
    }

    //进行马赛克绘制
    private fun onDrawMosaic(canvas: Canvas?) {
        if (!mosaic.isMosaicEmpty() || (image.mode == ECImageMode.MOSAIC && !tempPath.isEmpty())) {

            val count = canvas?.saveLayer(image.bitmapFrame, null, Canvas.ALL_SAVE_FLAG)
//            val scale: Float = 1f / image.getScale()
//            mosaic.getPathPaint().strokeWidth = BASE_MOSAIC_WIDTH * scale
            if (!mosaic.isMosaicEmpty()) {
                canvas?.save()
                val scale: Float = image.getScale()
                canvas?.translate(image.bitmapFrame.left, image.bitmapFrame.top)
                canvas?.scale(scale, scale)
                mosaic.drawPaths(canvas)
                canvas?.restore()
            }

            if (image.mode == ECImageMode.MOSAIC && !tempPath.isEmpty()) {
                LogUtils.d(TAG, "onDrawMosaic")
                mosaic.getPathPaint().strokeWidth = BASE_MOSAIC_WIDTH
                canvas?.save()
                val frame: RectF = image.clipFrame
                canvas?.rotate(-image.getRotate(), frame.centerX(), frame.centerY())
                canvas?.translate(scrollX.toFloat(), scrollY.toFloat())
                tempPath.path?.let {
                    canvas?.drawPath(it, mosaic.getPathPaint())
                }

                canvas?.restore()
            }
            mosaic.mosaicImage?.let {
                canvas?.drawBitmap(it, null, image.bitmapFrame, mosaic.getBitmapPaint())
            }

            count?.let { canvas.restoreToCount(it) }
        }
    }

    private fun onPathBegin(event: MotionEvent): Boolean{
        LogUtils.d(TAG, "onPathBegin")
        tempPath.moveTo(event.getX(), event.getY())
        tempPath.setIdentity(event.getPointerId(0))
        return true
    }

    private fun onPathMove(event: MotionEvent): Boolean{
        if (tempPath.isIdentity(event.getPointerId(0))
                && event.pointerCount == 1) {
            LogUtils.d(TAG, "onPathMove")
            tempPath.lineTo(event.x, event.y)
            invalidate()
            return true
        }
        return false
    }

    private fun onPathDone(): Boolean {
        LogUtils.d(TAG, "onPathDone")

        if (tempPath.isEmpty()) {
            return false
        }

        val mode = image.mode

        when(mode) {
            ECImageMode.DOODLE -> {
                tempPath.toPath()?.let {
                    val path = image.getTransformPath(it, getScrollX().toFloat(), getScrollY().toFloat())
                    doodle.addPath(ECPath().apply {
                        this.path = path
                        this.color = tempPath.color
                    })
                }
            }

            ECImageMode.MOSAIC -> {
                tempPath.toPath()?.let {
                    val path = image.getTransformPath(it, getScrollX().toFloat(), getScrollY().toFloat())
                    val ecPath = ECMosaicPath()
                    val scale: Float = 1f / image.getScale()
                    ecPath.width = ecPath.width * scale
                    ecPath.path = path
                    mosaic.addPath(ecPath)
                }
            }
        }

        tempPath.reset()
        invalidate()

        return true
    }

    fun onSteady(): Boolean {
        if (DEBUG) {
            Log.d(TAG, "onSteady: isRecovering=" + isRecovering())
        }
        if (!isRecovering()) {
            image.onSteady(scrollX.toFloat(), scrollY.toFloat())
            handleRecover()
            return true
        }
        return false
    }

    private val onGestureListener = object : GestureDetector.OnGestureListener {
        override fun onShowPress(e: MotionEvent?) {
//            LogUtils.d(TAG, "onShowPress")
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
//            LogUtils.d(TAG, "onSingleTapUp")
            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
//            LogUtils.d(TAG, "onDown")

            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
//            LogUtils.d(TAG, "onFling")
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
//            LogUtils.d(TAG, "onScroll")
            return onScroll(distanceX, distanceY);
        }

        override fun onLongPress(e: MotionEvent?) {
//            LogUtils.d(TAG, "onLongPress")

        }
    }

    private val onScaleGestureListener = object : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
//            LogUtils.d(TAG, "onScaleBegin")
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
//            LogUtils.d(TAG, "onScaleEnd")
        }

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
//            LogUtils.d(TAG, "onScale")
            if (pointerCount > 1) {
                image.onScale(detector!!.scaleFactor,
                        scrollX + detector.focusX,
                        scrollY + detector.focusY)
                invalidate()
                return true
            }

            return false
        }
    }

    override fun onImageScale(factor: Float, focusX: Float, focusY: Float) {
        //处理image变化，对其他影响
        frame.onScale(factor, focusX, focusY)
    }

    /**
     * 是否正在回到原始位置
     */
    private fun isRecovering(): Boolean {
        return (mRecoverAnimator != null
                && mRecoverAnimator!!.isRunning)
    }

    /**
     * 执行回到原始位置
     */
    private fun handleRecover() {
        invalidate()
        stopRecover()
        startRecover(image.getStartRecover(scrollX.toFloat(), scrollY.toFloat()),
                image.getEndRecover(scrollX.toFloat(), scrollY.toFloat()))
    }

    /**
     * 开启回到原始位置动画
     */
    private fun startRecover(sRecover: ECRecover?, eRecover: ECRecover?) {
        if (mRecoverAnimator == null) {
            mRecoverAnimator = ECRecoverAnimator()
            mRecoverAnimator!!.addUpdateListener(this)
            mRecoverAnimator!!.addListener(this)
        }
        mRecoverAnimator!!.setRecoveringValues(sRecover, eRecover)
        mRecoverAnimator!!.start()
    }

    /**
     * 停止回到原始位置动画
     */
    private fun stopRecover() {
        if (mRecoverAnimator != null) {
            mRecoverAnimator!!.cancel()
        }
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }

    override fun onAnimationEnd(animation: Animator?) {
        if (DEBUG) {
            Log.d(TAG, "onAnimationEnd")
        }
        if (image.onRecoveringEnd(scrollX.toFloat(), scrollY.toFloat(), mRecoverAnimator!!.isRotate)) {
            toHandlerRecovering(image.clip(scrollX.toFloat(), scrollY.toFloat())!!)
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
        if (DEBUG) {
            Log.d(TAG, "onAnimationCancel")
        }
        image.onRecoveringCancel(mRecoverAnimator!!.isRotate)
    }

    override fun onAnimationStart(animation: Animator?) {
        if (DEBUG) {
            Log.d(TAG, "onAnimationStart")
        }
        image.onRecoveringStart(mRecoverAnimator!!.isRotate)
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
//        LogUtils.d(TAG, "ValueAnimator " + animation!!.animatedFraction)
        image.onRecovering(animation!!.animatedFraction)
        toHandlerRecovering(animation?.animatedValue as ECRecover)
    }

    //慢慢还原，过程
    private fun toHandlerRecovering(recover: ECRecover) {
        image.setScale(recover.scale)
        image.setRotate(recover.rotate)
        if (!onScrollTo(Math.round(recover.x), Math.round(recover.y))) {
            invalidate()
        }
    }

    private fun onScroll(dx: Float, dy: Float): Boolean {
        val recovering = image.onScroll(scrollX.toFloat(), scrollY.toFloat(), -dx, -dy)
        if (recovering != null) {
            toHandlerRecovering(recovering)
            return true
        }
        return onScrollTo(scrollX + Math.round(dx), scrollY + Math.round(dy))
    }

    private fun onScrollTo(x: Int, y: Int): Boolean {
        if (scrollX != x || scrollY != y) {
            scrollTo(x, y)
            return true
        }
        return false
    }

    private fun <V> addFrameView(frameView: V?, params: LayoutParams?) where V : View?, V : IECFrame? {
        if (frameView != null) {
            addView(frameView, params)
            frameView.registerCallback(this)
            frame.addFrame(frameView)
        }
    }

    override fun <V : View?> onRemove(frameView: V): Boolean where V : IECFrame? {
        frame.removeFrame(frameView)
        frameView?.let {
            frameView.unregisterCallback(this)
            val parent = frameView.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(frameView)
            }
        }
        return true
    }

    override fun <V : View?> onDismiss(frameView: V) where V : IECFrame? {
        frame.onDismiss(frameView)
        invalidate()
    }

    override fun <V : View?> onShowing(frameView: V) where V : IECFrame? {
        frame.onShowing(frameView)
        invalidate()
    }

    override fun run() {
        //还原图片位置
        if (!onSteady()) {
            postDelayed(this, 500)
        }
    }

    //--------------------------------------------------------------------------------------------------------//
    //公共方法

    /**
     * @param 图片路径uri
     * 设置图片
     */
    fun setImageData(uri: Uri) {
        post {
            this.image.getBitmap(uri)
            this.image.initLocation()
            image.bitmap?.let { this.mosaic.makeMosaicBitmap(it) }
            invalidate()
        }
    }

    fun setImageBitmap(bm: Bitmap) {
        post {
            this.image.getBitmap(bm)
            this.image.initLocation()
            image.bitmap?.let { this.mosaic.makeMosaicBitmap(it) }
            invalidate()
        }
    }

    /**
     * 撤销涂鸦
     */
    fun cancelDoodlePath() {
        doodle.removeRecentPath()
        invalidate()
    }

    /**
     * 撤销涂鸦
     */
    fun cancelMosaicPath() {
        mosaic.removeRecentPath()
        invalidate()
    }

    /**
     * 添加文字磁块
     */
    fun addTextFrame(text: ECText?) {
        val textView = IECFrameTextView(context)
        val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        textView.apply {
            this.text = text
            x = scrollX.toFloat()
            y = scrollY.toFloat()
        }
        addFrameView(textView, layoutParams)
    }

    /**
     * 获取当前编辑模式
     */
    fun getMode(): ECImageMode {
        return image.mode
    }

    /**
     * @param mode
     * 切换图片编辑模式
     */
    fun setMode(mode: ECImageMode?) {
        // 保存现在的编辑模式
        mHistoryMode = image?.mode

        // 设置新的编辑模式
        mode?.let {
            if (image?.mode == mode) return
            image?.mode = mode

            //切换磁块模式
            frame?.stickAll()

            if (image?.mode === ECImageMode.CLIP) {

                image.openClipMode()
            } else {
                if (mode === ECImageMode.MOSAIC) {
                    image.bitmap?.let { it1 -> mosaic.makeMosaicBitmap(it1) }
                }
                image.setClipping(false)
            }
        }

        //还原
        handleRecover()
    }

    /**
     * @param color
     * 这是临时路径颜色
     */
    fun setTepPathColor(color: Int) {
        tempPath.color = color
    }

    /**
     * 旋转90°
     */
    fun doClipRotate() {
        if (!isRecovering()) {
            image.rotate(-90)
            handleRecover()
        }
    }

    /**
     * 还原裁剪
     */
    fun resetClip() {
        image.resetClip()
        handleRecover()
    }

    /**
     * 进行裁剪
     */
    fun doneClip() {
        image.clip(scrollX.toFloat(), scrollY.toFloat())
        setMode(mHistoryMode)
        handleRecover()
    }

    /**
     * 取消裁剪
     */
    fun cancelClip() {
        image.toBackupClip()
        setMode(mHistoryMode)
    }

    /**
     * 转成bitmap，保存
     */
    fun toBitmap(): Bitmap? {
        frame.stickAll()
        val scale: Float = 1f / image.getScale()
        val frame = RectF(image.clipFrame)

        // 旋转基画布
        val m = Matrix()
        m.setRotate(image.getRotate(), frame.centerX(), frame.centerY())
        m.mapRect(frame)

        // 缩放基画布
        m.setScale(scale, scale, frame.left, frame.top)
        m.mapRect(frame)
        val bitmap = Bitmap.createBitmap(Math.round(frame.width()),
                Math.round(frame.height()), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 平移到基画布原点&缩放到原尺寸
        canvas.translate(-frame.left, -frame.top)
        canvas.scale(scale, scale, frame.left, frame.top)
        onDrawImage(canvas)
        return bitmap
    }

}