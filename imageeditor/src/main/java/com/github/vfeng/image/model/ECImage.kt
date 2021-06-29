package com.github.vfeng.image.model

import android.graphics.*
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.github.vfeng.image.ECImageMode
import com.github.vfeng.image.utils.ImageUtils
import com.github.vfeng.image.utils.LogUtils
import com.github.vfeng.image.view.clip.IMGClip
import com.github.vfeng.image.view.clip.IMGClipWindow
import java.io.File

/**
 * Created by vfeng on 2020/8/14.
 */
class ECImage {

    companion object {
        const val TAG = "ECImage"
    }

    var bitmap: Bitmap?= null
    var mode = ECImageMode.NONE

    private val MAX_WIDTH = 1024

    private val MAX_HEIGHT = 1024

    private val MIN_SIZE = 500

    private val MAX_SIZE = 10000
    private val COLOR_SHADE = -0x34000000

    private val M = Matrix()
    /**
     * 完整图片边框
     */
    val bitmapFrame = RectF()

    /**
     * 裁剪图片边框（显示的图片区域）
     */
    val clipFrame = RectF()

    val tempClipFrame = RectF()

    /**
     * 可视区域，无Scroll 偏移区域
     */
    val windowFrame = RectF()

    /**
     * 旋转角度
     */
    var cRotate = 0f

    /**
     * 原始角度
     */
    var targetRotate = 0f

    /**
     * 是否初始位置
     */
    private var isInitialRecovering = false

    /**
     * 裁剪窗口
     */
    private val clipWin: IMGClipWindow = IMGClipWindow()

    private var shadePaint: Paint? = null

    private var isSteady = true
    /**
     * 裁剪模式前状态备份
     */
    private val backupClipFrame = RectF()

    private var backupClipRotate = 0f

    private var isRequestToBaseFitting = false

    private var isAnimCanceled = false

    private val shadePath = Path()

    private var isFreezing = mode == ECImageMode.CLIP

    /**
     * 裁剪模式时当前触摸锚点
     */
    private var mAnchor: IMGClip.Anchor? = null

    private var onSizeChangeListener: ImageSizeChangeListener? = null

    fun getBitmap(uri: Uri) {
        if (uri == null) return
        val path = uri.path
        if (TextUtils.isEmpty(path)) return

        val file = File(path)
        if (!file.exists()) return

        var options = BitmapFactory.Options()
        options.inSampleSize = 1
        options.inJustDecodeBounds = true

        BitmapFactory.decodeFile(path, options)

        if (options.outWidth > MAX_WIDTH) {
            options.inSampleSize = ImageUtils.inSampleSize(Math.round(1f * options.outWidth / MAX_WIDTH))
        }

        if (options.outHeight > MAX_HEIGHT) {
            options.inSampleSize = Math.max(options.inSampleSize,
                    ImageUtils.inSampleSize(Math.round(1f * options.outHeight / MAX_HEIGHT)))
        }

        options.inJustDecodeBounds = false

        bitmap =   BitmapFactory.decodeFile(path, options)

        if (bitmap == null) return
    }

    fun getBitmap(bm: Bitmap) {
        bitmap = bm

        if (bitmap == null) return
    }

    fun init() {
        bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        shadePath.setFillType(Path.FillType.WINDING)
    }

    fun initLocation() {
        isInitialRecovering = false
        onWindowChanged(windowFrame.width(), windowFrame.height())
        if (mode == ECImageMode.CLIP) {
            clipWin.reset(clipFrame, targetRotate)
        }
    }

    fun onWindowChanged(width: Float, height: Float) {
        if (width == 0f || height == 0f) {
            return
        }

        windowFrame.set(0f, 0f, width, height)

        if (!isInitialRecovering) {
            onInitialRecovering(width, height)
        } else {

            // Pivot to fit window.
            M.setTranslate(windowFrame.centerX() - clipFrame.centerX(), windowFrame.centerY() - clipFrame.centerY())
            M.mapRect(bitmapFrame)
            M.mapRect(clipFrame)
        }

        clipWin.setClipWinSize(width, height)
    }

