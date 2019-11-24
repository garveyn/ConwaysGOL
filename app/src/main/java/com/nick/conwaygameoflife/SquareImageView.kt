package com.nick.conwaygameoflife

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import kotlin.math.min


/**
 * This was absolutely the most painful thing in the world. You don't know how long I was looking
 * for this solution cause I don't know how long I was looking for it...
 *
 * Inspired from: https://stackoverflow.com/a/10157573/12369045, this ensures the cells will be
 * square, thus fixing the issue I had before where large grids would not be too large and take up
 * more than the screen
 */
class SquareImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : ImageView(context, attrs, defStyleAttr, defStyleRes) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measurement = min(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(measurement, measurement)
    }

}