package com.github.vfeng.image.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RadioGroup

class ECColorGroup : RadioGroup {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    var checkColor: Int
        get() {
            val checkedId = checkedRadioButtonId
            val radio: ECColorRadio = findViewById(checkedId)
            return radio?.getColor() ?: Color.WHITE
        }
        set(color) {
            val count = childCount
            for (i in 0 until count) {
                val radio = getChildAt(i) as ECColorRadio
                if (radio.getColor() == color) {
                    radio.isChecked = true
                    break
                }
            }
        }

}