    //初始化原位数据
    private fun onInitialRecovering(width: Float, height: Float) {
        bitmap?.let {
            bitmapFrame.set(0f, 0f, it.getWidth() * 1f, it.getHeight()  * 1f)
            clipFrame.set(bitmapFrame)
            clipWin.setClipWinSize(width, height)
            if (clipFrame.isEmpty()) {
                return
            }
            toBaseRecovering()
            isInitialRecovering = true
            onInitialRecoveringDone()
        }
    }

    private fun toBaseRecovering() {
        if (clipFrame.isEmpty()) {
            return
        }
        val scale: Float = Math.min(
                windowFrame.width() / clipFrame.width(),
                windowFrame.height() / clipFrame.height()
        )

        // Scale to fit window.
        M.setScale(scale, scale, clipFrame.centerX(), clipFrame.centerY())
        M.postTranslate(windowFrame.centerX() - clipFrame.centerX(), windowFrame.centerY() - clipFrame.centerY())
        M.mapRect(bitmapFrame)
        M.mapRect(clipFrame)
    }

    private fun onInitialRecoveringDone() {
        if (mode == ECImageMode.CLIP) {
            clipWin.reset(clipFrame, targetRotate)
        }
    }

    //画原始图片
    fun onDrawOriginalBitmap(canvas: Canvas?) {
        // 裁剪区域
        if (clipWin.isClipping) {
            canvas?.clipRect(bitmapFrame)
        } else {
            canvas?.clipRect(clipFrame)
        }
        bitmap?.let {
            canvas?.drawBitmap(it, null, bitmapFrame, null)
        }
    }

    fun onDrawFrameClip(canvas: Canvas?) {
        M.setRotate(getRotate(), clipFrame.centerX(), clipFrame.centerY())
        M.mapRect(tempClipFrame, if (isClipping()) bitmapFrame else clipFrame)
        canvas?.clipRect(tempClipFrame)
    }

    fun getRotate(): Float {
        return cRotate
    }

    fun setRotate(rotate: Float) {
        cRotate = rotate
    }

    fun getScale(): Float {
        return 1f * bitmapFrame.width() / bitmap?.width!!
    }

    fun setScale(scale: Float) {
        setScale(scale, clipFrame.centerX(), clipFrame.centerY())
    }

    fun setScale(scale: Float, focusX: Float, focusY: Float) {
        onScale(scale / getScale(), focusX, focusY)
    }

    //缩放
    fun onScale(factor: Float, focusX: Float, focusY: Float) {
        LogUtils.d(TAG, "factor : " + factor + " focusX : " + focusX + " focusY : " + focusY)
        var factor = factor
        if (factor == 1f) return
        if (Math.max(clipFrame.width(), clipFrame.height()) >= MAX_SIZE
                || Math.min(clipFrame.width(), clipFrame.height()) <= MIN_SIZE) {
            factor += (1 - factor) / 2
        }
        M.setScale(factor, factor, focusX, focusY)
        M.mapRect(bitmapFrame)
        M.mapRect(clipFrame)

        // 修正clip 窗口
        if (!bitmapFrame.contains(clipFrame)) {

        }

    }

    fun onRecovering(fraction: Float) {
        clipWin.recovering(fraction)
    }

    //获取开始回到原位的开始位置
    fun getStartRecover(scrollX: Float, scrollY: Float): ECRecover? {
        val  recover = ECRecover(scrollX, scrollY, getScale(), getRotate())
        LogUtils.d(TAG, "StartRecover " + recover.toString())
        return recover
    }

