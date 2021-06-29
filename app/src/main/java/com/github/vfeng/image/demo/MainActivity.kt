package com.github.vfeng.image.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.PermissionUtils
import com.github.vfeng.image.ECImageManager
import com.github.vfeng.image.utils.LogUtils

class MainActivity : AppCompatActivity() {

    companion object {
        const val PICK_PHOTO = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    fun selectedImage(view: View) {

        val permissions = arrayOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )

        if (PermissionUtils.isGranted(*permissions)) {
            //打开相册
            val intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PHOTO);
        } else {
            PermissionUtils.permission(*permissions).request()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO) {
            if (resultCode == RESULT_OK) {
                val uri = data?.data as Uri
                val filePath = FileUtils.getFilePath(this, uri)
                filePath?.let {
                    stepEditor(it)
                }
            }
        }
    }

    fun stepEditor(filePath: String) {
        val uri = Uri.parse("file://" + filePath)

        Log.d("MainActivity", "file path : " + filePath)
        ECImageManager.startImageEditor(
            this,
            uri,
            object : ECImageManager.ECImageEditorResultCallback {
                override fun onImageEditDone(saveUri: Uri) {
                    LogUtils.d("ECImageManager", "onImageEditDone url : " + saveUri.toString())
                }

                override fun onImageEditFail() {
                    LogUtils.d("ECImageManager", "onImageEditFail")
                }

                override fun onImageEditCancel() {
                    LogUtils.d("ECImageManager", "onImageEditCancel")
                }

            })
    }
}