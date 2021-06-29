package com.github.vfeng.image

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RadioGroup
import com.github.vfeng.image.ECImageManager.INTENT_IMAGE_PATH
import com.github.vfeng.image.ECImageManager.INTENT_SAVE_PATH
import com.github.vfeng.image.frame.ECText
import com.github.vfeng.image.frame.ECTextEditDialog
import com.github.vfeng.image.utils.ImageUtils
import kotlinx.android.synthetic.main.image_activity_editor_home.*
import kotlinx.android.synthetic.main.image_clip_layout.*
import kotlinx.android.synthetic.main.image_doodle_mosaic_layout.*
import java.io.File

class ECImageEditorActivity: Activity(), View.OnClickListener, ECTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener,
        DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    private var textDialog: ECTextEditDialog? = null
    private var imageEditorCallback = ECImageManager.callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_activity_editor_home)

        val uri = intent.getParcelableExtra<Uri>(INTENT_IMAGE_PATH)
        uri?.let {
            imageEditorView.setImageData(uri)
        }

        initView()
        updateUiByViewMode()
    }

    private fun initView() {
        colorsGroup.setOnCheckedChangeListener(this)
        tvCancel.setOnClickListener(this)
        tvDone.setOnClickListener(this)
        rbDoodle.setOnClickListener(this)
        ibText.setOnClickListener(this)
        rbMosaic.setOnClickListener(this)
        ibClip.setOnClickListener(this)
        btnCancelPath.setOnClickListener(this)
        ibClipRotate.setOnClickListener(this)
        ibClipCancel.setOnClickListener(this)
        ibClipReset.setOnClickListener(this)
        ibClipDone.setOnClickListener(this)
    }

    private fun updateUiByViewMode() {
        val mode = imageEditorView.getMode()

        when (mode) {
            ECImageMode.NONE -> {
                modesGroup.clearCheck()
                layoutColorOption.visibility = View.GONE
            }
            ECImageMode.DOODLE -> {
                modesGroup.check(R.id.rbDoodle)
                doodleMosaicSwitcher.displayedChild = 0
                layoutColorOption.visibility = View.VISIBLE
            }
            ECImageMode.MOSAIC -> {
                modesGroup.check(R.id.rbMosaic)
                doodleMosaicSwitcher.displayedChild = 1
                layoutColorOption.visibility = View.VISIBLE
            }
            ECImageMode.CLIP -> {
                modesGroup.check(R.id.ibClip)
                drawClipSwitcher.displayedChild = 1
            }
        }
    }

    private fun onModeClick(mode: ECImageMode) {
        var modeNew = mode
        val modeNow = imageEditorView.getMode()
        if (mode == modeNow) {
            modeNew = ECImageMode.NONE
        }

        imageEditorView.setMode(modeNew)

        updateUiByViewMode()
    }

    fun onTextModeClick() {
        if (textDialog == null) {
            textDialog = ECTextEditDialog(this, this)
            textDialog?.setOnShowListener(this)
            textDialog?.setOnDismissListener(this)
        }
        textDialog?.show()
    }

    override fun onBackPressed() {
        imageEditorCallback?.let {
            it.onImageEditCancel()
        }

        super.onBackPressed()
    }

    override fun onClick(v: View?) {
        val id = v?.id
        when(id) {
            R.id.rbDoodle -> {
                onModeClick(ECImageMode.DOODLE);
            }

            R.id.ibText -> {
                updateUiByViewMode()
                onTextModeClick()
            }

            R.id.rbMosaic -> {
                onModeClick(ECImageMode.MOSAIC);
            }

            R.id.ibClip -> {
                onModeClick(ECImageMode.CLIP);
            }

            R.id.btnCancelPath -> {
                val mode = imageEditorView.getMode()
                if (mode == ECImageMode.DOODLE) {
                    imageEditorView.cancelDoodlePath()
                } else if(mode == ECImageMode.MOSAIC) {
                    imageEditorView.cancelMosaicPath()
                }
            }
            R.id.ibClipRotate -> {
                imageEditorView.doClipRotate()
            }
            R.id.ibClipCancel -> {
                imageEditorView.cancelClip()
                drawClipSwitcher.displayedChild = 0
                updateUiByViewMode()
            }
            R.id.ibClipReset -> {
                imageEditorView.resetClip()
            }
            R.id.ibClipDone -> {
                imageEditorView.doneClip()
                drawClipSwitcher.displayedChild = 0
                updateUiByViewMode()
            }
            R.id.tvCancel -> {
               onBackPressed()
            }
            R.id.tvDone -> {
                var path = intent.getStringExtra(INTENT_SAVE_PATH)
                val fileName = "${System.currentTimeMillis()}.jpg"
                if (TextUtils.isEmpty(path)) {
                    path = File(ECImageManager.DEFAULT_SAVE_PATH, fileName).path
                } else {
                    path = File(path, fileName).path
                }

                val filePath = ImageUtils.saveBitmap(imageEditorView.toBitmap(), path)
                if (!TextUtils.isEmpty(filePath)) {
                    finish()
                    // 最后通知图库更新
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(filePath)))
                    val file = File(filePath)
                    MediaScannerConnection.scanFile(this, arrayOf(file.getPath()), arrayOf(ImageUtils.getMimeType(file)), object : MediaScannerConnection.OnScanCompletedListener {
                        override fun onScanCompleted(path: String?, uri: Uri?) {
                        }
                    });

                    imageEditorCallback?.let {
                        it.onImageEditDone(Uri.parse(filePath))
                    }

                } else {
                    imageEditorCallback?.let {
                        it.onImageEditFail()
                    }
                }
            }
        }
    }

    override fun onText(text: ECText) {
        imageEditorView.addTextFrame(text)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        imageEditorView.setTepPathColor(colorsGroup.checkColor)
    }

    override fun onShow(dialog: DialogInterface?) {
        drawClipSwitcher.visibility = View.GONE
        tvCancel.visibility = View.GONE
    }

    override fun onDismiss(dialog: DialogInterface?) {
        drawClipSwitcher.visibility = View.VISIBLE
        tvCancel.visibility = View.VISIBLE
    }
}