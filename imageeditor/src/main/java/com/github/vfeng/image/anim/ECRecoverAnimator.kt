package com.github.vfeng.image.anim

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.github.vfeng.image.model.ECRecover

class ECRecoverAnimator : ValueAnimator() {
    var isRotate = false
        private set
    private var mEvaluator: ECRecoverEvaluator? = null

    override fun setObjectValues(vararg values: Any) {
        super.setObjectValues(*values)
        if (mEvaluator == null) {
            mEvaluator = ECRecoverEvaluator()
        }
        setEvaluator(mEvaluator)
    }

    fun setRecoveringValues(sRecover: ECRecover?, eRecover: ECRecover?) {
        setObjectValues(sRecover!!, eRecover!!)
        isRotate = ECRecover.isRotate(sRecover, eRecover)
    }

    init {
        interpolator = AccelerateDecelerateInterpolator()
    }
}