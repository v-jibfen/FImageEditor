package com.github.vfeng.image.view.frame

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.github.vfeng.image.R

class IECFrameImageView : ECFrameView {
    private var mImageView: ImageView? = null

    constructor(context: Context?) : this(context, null) {}
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onCreateContentView(context: Context): View {
        mImageView = ImageView(context)
        mImageView!!.setImageResource(R.drawable.rce_ic_logo)
        return mImageView!!
    }
}