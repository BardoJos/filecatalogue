/*
 * Copyright (c) 2022. farrusco (jos dot farrusco at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.farrusco.projectclasses.messages

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.Constants
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.utils.ViewUtils
import com.farrusco.projectclasses.widget.EditTextExt
import com.farrusco.projectclasses.widget.RingProgressBar
import com.farrusco.projectclasses.widget.TextViewExt
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.core.graphics.toColorInt

class Mess( context: Context, message:String? = null, progress: Int = 0) {

    private var dialog: AlertDialog
    private var typeImage: Int = 0
    private var imageView: ImageView? = null
    private var mRingProgressBar: RingProgressBar? = null

    init {
        dialog = context.setProgressDialog(message,progress)
        showDialog()
    }

    private fun showDialog(){
        try {
            dialog.show()
        } catch (e: Exception){
            Logging.d(Constants.APP_NAME + "/Mess/showDialog\n" + e.stackTraceToString())
        }
    }

    fun dismissDialog(){
        try {
            dialog.dismiss()
        } catch (e: Exception){
            Logging.d(Constants.APP_NAME + "/Mess/dismissDialog\n" + e.stackTraceToString())
        }
    }

    private fun Context.setProgressDialog(message: String? = null, progressShow: Int):  AlertDialog {
        val llPadding = ViewUtils.getDip(R.dimen.normal_30sp, this).toInt()
        val ll = LinearLayout(this)
        ll.setBackgroundColor(Color.WHITE)
        var mess = message
        if (message == null){
            mess = getText(R.string.mess027_please_wait).toString()
        }
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(this)
        tvText.text = mess
        tvText.setTextColor("#000000".toColorInt())
        tvText.textSize = ViewUtils.getDip(R.dimen.font_size_textview, this)
        tvText.layoutParams = llParam
        typeImage = progressShow
        when (progressShow){
            0,1 -> {
                imageView = ImageView(this)
                imageView?.setImageResource(R.drawable.waiting_normal)
                imageView?.setPadding(0, 0, llPadding, 0)
                imageView?.layoutParams = llParam
                ll.addView(imageView)
            }
            2 -> {
                mRingProgressBar = RingProgressBar(this)
                mRingProgressBar?.setPadding(0, 0, llPadding, 0)
                //mRingProgressBar?.max = 100
                mRingProgressBar?.ringWidth = ViewUtils.getDip(R.dimen.normal_18sp, this)
                mRingProgressBar?.ringProgressColor = Color.BLUE
                //mRingProgressBar?.ringColor = Color.LtGray
                //mRingProgressBar?.style = 1
                //mRingProgressBar?.textIsShow = true
                mRingProgressBar?.setProgress(1)
                mRingProgressBar?.textSize = ViewUtils.getDip(R.dimen.normal_48sp, this)
                ll.addView(mRingProgressBar)
            }
        }

        ll.addView(tvText)
        //var x1: androidx.appcompat.app.AlertDialog

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()

            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams

            // Disabling screen touch to avoid exiting the Dialog
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }
        return dialog
    }

/*    fun setProgress(status: Int){
        when (typeImage){
            1 -> {
                val rot = imageView!!.rotation.toInt()
                val rotPlus = kotlin.math.min(kotlin.math.max(1,rot + (status * 360 / 100)),360)
                imageView!!.rotation = rotPlus.toFloat()
            }
            2 -> {
                mRingProgressBar!!.setProgress(kotlin.math.max(1,status * 100 / mRingProgressBar!!.max))
            }
        }
    }*/

    companion object{

        @SuppressLint("InflateParams")
        fun buildLogDialog(context: Context, layoutInflater: LayoutInflater, alertTitle: String): AlertDialog.Builder {
            val alertDialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogTheme))
            //alertDialog.setIcon(icon)
            val titleView = layoutInflater.inflate(R.layout.alertdialog_title, null)
            val title = titleView.findViewById<TextView>(R.id.title_template)
            title.text = alertTitle
            alertDialog.setCustomTitle(titleView)

            val ins = context.resources.openRawResource(Constants.ChangeLog)
            val br = BufferedReader(InputStreamReader(ins))
            val sb = StringBuffer()
            var line: String
            while (br.readLine().also { line = it } != null) {
                line = line.trim { it <= ' ' }
                if (line.length > 2){
/*                    $ 1.0.6 <h1>
                    % Version 1.0.6 <h2>
                    _ 2012-04-15 <h3>
                    * added option screen <li><big>    */
                    when (line.substring(0,2)){
                        "$ " -> line = "<h1>" + line.substring(2) + "</h1>"
                        "% " -> line = "<h2>" + line.substring(2) + "</h2>"
                        "_ " -> line = "<h3>" + line.substring(2) + "</h3>"
                        "* " -> line = "<li><big>" + line.substring(2) + "</big></li>"
                    }
                }
                sb.append(line)
            }
            br.close()
            line = "<html><body bgcolor=\"black\" text=\"white\">$sb</body></html>"
            val wv = WebView(context)
            wv.loadDataWithBaseURL(
                null, line, "text/html", "UTF-8",
                null
            )
            alertDialog.setView(wv)
            alertDialog.setCancelable(false)
            return alertDialog
        }

        fun buildAlertDialog(context: Context, layoutInflater: LayoutInflater, alertTitle: String, alertMessage: String): AlertDialog.Builder {
            val builder = AlertDialog.Builder( ContextThemeWrapper( context, R.style.AlertDialogTheme ))
            val dialogLayout = layoutInflater.inflate(R.layout.alertdialog_title, null)
            val titleText = dialogLayout.findViewById<TextViewExt>(R.id.title_template)
            if (alertMessage.isEmpty()){
                titleText.visibility = View.GONE
            } else {
                titleText.text = alertMessage
            }

            if (alertTitle.isNotEmpty()){
                val textView = TextView(context)
                textView.text = alertTitle
                val sp20 =
                    ViewUtils.getDip(R.dimen.normal_20sp, context).toInt()
                val sp30 =
                    ViewUtils.getDip(R.dimen.normal_30sp, context).toInt()
                textView.setPadding(sp20, sp20, sp30, sp30)
                textView.textSize = sp20.toFloat()
                textView.setBackgroundColor(Color.BLUE)
                textView.setTextColor(Color.WHITE)
                builder.setCustomTitle(textView)
            }

            builder.setCancelable(false)
            builder.setView(dialogLayout)
            return builder
        }
        fun showEditTextDialog(context: Context, layoutInflater: LayoutInflater
                               , alertTitle: String, alertMessage: String
                               , edittext: String): AlertDialog.Builder {
            val builder = AlertDialog.Builder( ContextThemeWrapper( context, R.style.AlertDialogTheme ))
            val dialogLayout = layoutInflater.inflate(R.layout.alertdialog_edittext, null)
            val editText = dialogLayout.findViewById<EditTextExt>(R.id.et_editText)
            val titleText = dialogLayout.findViewById<TextViewExt>(R.id.title_template)
            if (alertMessage.isEmpty()){
                titleText.visibility = View.GONE
            } else {
                titleText.text = alertMessage
            }

            if (alertTitle.isNotEmpty()){
                val textView = TextView(context)
                textView.text = alertTitle
                val sp20 =
                    ViewUtils.getDip(R.dimen.normal_20sp, context).toInt()
                val sp30 =
                    ViewUtils.getDip(R.dimen.normal_30sp, context).toInt()
                textView.setPadding(sp20, sp20, sp30, sp30)
                textView.textSize = sp20.toFloat()
                textView.setBackgroundColor(Color.BLUE)
                textView.setTextColor(Color.WHITE)
                builder.setCustomTitle(textView)
            }

            editText.setText(edittext)
            editText.skipEditTagAlways = true
            builder.setView(dialogLayout)
            return builder
        }
    }
}