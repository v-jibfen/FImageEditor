package com.github.vfeng.image.frame

import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View

class ECFrameMoveHelper(private val mView: View) {
    private var mX = 0f
    private var mY = 0f
    fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mX = event.x
                mY = event.y
                M.reset()
                M.setRotate(v.rotation)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dxy = floatArrayOf(event.x - mX, event.y - mY)
                M.mapPoints(dxy)
                v.translationX = mView.translationX + dxy[0]
                v.translationY = mView.translationY + dxy[1]
                return true
            }
        }
        return false
    }

    companion object {
        private const val TAG = "ECFrameMoveHelper"
        private val M = Matrix()
    }

}