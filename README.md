# FImageEditor
图片编辑，可进行涂鸦，马赛克，添加文字，裁剪，编辑过程可手势放大、缩小、旋转

# 引用

1.在项目根目录build.gradle中添加JitPack

```gradle
  allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
  }
```

2.在build.gradle中添加dependency

```gradle
  dependencies {
     implementation 'com.github.v-jibfen:FImageEditor:1.0.0'
  }
```

3.开启编辑

```java
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
```
