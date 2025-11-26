package systems.concurrent.crediversemobile.services

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.utils.ColorContrastController
import systems.concurrent.crediversemobile.utils.Icon

enum class DialogType(private val _color: Int, private val _icon: Icon) {
    LOGIN(R.color.cs_secondary, Icon.ARROW_RIGHT_TO_BRACKET),
    CONFIRM(R.color.cs_secondary, Icon.QUESTION),
    SUCCESS(R.color.success, Icon.CHECK_DOUBLE),
    ERROR(R.color.danger, Icon.X_MARK),
    WARN(R.color.warn, Icon.EXCLAMATION),
    INFO(R.color.info, Icon.INFO);

    val color get() = this._color
    val icon get() = this._icon
}

enum class DialogSize { AUTO, LARGE }

class DialogOptions {
    private var _confirmBtnTextResource = R.string.c_ok
    private var _dismissBtnTextResource = R.string.cancel
    private var _showConfirmBtn = false
    val confirmBtnTextResource get() = _confirmBtnTextResource
    val dismissBtnTextResource get() = _dismissBtnTextResource
    val shouldShowConfirmBtn get() = _showConfirmBtn

    private var _dialogSize = DialogSize.AUTO
    private var _cancellable = true
    private var _autoDismiss = true
    val dialogSize get() = _dialogSize
    val cancellable get() = _cancellable
    val autoDismiss get() = _autoDismiss

    private var _withInput = false
    private var _inputHint = "input here" // TODO -- i18n
    private var _inputType = InputType.TYPE_CLASS_NUMBER
    private var _inputDefaultText = ""
    private var _dialogHint = ""

    val hasInput get() = _withInput
    val inputHint get() = _inputHint
    val inputType get() = _inputType
    val inputDefaultText get() = _inputDefaultText
    val dialogHint get() = _dialogHint

    fun showConfirmBtn(): DialogOptions {
        _showConfirmBtn = true
        return this
    }

    fun setConfirmBtnTextResource(resource: Int): DialogOptions {
        _confirmBtnTextResource = resource
        return this
    }

    fun setDismissBtnTextResource(resource: Int): DialogOptions {
        _dismissBtnTextResource = resource
        return this
    }

    fun setSize(size: DialogSize): DialogOptions {
        _dialogSize = size
        return this
    }

    fun setCancellable(cancellable: Boolean): DialogOptions {
        _cancellable = cancellable
        return this
    }

    fun setAutoDismiss(autoDismiss: Boolean): DialogOptions {
        _autoDismiss = autoDismiss
        return this
    }

    fun withInput(
        showInput: Boolean, hint: String, defaultValue: String? = null, type: Int? = null
    ): DialogOptions {
        _withInput = showInput
        _inputHint = hint
        _inputDefaultText = defaultValue ?: _inputDefaultText
        _inputType = type ?: _inputType
        return this
    }

    fun setDialogHint(dialogHint: String): DialogOptions {
        _dialogHint = dialogHint
        return this
    }
}

