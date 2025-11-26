package systems.concurrent.crediversemobile.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.fonts.Font
import androidx.core.content.res.ResourcesCompat
import systems.concurrent.crediversemobile.R

class DrawableFont(private val context: Context) {

    enum class FontFamily {
        SOLID,
        REGULAR
    }

    fun from(icon: Icon, family: FontFamily = FontFamily.SOLID): BitmapDrawable {
        val familyResource = when(family) {
            FontFamily.SOLID -> R.font.fa_solid
            FontFamily.REGULAR -> R.font.fa_regular
        }

        val myTypeface = ResourcesCompat.getFont(context, familyResource)
        val paint = Paint().apply {
            typeface = myTypeface
        }

        paint.textSize = 24f
        paint.color = Color.WHITE

        val textWidth = paint.measureText(icon.unicode)
        val textBounds = Rect()
        paint.getTextBounds(icon.unicode, 0, icon.unicode.length, textBounds)
        val textHeight = textBounds.height()

        val bitmap = Bitmap.createBitmap(textWidth.toInt(), textHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(icon.unicode, 0f, textHeight - paint.descent(), paint)

        return BitmapDrawable(context.resources, bitmap)
    }
}