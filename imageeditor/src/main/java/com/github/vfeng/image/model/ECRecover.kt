package com.github.vfeng.image.model

class ECRecover {

    var x = 0f

    var y = 0f

    var scale = 0f

    var rotate = 0f

    constructor(x: Float, y: Float, scale: Float, rotate: Float) {
        this.x = x
        this.y = y
        this.scale = scale
        this.rotate = rotate
    }

    fun set(x: Float, y: Float, scale: Float, rotate: Float) {
        this.x = x
        this.y = y
        this.scale = scale
        this.rotate = rotate
    }

    fun concat(recover: ECRecover) {
        scale *= recover.scale
        x += recover.x
        y += recover.y
    }

    fun rConcat(recover: ECRecover) {
        scale *= recover.scale
        x -= recover.x
        y -= recover.y
    }

    companion object {
        fun isRotate(sRecover: ECRecover, eRecover: ECRecover): Boolean {
            return java.lang.Float.compare(sRecover.rotate, eRecover.rotate) != 0
        }
    }


    override fun toString(): String {
        return "ECRecover{" +
                "x=" + x +
                ", y=" + y +
                ", scale=" + scale +
                ", rotate=" + rotate +
                '}'
    }
}