class Dialog(
    private val activity: Activity, dialogType: DialogType,
    title: CharSequence, message: CharSequence,
    private val dialogOptions: DialogOptions = DialogOptions()
) {

    private val dialogBuilder = AlertDialog.Builder(activity)

    private val dialog: AlertDialog

    private val confirmButton: MaterialButton
    private val dismissButton: MaterialButton

    private val textInputEditText: TextInputEditText

    private var dialogView: View

    init {
        val inflater = LayoutInflater.from(activity)
        dialogView = inflater.inflate(R.layout.dialog, null)
        dialogBuilder.setView(dialogView)

        dialog = dialogBuilder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(dialogOptions.cancellable)
        dialog.setContentView(R.layout.dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        /**
         * If DialogSize.AUTO .... MAX card width of 350dp
         * If DialogSize.LARGE ... No maximum width
         *
         */
        val dialogCard = dialogView.findViewById<CardView>(R.id.dialog_card)
        if (dialogOptions.dialogSize == DialogSize.AUTO) {
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
        }
        /****************/

        val dialogIconWrapper = dialogView.findViewById<FlexboxLayout>(R.id.icon_wrapper)!!
        val dialogIconNotch = dialogView.findViewById<TextView>(R.id.dialog_icon_notch)!!
        val dialogIconTextView = dialogView.findViewById<TextView>(R.id.dialog_icon)!!
        val dismissBtnTextView = dialogView.findViewById<FlexboxLayout>(R.id.dismiss_btn_wrapper)!!

        /**
         * Expects Input?
         */
        textInputEditText = dialogView.findViewById(R.id.input)!!
        textInputEditText.inputType = dialogOptions.inputType
        if (dialogOptions.hasInput) {
            textInputEditText.visibility = View.VISIBLE
            textInputEditText.hint = dialogOptions.inputHint
            textInputEditText.setText(dialogOptions.inputDefaultText)
        }

        /**
         * Dialog hint exists? Let's show it
         */
        if (dialogOptions.dialogHint.isNotEmpty()) {
            val view = dialogView.findViewById<TextView>(R.id.dialog_hint)
            view.text = dialogOptions.dialogHint
            view.visibility = View.VISIBLE
        }

        /**
         * Hide the icon block?
         */
        dialogIconWrapper.visibility = View.VISIBLE

        /**
         * Set initial icon and it's respective background color
         */
        // set colors and icon according to dialog type
        dialogIconWrapper.background = ContextCompat.getDrawable(activity, dialogType.color)
        dialogIconNotch.background = ContextCompat.getDrawable(activity, dialogType.color)
        dialogIconTextView.text = dialogType.icon.unicode

        /**
         * Fix color contrast problems
         *  when PRIMARY/SECONDARY colors do not provide sufficient contrast for users
         */
        val contrastController = ColorContrastController(activity.applicationContext)

        val textColor = contrastController.getTextColorResource(dialogType.color)
        if (textColor == contrastController.lightTextColor) {
            dialogIconTextView.background =
                ContextCompat.getDrawable(activity, R.drawable.circle_border_light)
        } else {
            dialogIconTextView.background =
                ContextCompat.getDrawable(activity, R.drawable.circle_border_dark)
        }
        dialogIconTextView.setTextColor(textColor)

        /**
         * Is this a confirm dialog? show both dismiss and confirm actions
         */
        dismissBtnTextView.visibility =
            if (dialogType == DialogType.CONFIRM || dialogOptions.shouldShowConfirmBtn) View.VISIBLE else View.GONE

        /**
         * Dialog Title present? Show it
         */
        val dialogTitleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)!!
        if (title.isEmpty()) dialogTitleTextView.visibility = View.GONE
        else dialogTitleTextView.text = title

        /**
         * Main Dialog Message
         */
        dialogView.findViewById<TextView>(R.id.dialog_message)?.text = message

        /**
         * Handle CONFIRM and DISMISS buttons
         */
        confirmButton = dialogView.findViewById(R.id.confirm_btn)!!
        dismissButton = dialogView.findViewById(R.id.dismiss_btn)!!

        confirmButton.text = activity.getString(dialogOptions.confirmBtnTextResource)
        dismissButton.text = activity.getString(dialogOptions.dismissBtnTextResource)

        // defaults in case no handlers are set!
        confirmButton.setOnClickListener {
            dialog.dismiss()
        }
        dismissButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun show() {
        dialog.show()
    }

    fun toggleProgress(show: Boolean) {
        val contentWrapper = dialogView.findViewById<FlexboxLayout>(R.id.dialog_content_wrapper)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress)
        activity.runOnUiThread {
            when (show) {
                true -> {
                    contentWrapper.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                }
                false -> {
                    contentWrapper.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    fun toggleInputProgress(show: Boolean) {
        val progressBar = dialogView.findViewById<FlexboxLayout>(R.id.input_progress)
        activity.runOnUiThread {
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

    fun onConfirmGetInput(callback: (String) -> Unit): Dialog {
        confirmButton.setOnClickListener {
            if (dialogOptions.autoDismiss) dismiss()
            callback(textInputEditText.text.toString())
        }
        return this
    }

    fun onConfirm(callback: () -> Unit): Dialog {
        confirmButton.setOnClickListener {
            if (dialogOptions.autoDismiss) dismiss()
            callback()
        }
        return this
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun onDismiss(callback: () -> Unit): Dialog {
        dismissButton.setOnClickListener {
            if (dialogOptions.autoDismiss) dismiss()
            callback()
        }
        return this
    }
}
