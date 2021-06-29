package com.github.vfeng.image.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import com.github.vfeng.image.model.ECRecover
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLConnection

object ImageUtils {
    private val M = Matrix()

    fun inSampleSize(rawSampleSize: Int): Int {
        var raw = rawSampleSize
        var ans = 1
        while (raw > 1) {
            ans = ans shl 1
            raw = raw shr 1
        }
        if (ans != rawSampleSize) {
            ans = ans shl 1
        }
        return ans
    }

    fun fitRecovering(win: RectF, frame: RectF, isJustInner: Boolean): ECRecover? {
        val dRecovering = ECRecover(0f, 0f, 1f, 0f)
        if (frame.contains(win) && !isJustInner) {
            // 不需要Fit
            return dRecovering
        }

        // 宽高都小于Win，才有必要放大
        if (isJustInner || frame.width() < win.width() && frame.height() < win.height()) {
            dRecovering.scale = Math.min(win.width() / frame.width(), win.height() / frame.height())
        }
        val rect = RectF()
        M.setScale(dRecovering.scale, dRecovering.scale, frame.centerX(), frame.centerY())
        M.mapRect(rect, frame)
        if (rect.width() < win.width()) {
            dRecovering.x += win.centerX() - rect.centerX()
        } else {
            if (rect.left > win.left) {
                dRecovering.x += win.left - rect.left
            } else if (rect.right < win.right) {
                dRecovering.x += win.right - rect.right
            }
        }
        if (rect.height() < win.height()) {
            dRecovering.y += win.centerY() - rect.centerY()
        } else {
            if (rect.top > win.top) {
                dRecovering.y += win.top - rect.top
            } else if (rect.bottom < win.bottom) {
                dRecovering.y += win.bottom - rect.bottom
            }
        }
        return dRecovering
    }

    fun fitRecovering(win: RectF, frame: RectF, centerX: Float, centerY: Float): ECRecover? {
        val dRecovering = ECRecover(0f, 0f, 1f, 0f)
        if (frame.contains(win)) {
            // 不需要Fit
            return dRecovering
        }

        // 宽高都小于Win，才有必要放大
        if (frame.width() < win.width() && frame.height() < win.height()) {
            dRecovering.scale = Math.min(win.width() / frame.width(), win.height() / frame.height())
        }
        val rect = RectF()
        M.setScale(dRecovering.scale, dRecovering.scale, centerX, centerY)
        M.mapRect(rect, frame)
        if (rect.width() < win.width()) {
            dRecovering.x += win.centerX() - rect.centerX()
        } else {
            if (rect.left > win.left) {
                dRecovering.x += win.left - rect.left
            } else if (rect.right < win.right) {
                dRecovering.x += win.right - rect.right
            }
        }
        if (rect.height() < win.height()) {
            dRecovering.y += win.centerY() - rect.centerY()
        } else {
            if (rect.top > win.top) {
                dRecovering.y += win.top - rect.top
            } else if (rect.bottom < win.bottom) {
                dRecovering.y += win.bottom - rect.bottom
            }
        }
        return dRecovering
    }


    fun fillRecovering(win: RectF, frame: RectF, pivotX: Float, pivotY: Float): ECRecover? {
        val dRecovering = ECRecover(0f, 0f, 1f, 0f)
        if (frame.contains(win)) {
            // 不需要Fill
            return dRecovering
        }
        if (frame.width() < win.width() || frame.height() < win.height()) {
            dRecovering.scale = Math.max(win.width() / frame.width(), win.height() / frame.height())
        }
        val rect = RectF()
        M.setScale(dRecovering.scale, dRecovering.scale, pivotX, pivotY)
        M.mapRect(rect, frame)
        if (rect.left > win.left) {
            dRecovering.x += win.left - rect.left
        } else if (rect.right < win.right) {
            dRecovering.x += win.right - rect.right
        }
        if (rect.top > win.top) {
            dRecovering.y += win.top - rect.top
        } else if (rect.bottom < win.bottom) {
            dRecovering.y += win.bottom - rect.bottom
        }
        return dRecovering
    }

    fun center(win: RectF, frame: RectF) {
        frame.offset(win.centerX() - frame.centerX(), win.centerY() - frame.centerY())
    }

    fun fitCenter(win: RectF, frame: RectF) {
        fitCenter(win, frame, 0f)
    }

    fun fitCenter(win: RectF, frame: RectF, padding: Float) {
        fitCenter(win, frame, padding, padding, padding, padding)
    }

    fun fitCenter(win: RectF, frame: RectF, paddingLeft: Float, paddingTop: Float, paddingRight: Float, paddingBottom: Float) {
        var paddingLeft = paddingLeft
        var paddingTop = paddingTop
        var paddingRight = paddingRight
        var paddingBottom = paddingBottom
        if (win.isEmpty || frame.isEmpty) {
            return
        }
        if (win.width() < paddingLeft + paddingRight) {
            paddingRight = 0f
            paddingLeft = paddingRight
            // 忽略Padding 值
        }
        if (win.height() < paddingTop + paddingBottom) {
            paddingBottom = 0f
            paddingTop = paddingBottom
            // 忽略Padding 值
        }
        val w = win.width() - paddingLeft - paddingRight
        val h = win.height() - paddingTop - paddingBottom
        val scale = Math.min(w / frame.width(), h / frame.height())

        // 缩放FIT
        frame[0f, 0f, frame.width() * scale] = frame.height() * scale

        // 中心对齐
        frame.offset(
                win.centerX() + (paddingLeft - paddingRight) / 2 - frame.centerX(),
                win.centerY() + (paddingTop - paddingBottom) / 2 - frame.centerY()
        )
    }

    fun fill(win: RectF, frame: RectF): ECRecover? {
        val dRecovering = ECRecover(0F, 0F, 1F, 0F)
        if (win == frame) {
            return dRecovering
        }

        // 第一次时缩放到裁剪区域内
        dRecovering.scale = Math.max(win.width() / frame.width(), win.height() / frame.height())
        val rect = RectF()
        M.setScale(dRecovering.scale, dRecovering.scale, frame.centerX(), frame.centerY())
        M.mapRect(rect, frame)
        dRecovering.x += win.centerX() - rect.centerX()
        dRecovering.y += win.centerY() - rect.centerY()
        return dRecovering
    }

    fun saveBitmap(bitmap: Bitmap?, path: String): String {
        var filePath = ""
        if (bitmap != null) {
            val file = File(path)
            file.getParentFile().mkdirs()

            if (!file.exists()) {
                file.createNewFile()
            }

            var fout: FileOutputStream? = null
            try {
                fout = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {
                if (fout != null) {
                    try {
                        fout.close()
                        filePath = file.path
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return filePath
    }

    fun getMimeType(file: File): String {
        val fileNameMap = URLConnection.getFileNameMap();
        val type = fileNameMap.getContentTypeFor(file.getName());
        return type
    }
}