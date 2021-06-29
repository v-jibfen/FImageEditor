package com.github.vfeng.image.view.frame

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.github.vfeng.image.frame.ECText
import com.github.vfeng.image.frame.ECTextEditDialog

class IECFrameTextView : ECFrameView, ECTextEditDialog.Callback {
    private var mTextView: ECTextView? = null
    private var mText: ECText? = null
    private var mDialog: ECTextEditDialog? = null

    constructor(context: Context?) : this(context, null) {}
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onInitialize(context: Context) {
        super.onInitialize(context)
    }

    override fun onCreateContentView(context: Context): View {
        mTextView = ECTextView(context)
        return mTextView!!
    }

    var text: ECText?
        get() = mText
        set(text) {
            mText = text
            if (mText != null && mTextView != null) {
                mTextView!!.setText(text!!)
            }
        }

    override fun onContentTap() {
        val dialog = dialog
        dialog.setText(mText)
        dialog.show()
    }

    private val dialog: ECTextEditDialog
        private get() {
            if (mDialog == null) {
                mDialog = ECTextEditDialog(context, this)
            }
            return mDialog!!
        }

    override fun onText(text: ECText) {
        mText = text
        if (mText != null && mTextView != null) {
            mTextView!!.setText(text)
        }
    }

    companion object {
        private const val TAG = "IECFrameTextView"
    }
}