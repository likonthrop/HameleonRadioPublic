package com.anisimov.radioonline.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.anisimov.radioonline.R
import kotlin.random.Random

class CustomProgressBar : View {

    private var mHeight = 0f
    private var mWidth = 0f
    private var divider = .5f

    private val random = Random
    private var mPaint: Paint? = null
    private var isInit = false

    private var pos1 = 0f
    private var pos2 = 0f
    private var pos3 = 0f

    private var rndHeight1 = 0f
    private var rndHeight2 = 0f
    private var rndHeight3 = 0f

    private var enable = true

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun enable(enable: Boolean = true) {
        this.enable = enable
    }

    override fun onDraw(canvas: Canvas) {
        if (!isInit) {
            mPaint = Paint()
            mHeight = height.toFloat()
            mWidth = width.toFloat()
            isInit = true
            if (pos1 == rndHeight1) pos1 = random.nextInt(5, mHeight.toInt()).toFloat()
            if (pos2 == rndHeight2) pos2 = random.nextInt(5, mHeight.toInt()).toFloat()
            if (pos3 == rndHeight3) pos3 = random.nextInt(5, mHeight.toInt()).toFloat()
        }

        mPaint?.apply {
            if (pos1 == rndHeight1) rndHeight1 = random.nextInt(5, mHeight.toInt()).toFloat()
            if (pos2 == rndHeight2) rndHeight2 = random.nextInt(5, mHeight.toInt()).toFloat()
            if (pos3 == rndHeight3) rndHeight3 = random.nextInt(5, mHeight.toInt()).toFloat()

            color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(R.color.progressBarColor, resources.newTheme())
            } else resources.getColor(R.color.progressBarColor)
            style = Paint.Style.FILL
            isAntiAlias = true

            if (enable) {
                pos1 = if (pos1 < rndHeight1) pos1 + 1f else pos1 - 1f
                pos2 = if (pos2 < rndHeight2) pos2 + 1f else pos2 - 1f
                pos3 = if (pos3 < rndHeight3) pos3 + 1f else pos3 - 1f
            }

            canvas.drawRect(0f, mHeight, mWidth / 3f - divider, pos1, this)
            canvas.drawRect(mWidth / 3f + divider, mHeight, (mWidth / 3f) * 2f - divider, pos2, this)
            canvas.drawRect((mWidth / 3f) * 2f + divider, mHeight, mWidth, pos3, this)
        }

        postInvalidateDelayed(20)
        invalidate()
    }
}