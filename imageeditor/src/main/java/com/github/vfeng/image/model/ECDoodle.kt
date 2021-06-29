package com.github.vfeng.image.model

import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint

/**
 * Created by vfeng on 2020/8/17.
 */
class ECDoodle {
    private lateinit var doodles: MutableList<ECPath>
    private lateinit var doodlePaint: Paint//涂鸦画笔

    companion object {
        const val BASE_DOODLE_WIDTH = 10f
    }

    fun init() {
        doodles =  mutableListOf()
        // 涂鸦画刷
        doodlePaint = Paint()
        doodlePaint.color = 0xFFFF5151.toInt()
        doodlePaint.isAntiAlias = true
        doodlePaint.setStyle(Paint.Style.STROKE)
        doodlePaint.setStrokeWidth(BASE_DOODLE_WIDTH)
        doodlePaint.setPathEffect(CornerPathEffect(20f))
        doodlePaint.setStrokeCap(Paint.Cap.ROUND)
        doodlePaint.setStrokeJoin(Paint.Join.ROUND)
    }

    fun getDoodlePaint(): Paint {
        return doodlePaint
    }

    //添加一条path路径
    fun addPath(path: ECPath) {
        doodles.add(path)
    }

    //撤销最近的一条路径
    fun removeRecentPath() {
        if (doodles.isEmpty()) {
            return
        }
        doodles.removeAt(doodles.size - 1)
    }

    fun drawPaths(canvas: Canvas?) {
        if (!doodles.isEmpty()) {
            for (path in doodles) {
                doodlePaint.strokeWidth = BASE_DOODLE_WIDTH
                doodlePaint.color = path.color
                path.path?.let {
                    canvas?.drawPath(it, doodlePaint)
                }
            }
        }
    }
}