    fun getEndRecover(scrollX: Float, scrollY: Float): ECRecover? {
        val recovering = ECRecover(scrollX, scrollY, getScale(), targetRotate)
        if (mode == ECImageMode.CLIP) {
            val frame = RectF(clipWin.getTargetFrame())
            frame.offset(scrollX, scrollY)
            if (clipWin.isResetting()) {
                val cFrame = RectF()
                M.setRotate(targetRotate, clipFrame.centerX(), clipFrame.centerY())
                M.mapRect(cFrame, clipFrame)
                recovering.rConcat(ImageUtils.fill(frame, cFrame)!!)
            } else {
                val cFrame = RectF()
                if (clipWin.isRecovering()) {
                    M.setRotate(targetRotate - getRotate(), clipFrame.centerX(), clipFrame.centerY())
                    M.mapRect(cFrame, clipWin.getOffsetFrame(scrollX, scrollY))
                    recovering.rConcat(ImageUtils.fitRecovering(frame, cFrame, clipFrame.centerX(), clipFrame.centerY())!!)
                } else {
                    M.setRotate(targetRotate, clipFrame.centerX(), clipFrame.centerY())
                    M.mapRect(cFrame, bitmapFrame)
                    recovering.rConcat(ImageUtils.fillRecovering(frame, cFrame, clipFrame.centerX(), clipFrame.centerY())!!)
                }
            }
        } else {
            val cFrame = RectF()
            M.setRotate(targetRotate, clipFrame.centerX(), clipFrame.centerY())
            M.mapRect(cFrame, clipFrame)
            val win = RectF(windowFrame)
            win.offset(scrollX, scrollY)
            recovering.rConcat(ImageUtils.fitRecovering(win, cFrame, isRequestToBaseFitting)!!)
            isRequestToBaseFitting = false
        }
        LogUtils.d(TAG, "getEndRecover " + recovering.toString())
        return recovering
    }

