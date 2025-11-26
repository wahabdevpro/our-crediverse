package systems.concurrent.crediversemobile.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity

object EventUtil {
    const val EVENT_CONSUMED = true
    const val EVENT_NOT_CONSUMED = false
}

class ActivityUtils {
    companion object {
        fun hideKeyboard(fragmentActivity: FragmentActivity, view: View? = null) {
            hideKeyboard(fragmentActivity as Activity, view)
        }

        private fun hideKeyboard(activity: Activity, view: View? = null) {
            val windowView = view ?: activity.findViewById(android.R.id.content)
            val imm =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(windowView.windowToken, 0)
        }

        @SuppressLint("DiscouragedApi", "InternalInsetResource")
        fun getNavigationBarHeight(context: Context): Int {
            val resources = context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")

            return if (resourceId > 0) {
                // Navigation bar height found using the system resource
                resources.getDimensionPixelSize(resourceId)
            } else {
                // No system resource found, fallback to other methods
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) context.display
                    else windowManager.defaultDisplay

                val realMetrics = DisplayMetrics()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Using WindowInsets.getInsets() for Android 11 and above
                    val window = (context as Activity).window
                    val windowInsets = window.decorView.rootWindowInsets
                    val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
                    return insets.bottom
                } else {
                    // Using Display.getRealMetrics() for Android versions prior to 11
                    display?.getRealMetrics(realMetrics)
                }

                val usableMetrics = DisplayMetrics()
                display?.getMetrics(usableMetrics)

                val navigationBarHeight = realMetrics.heightPixels - usableMetrics.heightPixels

                // Adjusting for screen orientation
                if (navigationBarHeight > 0) {
                    return navigationBarHeight
                } else {
                    return 0
                }
            }
        }

    }
}