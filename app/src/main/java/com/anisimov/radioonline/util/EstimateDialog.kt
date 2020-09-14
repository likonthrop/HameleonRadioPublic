package com.anisimov.radioonline.util

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import com.anisimov.radioonline.R
import java.util.*

const val NOTIFY = "notify"
const val ESTIMATE_SP = "estimate_sp"

/**
 * Диалоговое окно с предложением оценить приложение на маркете
 * */
class EstimateDialog(context: Context) : Dialog(context) {
    init {
        setContentView(R.layout.estimate_dialog_layout)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        findViewById<Button>(R.id.button1).setOnClickListener{
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.anisimov.radioonline")))
            context.getSharedPreferences(ESTIMATE_SP, Context.MODE_PRIVATE).edit().putBoolean(NOTIFY, false).apply()
            dismiss()
        }
        findViewById<Button>(R.id.button2).setOnClickListener{
            context.getSharedPreferences(ESTIMATE_SP, Context.MODE_PRIVATE).edit().putBoolean(NOTIFY, true).apply()
            dismiss()
        }
        findViewById<Button>(R.id.button3).setOnClickListener{
            context.getSharedPreferences(ESTIMATE_SP, Context.MODE_PRIVATE).edit().putBoolean(NOTIFY, false).apply()
            dismiss()
        }
    }

    fun setOnClickListener(i: ((View) -> Unit?)): EstimateDialog {
        findViewById<Button>(R.id.button1).setOnClickListener{i.invoke(it)}
        findViewById<Button>(R.id.button2).setOnClickListener{i.invoke(it)}
        findViewById<Button>(R.id.button3).setOnClickListener{i.invoke(it)}
        return this
    }

    fun onDismiss(i: () -> Unit?): EstimateDialog {
        setOnDismissListener{i.invoke()}
        return this
    }
}

/**
 * Задача для таймера
 * */
class EstimateTimerTask(val context: Context): TimerTask() {
    override fun run() {
            //Стартует Диалог с предложением оценить приложение
            Handler(Looper.getMainLooper()).post { EstimateDialog(context).show() }
    }
}

/**
 * Таймер
 * */
class EstimateTimer(context: Context, delay: Long, name: String = "estimate_timer"): Timer(name) {
    init {
        //Запускает задачу в случае если приложение не оценено
        if (context.getSharedPreferences(ESTIMATE_SP, Context.MODE_PRIVATE).getBoolean(NOTIFY, true)) {
            schedule(EstimateTimerTask(context), delay)
        }
    }
}