    fun initShadePaint() {
        if (shadePaint == null) {
            shadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                setColor(COLOR_SHADE)
                setStyle(Paint.Style.FILL)
            }
        }
    }

    fun openClipMode() {
        setFreezing(true)

        // 初始化Shade 画刷
        initShadePaint()
        // 备份裁剪前Clip 区域
        backupClipRotate = getRotate()
        backupClipFrame.set(clipFrame)

        val scale: Float = 1 / getScale()
        M.setTranslate(-bitmapFrame.left, -bitmapFrame.top)
        M.postScale(scale, scale)
        M.mapRect(backupClipFrame)

        // 重置裁剪区域
        clipWin.reset(clipFrame, targetRotate)
    }

    fun onDrawClip(canvas: Canvas?, scrollX: Float, scrollY: Float) {
        if (mode == ECImageMode.CLIP) {
            clipWin.onDraw(canvas)
        }
    }

    fun onRecoveringStart(isRotate: Boolean) {
        isAnimCanceled = false
    }

    fun onRecoveringEnd(scrollX: Float, scrollY: Float, isRotate: Boolean): Boolean {
        if (mode == ECImageMode.CLIP) {
            // 开启裁剪模式
            val clip = !isAnimCanceled
            clipWin.isRecovering = false
            clipWin.isClipping = true
            clipWin.isResetting = false
            return clip
        } else {
            if (isFreezing && !isAnimCanceled) {
                setFreezing(false)
            }
        }
        return false
    }

    fun onRecoveringCancel(isRotate: Boolean) {
        isAnimCanceled = true
        Log.d(TAG, "Recovering cancel")
    }

    fun isFreezing(): Boolean {
        return isFreezing
    }

    private fun setFreezing(freezing: Boolean) {
        if (freezing != isFreezing) {
            isFreezing = freezing
        }
    }

    fun isClipping(): Boolean {
        return clipWin.isClipping
    }

    fun setClipping(clipping: Boolean) {
        clipWin.isClipping = clipping
    }

    /**
     * 裁剪区域旋转回原始角度后形成新的裁剪区域，旋转中心发生变化，
     * 因此需要将视图窗口平移到新的旋转中心位置。
     */
    fun clip(scrollX: Float, scrollY: Float): ECRecover? {
        val frame = clipWin.getOffsetFrame(scrollX, scrollY)

        M.setRotate(-getRotate(), clipFrame.centerX(), clipFrame.centerY())
        M.mapRect(clipFrame, frame)

        return ECRecover(
                scrollX + (clipFrame.centerX() - frame.centerX()),
                scrollY + (clipFrame.centerY() - frame.centerY()),
                getScale(), getRotate()
        )
    }

    /**
     * 在当前基础上旋转
     */
    fun rotate(rotate: Int) {
        targetRotate = Math.round((getRotate() + rotate) / 90f) * 90.toFloat()
        clipWin.reset(clipFrame, targetRotate)
    }

    fun resetClip() {
        targetRotate = getRotate() - getRotate() % 360
        clipFrame.set(bitmapFrame)
        clipWin.reset(clipFrame, targetRotate)
    }


    fun toBackupClip() {
        M.setScale(getScale(), getScale())
        M.postTranslate(bitmapFrame.left, bitmapFrame.top)
        M.mapRect(clipFrame, backupClipFrame)
        targetRotate = backupClipRotate
        isRequestToBaseFitting = true
    }

    fun onScroll(scrollX: Float, scrollY: Float, dx: Float, dy: Float): ECRecover? {
        if (mode == ECImageMode.CLIP) {
            clipWin.isShowShade = false
            if (mAnchor != null) {
                clipWin.onScroll(mAnchor, dx, dy)
                val cFrame = RectF()
                M.setRotate(getRotate(), clipFrame.centerX(), clipFrame.centerY())
                M.mapRect(cFrame, bitmapFrame)
                val frame = clipWin.getOffsetFrame(scrollX, scrollY)
                val recovering = ECRecover(scrollX, scrollY, getScale(), targetRotate)
                recovering.rConcat(ImageUtils.fillRecovering(frame, cFrame, clipFrame.centerX(), clipFrame.centerY())!!)
                return recovering
            }
        }
        return null
    }

    fun onTouchDown(x: Float, y: Float) {
        isSteady = false

        if (mode == ECImageMode.CLIP) {
            mAnchor = clipWin.getAnchor(x, y)
        }
    }

    fun onTouchUp(scrollX: Float, scrollY: Float) {
        if (mAnchor != null) {
            mAnchor = null
        }
    }

    fun onSteady(scrollX: Float, scrollY: Float) {
        isSteady = true
        onClipRecovering()
        clipWin.isShowShade = true
    }

    fun onClipRecovering(): Boolean {
        return clipWin.recovering()
    }

    fun onDrawShade(canvas: Canvas?) {
        if (mode == ECImageMode.CLIP && isSteady) {
            LogUtils.d(TAG, "onDrawShade")
            shadePath.reset()
            shadePath.addRect(bitmapFrame.left - 2, bitmapFrame.top - 2, bitmapFrame.right + 2, bitmapFrame.bottom + 2, Path.Direction.CW)
            shadePath.addRect(clipFrame, Path.Direction.CCW)
            canvas?.drawPath(shadePath, shadePaint!!)
        }
    }

    fun getTransformPath(path: Path, scrollX: Float, scrollY: Float): Path {
        val scale: Float = 1f / getScale()
        M.setTranslate(scrollX, scrollY)
        M.postRotate(-getRotate(), clipFrame.centerX(), clipFrame.centerY())
        M.postTranslate(-bitmapFrame.left, -bitmapFrame.top)
        M.postScale(scale, scale)
        path.transform(M)

        return path
    }

    fun setImageSizeChangeListener(listener: ImageSizeChangeListener) {
        this.onSizeChangeListener = listener
    }

    interface ImageSizeChangeListener {
        fun onImageScale(factor: Float, focusX: Float, focusY: Float)
    }
}