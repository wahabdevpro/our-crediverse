package systems.concurrent.crediversemobile.services

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.utils.ActivityUtils

class LoginDialogOptions {
    private var _loginBtnTextResource = R.string.login
    private var _cancelBtnTextResource = R.string.cancel
    val loginBtnTextResource get() = _loginBtnTextResource
    val cancelBtnTextResource get() = _cancelBtnTextResource

    private var _autoDismiss = true
    val autoDismiss get() = _autoDismiss

    private var _usernameInputHintResource = R.string.username_hint
    val usernameInputHintResource = _usernameInputHintResource
    private var _passwordInputHintResource = R.string.password_hint
    val passwordInputHintResource = _passwordInputHintResource

    private var _usernameInputType = InputType.TYPE_CLASS_TEXT
    private var _passwordInputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

    fun passwordInputType(inputType: Int): LoginDialogOptions {
        _passwordInputType = inputType
        return this
    }

    fun loginBtnTextResource(resource: Int): LoginDialogOptions {
        _loginBtnTextResource = resource
        return this
    }

    fun cancelBtnTextResource(resource: Int): LoginDialogOptions {
        _cancelBtnTextResource = resource
        return this
    }

    fun autoDismiss(autoDismiss: Boolean): LoginDialogOptions {
        _autoDismiss = autoDismiss
        return this
    }
}

class LoginDialog(
    private val activity: Activity, title: CharSequence,
    private val dialogOptions: LoginDialogOptions = LoginDialogOptions()
) {

    private val dialogBuilder = AlertDialog.Builder(activity)

    private val dialog: AlertDialog

    private val loginButton: MaterialButton
    private val cancelButton: MaterialButton

    private val usernameInput: TextInputEditText
    private val passwordInput: TextInputEditText

    private var dialogView: View

    init {
        val inflater = LayoutInflater.from(activity)
        dialogView = inflater.inflate(R.layout.dialog_login, null)
        dialogBuilder.setView(dialogView)

        dialog = dialogBuilder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_login)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        /**
         * If DialogSize.AUTO .... MAX card width of 350dp
         * If DialogSize.LARGE ... No maximum width
         */
        /*

        // ---- NO NEED FOR AUTO WIDTH ... allow max-width

        val dialogCard = dialogView.findViewById<CardView>(R.id.login_dialog_card)
        val layoutParams = dialogCard.layoutParams as ViewGroup.LayoutParams
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        val maxWidthPx = (350 * activity.resources.displayMetrics.density).toInt()
        dialogCard.layoutParams = layoutParams
        dialogCard.post {
            if (dialogCard.measuredWidth > maxWidthPx) {
                layoutParams.width = maxWidthPx
                dialogCard.layoutParams = layoutParams
            }
        }
        */
        /****************/

        val dialogIconWrapper = dialogView.findViewById<FlexboxLayout>(R.id.icon_wrapper)!!
        val dialogIconNotch = dialogView.findViewById<TextView>(R.id.login_dialog_icon_notch)!!
        val dialogIconTextView = dialogView.findViewById<TextView>(R.id.login_dialog_icon)!!
        val cancelBtnTextView = dialogView.findViewById<FlexboxLayout>(R.id.cancel_btn_wrapper)!!

        /**
         * Set initial icon and it's respective background color
         */
        // set colors and icon according to dialog type
        dialogIconWrapper.background = ContextCompat.getDrawable(activity, DialogType.LOGIN.color)
        dialogIconNotch.background = ContextCompat.getDrawable(activity, DialogType.LOGIN.color)
        dialogIconTextView.text = DialogType.LOGIN.icon.unicode

        /**
         * FIXME --- handle input hints ... etc
         */
        usernameInput = dialogView.findViewById(R.id.username_input)!!
        passwordInput = dialogView.findViewById(R.id.password_input)!!
        usernameInput.hint = activity.getString(dialogOptions.usernameInputHintResource)
        passwordInput.hint = activity.getString(dialogOptions.passwordInputHintResource)

        /**
         * This is a 'confirm' dialog - show cancel button as well as ok
         */
        cancelBtnTextView.visibility = View.VISIBLE

        /**
         * Dialog Title present? Show it
         */
        val dialogTitleTextView = dialogView.findViewById<TextView>(R.id.login_dialog_title)!!
        if (title.isEmpty()) dialogTitleTextView.visibility = View.GONE
        else dialogTitleTextView.text = title

        /**
         * Manage OK and CANCEL buttons
         */
        loginButton = dialogView.findViewById(R.id.login_btn)!!
        cancelButton = dialogView.findViewById(R.id.cancel_btn)!!

        loginButton.text = activity.getString(dialogOptions.loginBtnTextResource)
        cancelButton.text = activity.getString(dialogOptions.cancelBtnTextResource)

        // default in case no handlers are set!
        /**
         * NOTE : We explicitly don't put a default on the 'ok' button
         *        we want NO action if it is not set (i.e. it indicates a problem)
         */
        cancelButton.setOnClickListener { dialog.dismiss() }
    }

    fun show() {
        dialog.show()
    }

    fun showUsernameInputError(error: String) {
        usernameInput.error = error
        usernameInput.requestFocus()
    }

    fun showPasswordInputError(error: String) {
        passwordInput.error = error
        passwordInput.requestFocus()
    }

    fun toggleInputProgress(show: Boolean) {
        val inputs = dialogView.findViewById<FlexboxLayout>(R.id.inputs)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.input_progress)
        activity.runOnUiThread {
            inputs.visibility = when (show) {
                true -> View.INVISIBLE
                false -> View.VISIBLE
            }
            progressBar.visibility = when (show) {
                true -> View.VISIBLE
                false -> View.GONE
            }
        }
    }

    /**
     * Useful for attaching a TOAST that is elevated 'above' the dialog
     */
    fun getDialogRootView(): View? {
        return dialog.window?.decorView?.rootView
    }

    fun onLoginGetCredentials(callback: (uname: String, pword: String) -> Unit): LoginDialog {
        loginButton.setOnClickListener {
            /**
             * callback ( uname , pword )
             */
            // FIXME -- only dismiss if success
            ActivityUtils.hideKeyboard(activity as FragmentActivity, getDialogRootView())
            callback(
                usernameInput.text.toString(),
                passwordInput.text.toString()
            )
        }
        return this
    }

    fun onCancel(onDone: (() -> Unit)? = null): LoginDialog {
        cancelButton.setOnClickListener {
            ActivityUtils.hideKeyboard(activity as FragmentActivity, getDialogRootView())
            if (dialogOptions.autoDismiss) dismiss()
            onDone?.invoke()
        }
        return this
    }

    fun dismiss() {
        dialog.dismiss()
    }
}
