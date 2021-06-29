package com.github.vfeng.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment

object ECImageManager {
    const val INTENT_IMAGE_PATH = "intent_image_path"
    const val INTENT_SAVE_PATH = "intent_save_path"
    const val INTENT_REQUEST = 33008
    val DEFAULT_SAVE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path

    var callback: ECImageEditorResultCallback? = null

    /**
     * @param context 上下文
     * @param imageUri 图片本地路径
     */
    fun startImageEditor(context: Activity, imageUri: Uri, callback: ECImageEditorResultCallback?) {
        context.startActivityForResult(Intent(context, ECImageEditorActivity::class.java)
                .putExtra(INTENT_IMAGE_PATH, imageUri), INTENT_REQUEST)
        this.callback = callback
    }

    /**
     * @param context 上下文
     * @param imageUri 图片本地路径
     * @param savePath 图片保存路径，不传则保存默认路径
     */
    fun startImageEditor(context: Activity, imageUri: Uri, savePath: String, callback: ECImageEditorResultCallback?) {
        context.startActivityForResult(Intent(context, ECImageEditorActivity::class.java)
                .putExtra(INTENT_IMAGE_PATH, imageUri)
                .putExtra(INTENT_SAVE_PATH, savePath), INTENT_REQUEST)
        this.callback = callback
    }

    interface ECImageEditorResultCallback {
        fun onImageEditDone(saveUri: Uri)
        fun onImageEditFail()
        fun onImageEditCancel()
    }
}