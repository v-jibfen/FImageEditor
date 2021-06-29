package com.github.vfeng.image.view.frame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.github.vfeng.image.frame.ECText
import com.github.vfeng.image.utils.SizeUtils
import com.github.vfeng.image.utils.SizeUtils.dp2px

class ECTextView: View {
    private val paint = Paint()

    private var text: ECText?= null

    private var rectF: RectF = RectF()

    private val PADDING = dp2px(26f)

    constructor(context: Context?) : this(context, null, 0)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.style = Paint.Style.FILL
        paint.textSize = SizeUtils.sp2px(14f).toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        text?.let {
            val rect = Rect()
            paint.getTextBounds(it.text, 0, it.text.length, rect)
            val textW = rect.width()
            val textH = rect.height()
            val width = textW + PADDING * 2
            val height = textH + PADDING * 2

            setMeasuredDimension(width, height)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        rectF = RectF(0f, 0f, (right - left) * 1f, (bottom - top) * 1f)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        text?.let {
            paint.color = it.color
            val fontMetrics: Paint.FontMetrics = paint.getFontMetrics()
            val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
            val baseline: Float = rectF.centerY() + distance
            canvas!!.drawText(it.text, rectF.centerX(), baseline, paint)
        }
    }

    fun setText(text: ECText) {
        this.text = text
    }
}