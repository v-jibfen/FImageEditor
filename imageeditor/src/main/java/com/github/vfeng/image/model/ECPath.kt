package com.github.vfeng.image.model

import android.graphics.Path

/**
 * Created by vfeng on 2020/8/17.
 */
class ECPath {
    private var identity = Int.MIN_VALUE

    var path: Path? =null

    var color: Int = 0xFFFF5151.toInt()

    fun reset() {
        this.path?.reset()
        this.path = null
        identity = Int.MIN_VALUE
    }

    fun moveTo(x: Float, y: Float) {

        this.path = Path()
        this.path?.reset()
        this.path?.moveTo(x, y)
        identity = Int.MIN_VALUE
    }

    fun setIdentity(identity: Int) {
        this.identity = identity
    }

    fun isIdentity(identity: Int): Boolean {
        return this.identity == identity
    }

    fun lineTo(x: Float, y: Float) {
        this.path?.lineTo(x, y)
    }

    fun isEmpty(): Boolean {
        return this.path == null
    }

    fun toPath(): Path? {
        return Path(path)
    }
}