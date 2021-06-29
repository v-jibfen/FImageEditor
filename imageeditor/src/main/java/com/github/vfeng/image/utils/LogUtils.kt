package com.github.vfeng.image.utils

import android.util.Log

object LogUtils {
    const val PREFIX = "[RongLog][ImageEditor]"
    private var debug = true
    fun setDebug(b: Boolean) {
        debug = b
    }

    fun v(TAG: String, msg: String) {
        if (!debug) {
            return
        }
        Log.v(PREFIX + TAG, msg)
    }

    fun d(TAG: String, msg: String) {
        if (!debug) {
            return
        }
        val NewTAG = PREFIX + TAG
        Log.d(NewTAG, msg)
    }

    fun i(TAG: String, msg: String) {
        if (!debug) {
            return
        }
        Log.i(PREFIX + TAG, msg)
    }

    fun w(TAG: String, msg: String) {
        if (!debug) {
            return
        }
        Log.w(PREFIX + TAG, msg)
    }

    fun e(TAG: String, msg: String) {
        if (!debug) {
            return
        }
        Log.e(PREFIX + TAG, msg)
    }

    fun wtf(TAG: String, msg: String) {
        if (!debug) {
            return
        }
        Log.wtf(PREFIX + TAG, msg)
    }
}