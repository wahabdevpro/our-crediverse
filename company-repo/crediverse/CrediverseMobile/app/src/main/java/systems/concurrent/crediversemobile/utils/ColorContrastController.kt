package systems.concurrent.crediversemobile.utils

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import systems.concurrent.crediversemobile.R

class ColorContrastController(private val context: Context) {
    val lightTextColor get() = ContextCompat.getColor(context, R.color.light)
    private val darkTextColor get() = ContextCompat.getColor(context, R.color.black)

    fun getTextColorResource(backgroundColorResource: Int): Int {
        // Get the background color of the view from colors.xml
        val backgroundColor = ContextCompat.getColor(context, backgroundColorResource)

        // Calculate the contrast ratio between the background color and black
        val contrastWithBlack = ColorUtils.calculateContrast(backgroundColor, darkTextColor)

        // Calculate the contrast ratio between the background color and white
        val contrastWithWhite = ColorUtils.calculateContrast(backgroundColor, lightTextColor)

        // Determine whether the background color is light or dark
        val isLight = contrastWithBlack > contrastWithWhite

        // Set the text color based on whether the background color is light or dark
        return if (isLight)
            darkTextColor
        else
            lightTextColor
    }
}