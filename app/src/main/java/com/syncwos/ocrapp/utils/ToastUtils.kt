package com.syncwos.ocrapp.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {

    fun showLongMessage(message: String?, context: Context?) {
        getToast(message, context).show()
    }

    private fun getToast(message: String?, context: Context?): Toast {
        return Toast.makeText(context, message, Toast.LENGTH_LONG)
    }
}