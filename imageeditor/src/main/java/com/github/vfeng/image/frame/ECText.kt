package com.github.vfeng.image.frame

import android.graphics.Color
import android.text.TextUtils

class ECText(var text: String, color: Int) {
    var color = Color.WHITE

    val isEmpty: Boolean
        get() = TextUtils.isEmpty(text)

    fun length(): Int {
        return if (isEmpty) 0 else text.length
    }

    init {
        this.color = color
    }
}