package systems.concurrent.crediversemobile.services

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.fragment.app.FragmentActivity
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.utils.CustomUtils
import systems.concurrent.crediversemobile.utils.Formatter
import systems.concurrent.crediversemobile.utils.Icon

enum class ToasterType(private val _color: Int, private val _icon: Icon) {
    SUCCESS(R.color.success, Icon.CHECK_DOUBLE),
    ERROR(R.color.danger, Icon.X_MARK),
    WARN(R.color.warn_light, Icon.EXCLAMATION),
    INFO(R.color.info, Icon.INFO);

    val color get() = this._color
    val icon get() = this._icon
}

class Toaster {
    class Options {
        private var _toasterType: ToasterType = ToasterType.INFO
        private var _toastTime: Int = Snackbar.LENGTH_LONG

        private var _customRootView: View? = null

        private var _topMarginInDp: Float? = null

        val toasterType get() = _toasterType
        val toastTime get() = _toastTime
        val customRootView get() = _customRootView
        val topMargin get() = _topMarginInDp

        fun setType(toasterType: ToasterType): Options {
            _toasterType = toasterType
            return this
        }

        fun setTime(time: Int): Options {
            _toastTime = when (time) {
                in listOf(
                    Snackbar.LENGTH_LONG, Snackbar.LENGTH_SHORT,
                    Snackbar.LENGTH_INDEFINITE
                ) -> time
                else -> Snackbar.LENGTH_LONG
            }
            return this
        }

        fun setRootView(view: View?): Options {
            _customRootView = view
            return this
        }

        fun setTopMargin(value: Float?): Options {
            _topMarginInDp = value
            return this
        }
    }

    companion object {

        fun showCustomToast(
            fragmentActivity: FragmentActivity, message: CharSequence, options: Options = Options()
        ) {
            showCustomToast(fragmentActivity as Activity, message, options)
        }

        fun showCustomToast(
            activity: Activity, message: CharSequence, options: Options = Options()
        ) {
            val view =
                options.customRootView ?: activity.findViewById(android.R.id.content) ?: return

            val snackBar = Snackbar.make(view, "", options.toastTime)

            val params = snackBar.view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackBar.view.layoutParams = params

            // inflate the custom_snackbar_view created previously
            val customSnackView: View = LayoutInflater.from(activity).inflate(R.layout.toast, null)

            snackBar.view.setBackgroundColor(Color.TRANSPARENT)

            val bgColor = activity.getColor(options.toasterType.color)
            val icon = options.toasterType.icon

            if (options.topMargin != null) {
                val toastRootWrapper =
                    customSnackView.findViewById<ConstraintLayout>(R.id.toast_root_wrapper)
                val currentLayoutParams = if (toastRootWrapper.layoutParams == null) {
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                } else {
                    toastRootWrapper.layoutParams as LayoutParams
                }

                // Convert dp to pixels
                val density = activity.resources.displayMetrics.density
                val topMarginInPixels = (options.topMargin!! * density).toInt()
                currentLayoutParams.setMargins(0, topMarginInPixels, 0, 0)
                toastRootWrapper.layoutParams = currentLayoutParams
            }


            val toastWrapper = customSnackView.findViewById<FlexboxLayout>(R.id.toast_wrapper)
            toastWrapper.setBackgroundColor(bgColor)
            val iconView = customSnackView.findViewById<TextView>(R.id.toast_icon)
            iconView.text = icon.unicode
            val iconNotch = customSnackView.findViewById<TextView>(R.id.toast_icon_notch)
            iconNotch.setBackgroundColor(bgColor)

            val toastClose = customSnackView.findViewById<TextView>(R.id.toast_close)
            toastClose.setOnClickListener { snackBar.dismiss() }

            val messageView = customSnackView.findViewById<TextView>(R.id.message)
            messageView.text = message

            if (options.toasterType == ToasterType.ERROR) {
                iconView.background =
                    AppCompatResources.getDrawable(activity, R.drawable.circle_border_light)
                iconView.setTextColor(activity.getColor(R.color.white))
                messageView.setTextColor(activity.getColor(R.color.white))
                toastClose.setTextColor(activity.getColor(R.color.white))
            }

            // now change the layout of the snackbar
            val snackBarLayout = snackBar.view as SnackbarLayout

            // add the custom snack bar layout to snackbar layout
            snackBarLayout.addView(customSnackView, 0)

            /**
             * TODO
             *  Run with Android Toast ... exclusively in Android 12 (API 33) ....
             *  until "missing text" bug is resolved
             *
             * TODO
             *  temporarily disabling this ... we will resolve this later
             */
            //if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.S) {
            activity.runOnUiThread {
                val toast = Toast.makeText(
                    activity,
                    Formatter.color(message, options.toasterType.color),
                    Toast.LENGTH_LONG
                )
                toast.setGravity(Gravity.TOP, 0, CustomUtils.dpToPx(activity, 42))
                toast.show()
            }
            /*
            } else {
                snackBar.show()
            }

             */
        }
    }
}
