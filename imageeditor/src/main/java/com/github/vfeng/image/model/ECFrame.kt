package com.github.vfeng.image.model

import android.graphics.Canvas
import android.graphics.Matrix
import com.github.vfeng.image.frame.IECFrame


class ECFrame {
    private val M = Matrix()

    /**
     * 当前选中磁片
     */
    private var foreFrame: IECFrame? = null

    /**
     * 未被选中磁片
     */
    private var backFrames: MutableList<IECFrame>? = null


    fun init() {
        backFrames = mutableListOf<IECFrame>()
    }

    fun <S : IECFrame?> addFrame(frame: S?) {
        frame?.let {
            moveToForeground(it)
        }
    }

    fun removeFrame(frame: IECFrame?) {
        if (foreFrame === frame) {
            foreFrame = null
        } else {
            backFrames!!.remove(frame)
        }
    }

    //高亮
    fun moveToForeground(frame: IECFrame?) {
        if (frame == null) return
        moveToBackground(foreFrame)
        if (frame.isShowing) {
            foreFrame = frame
            backFrames!!.remove(frame)
        } else frame.show()
    }

    //移到未选中集合中
    fun moveToBackground(frame: IECFrame?) {
        if (frame == null) return
        if (!frame.isShowing) {
            if (!backFrames!!.contains(frame)) {
                backFrames!!.add(frame)
            }
            if (foreFrame === frame) {
                foreFrame = null
            }
        } else frame.dismiss()
    }

    fun stickAll() {
        moveToBackground(foreFrame)
    }

    fun onDismiss(frame: IECFrame?) {
        moveToBackground(frame)
    }

    fun onShowing(frame: IECFrame?) {
        if (foreFrame !== frame) {
            moveToForeground(frame)
        }
    }

    fun onDrawFrames(canvas: Canvas?) {
        if (backFrames!!.isEmpty()) return
        canvas?.save()
        for (frame in backFrames!!) {
            if (!frame.isShowing) {
                val tPivotX = frame.x + frame.pivotX
                val tPivotY = frame.y + frame.pivotY
                canvas?.save()
                M.setTranslate(frame.x, frame.y)
                M.postScale(frame.scale, frame.scale, tPivotX, tPivotY)
                M.postRotate(frame.rotation, tPivotX, tPivotY)
                canvas?.concat(M)
                frame.onFrame(canvas)
                canvas?.restore()
            }
        }
        canvas?.restore()
    }

    fun onScale(factor: Float, focusX: Float, focusY: Float) {
        backFrames?.let {
            for (frame in it) {
                M.mapRect(frame.frame)
                val tPivotX: Float = frame.x + frame.pivotX
                val tPivotY: Float = frame.y + frame.pivotY
                frame.addScale(factor)
                frame.x = frame.x + frame.frame.centerX() - tPivotX
                frame.y = frame.y + frame.frame.centerY() - tPivotY
            }
        }
    }
}