package com.github.vfeng.image.frame

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.RadioGroup
import com.github.vfeng.image.R
import com.github.vfeng.image.view.ECColorGroup

class ECTextEditDialog(context: Context, callback: Callback?) : Dialog(context, R.style.ImageTextDialog), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private lateinit var mEditText: EditText
    private var mCallback: Callback?= null
    private var mDefaultText: ECText? = null
    private lateinit var mColorGroup: ECColorGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.image_text_dialog)

        val window = window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

        mColorGroup = findViewById(R.id.cg_colors)
        mColorGroup.setOnCheckedChangeListener(this)
        mEditText = findViewById(R.id.et_text)
        findViewById<View>(R.id.tv_cancel).setOnClickListener(this)
        findViewById<View>(R.id.tv_done).setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (mDefaultText != null) {
            mEditText.setText(mDefaultText!!.text)
            mEditText.setTextColor(mDefaultText!!.color)
            if (!mDefaultText!!.isEmpty) {
                mEditText!!.setSelection(mEditText!!.length())
            }
            mDefaultText = null
        } else mEditText!!.setText("")
        mColorGroup!!.checkColor = mEditText!!.currentTextColor
    }

    fun setText(text: ECText?) {
        mDefaultText = text
    }

    fun reset() {
        setText(ECText("", Color.WHITE))
    }

    override fun onClick(v: View) {
        val vid = v.id
        if (vid == R.id.tv_done) {
            onDone()
        } else if (vid == R.id.tv_cancel) {
            dismiss()
        }
    }

    private fun onDone() {
        val text = mEditText!!.text.toString()
        if (!TextUtils.isEmpty(text) && mCallback != null) {
            mCallback?.onText(ECText(text, mEditText.currentTextColor))
        }
        dismiss()
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        mEditText.setTextColor(mColorGroup!!.checkColor)
    }

    interface Callback {
        fun onText(text: ECText)
    }

    companion object {
        private const val TAG = "ECTextEditDialog"
    }

    init {
        this.mCallback = callback
    }
}