package com.github.vfeng.image.anim

import android.animation.TypeEvaluator
import com.github.vfeng.image.model.ECRecover

class ECRecoverEvaluator : TypeEvaluator<ECRecover> {
    private var recover: ECRecover? = null

    constructor() {}
    constructor(recover: ECRecover?) {
        this.recover = recover
    }

    override fun evaluate(fraction: Float, startValue: ECRecover, endValue: ECRecover): ECRecover {
        val x = startValue.x + fraction * (endValue.x - startValue.x)
        val y = startValue.y + fraction * (endValue.y - startValue.y)
        val scale = startValue.scale + fraction * (endValue.scale - startValue.scale)
        val rotate = startValue.rotate + fraction * (endValue.rotate - startValue.rotate)
        if (recover == null) {
            recover = ECRecover(x, y, scale, rotate)
        } else {
            recover!!.set(x, y, scale, rotate)
        }
        return recover!!
    }
}