package com.github.vfeng.image.frame

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.View

class ECFrameHelper<FrameView>(private val mView: FrameView) : ECFramePortrait, ECFramePortrait.Callback where FrameView : View?, FrameView : IECFrame? {
    private var mFrame: RectF? = null
    private var mCallback: ECFramePortrait.Callback? = null
    private var isShowing = false
    override fun show(): Boolean {
        if (!isShowing()) {
            isShowing = true
            onShowing(mView)
            return true
        }
        return false
    }

    override fun remove(): Boolean {
        return onRemove(mView)
    }

    override fun dismiss(): Boolean {
        if (isShowing()) {
            isShowing = false
            onDismiss(mView)
            return true
        }
        return false
    }

    override fun isShowing(): Boolean {
        return isShowing
    }

    override fun getFrame(): RectF {
        if (mFrame == null) {
            mFrame = RectF(0f, 0f, mView!!.getWidth().toFloat(), mView.getHeight().toFloat())
            val pivotX = mView.getX() + mView.getPivotX()
            val pivotY = mView.getY() + mView.getPivotY()
            val matrix = Matrix()
            matrix.setTranslate(mView.getX(), mView.getY())
            matrix.postScale(mView.getScaleX(), mView.getScaleY(), pivotX, pivotY)
            matrix.mapRect(mFrame)
        }
        return mFrame!!
    }

    override fun onFrame(canvas: Canvas) {
        // empty
    }

    override fun registerCallback(callback: ECFramePortrait.Callback) {
        mCallback = callback
    }

    override fun unregisterCallback(callback: ECFramePortrait.Callback) {
        mCallback = null
    }

    override fun <V> onRemove(frameView: V): Boolean where V : View?, V : IECFrame? {
        return mCallback != null && mCallback!!.onRemove(frameView)
    }

    override fun <V> onDismiss(frameView: V) where V : View?, V : IECFrame? {
        mFrame = null
        frameView!!.invalidate()
        if (mCallback != null) {
            mCallback!!.onDismiss(frameView)
        }
    }

    override fun <V> onShowing(frameView: V) where V : View?, V : IECFrame? {
        frameView!!.invalidate()
        if (mCallback != null) {
            mCallback!!.onShowing(frameView)
        }
    }

}