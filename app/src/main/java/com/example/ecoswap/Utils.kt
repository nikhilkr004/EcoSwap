package com.example.ecoswap

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView

object Utils {
    private var loadingDialog: Dialog? = null

    fun showLoadingDialog(context: Context, message: String = "") {
        if (loadingDialog?.isShowing == true) return

        loadingDialog = Dialog(context)
        loadingDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_blur_loading)
            setCancelable(false)

            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }

            findViewById<TextView>(R.id.loadingText)?.text = message

            show()
        }
    }


    fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }


}