package com.github.vfeng.image.model

import android.graphics.*


class ECMosaic {
    private lateinit var pathPaint: Paint
    private lateinit var bitmapPaint: Paint

    private lateinit var mosaicPaths: MutableList<ECMosaicPath>

    var mosaicImage: Bitmap?= null

    companion object {
        const val BASE_MOSAIC_WIDTH = 40f
    }

    fun init(){

        mosaicPaths = mutableListOf()

        // 马赛克路径画刷
        pathPaint = Paint(Paint.FAKE_BOLD_TEXT_FLAG)
        pathPaint.color = 0xFF000000.toInt()
        pathPaint.setStrokeCap(Paint.Cap.ROUND)
        pathPaint.setStrokeJoin(Paint.Join.ROUND)
        pathPaint.setStrokeWidth(BASE_MOSAIC_WIDTH)
        pathPaint.setPathEffect(CornerPathEffect(20f))
        pathPaint.setStyle(Paint.Style.STROKE)

        bitmapPaint = Paint()
        bitmapPaint.setFilterBitmap(false)
        bitmapPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    }

    fun makeMosaicBitmap(image: Bitmap) {
        if (mosaicImage != null || image == null) {
            return
        }

        var w = Math.round(image.width / 16f)
        var h = Math.round(image.height / 16f)
        w = Math.max(w, 8)
        h = Math.max(h, 8)

        // 先创建小图
        mosaicImage = Bitmap.createScaledBitmap(image, w, h, false);
        // 再把小图放大
        mosaicImage = Bitmap.createScaledBitmap(mosaicImage!!, image.width, image.height, false);
    }

    fun getPathPaint(): Paint {
        return pathPaint
    }

    fun getBitmapPaint(): Paint {
        return bitmapPaint
    }

    fun drawPaths(canvas: Canvas?) {
        if (!mosaicPaths.isEmpty()) {
            for (path in mosaicPaths) {
                pathPaint.strokeWidth = path.width
                path.path?.let {
                    canvas?.drawPath(it, pathPaint)
                }
            }
        }
    }

    //判断是否有马赛克数据
    fun isMosaicEmpty(): Boolean {
        return mosaicPaths.isEmpty()
    }

    //添加一条path路径
    fun addPath(path: ECMosaicPath) {
        mosaicPaths.add(path)
    }

    //撤销最近的一条路径
    fun removeRecentPath() {
        if (mosaicPaths.isEmpty()) {
            return
        }
        mosaicPaths.removeAt(mosaicPaths.size - 1)
    }
}