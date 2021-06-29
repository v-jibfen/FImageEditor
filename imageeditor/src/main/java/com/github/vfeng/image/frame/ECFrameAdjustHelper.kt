package com.github.vfeng.image.frame

import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.github.vfeng.image.view.frame.ECFrameView

class ECFrameAdjustHelper(private val mContainer: ECFrameView, private val mView: View) : View.OnTouchListener {
    private var mCenterX = 0f
    private var mCenterY = 0f
    private var mRadius = 0.0
    private var mDegrees = 0.0
    private val M = Matrix()
    override fun onTouch(v: View, event: MotionEvent): Boolean {

        var pointX = 0f
        var pointY = 0f
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                run {
                    mCenterY = 0f
                    mCenterX = mCenterY
                }
                pointX = mView.x + x - mContainer.pivotX
                pointY = mView.y + y - mContainer.pivotY
                Log.d(TAG, String.format("X=%f,Y=%f", pointX, pointY))
                mRadius = toLength(0f, 0f, pointX, pointY)
                mDegrees = toDegrees(pointY, pointX)
                M.setTranslate(pointX - x, pointY - y)
                Log.d(TAG, String.format("degrees=%f", toDegrees(pointY, pointX)))
                M.postRotate((-toDegrees(pointY, pointX)).toFloat(), mCenterX, mCenterY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val xy = floatArrayOf(event.x, event.y)
                pointX = mView.x + xy[0] - mContainer.pivotX
                pointY = mView.y + xy[1] - mContainer.pivotY
                Log.d(TAG, String.format("X=%f,Y=%f", pointX, pointY))
                val radius = toLength(0f, 0f, pointX, pointY)
                val degrees = toDegrees(pointY, pointX)
                val scale = (radius / mRadius).toFloat()
                mContainer.addScale(scale)
                Log.d(TAG, "    D   = " + (degrees - mDegrees))
                mContainer.rotation = (mContainer.rotation + degrees - mDegrees).toFloat()
                mRadius = radius
                return true
            }
        }
        return false
    }

    companion object {
        private const val TAG = "ECFrameAdjustHelper"
        private fun toDegrees(v: Float, v1: Float): Double {
            return Math.toDegrees(Math.atan2(v.toDouble(), v1.toDouble()))
        }

        private fun toLength(x1: Float, y1: Float, x2: Float, y2: Float): Double {
            return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2).toDouble())
        }
    }

    init {
        mView.setOnTouchListener(this)
    }
}