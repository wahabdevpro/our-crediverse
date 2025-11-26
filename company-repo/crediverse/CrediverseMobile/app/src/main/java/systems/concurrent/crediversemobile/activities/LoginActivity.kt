package systems.concurrent.crediversemobile.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.ActivityLoginBinding
import systems.concurrent.crediversemobile.models.AccountInfoResponseModel
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.repositories.MasRepository.ErrorMessages
import systems.concurrent.crediversemobile.repositories.SandboxRepository
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.AccountInfoViewModel
import systems.concurrent.crediversemobile.view_models.LoginViewModel
import systems.concurrent.crediversemobile.view_models.StatisticsViewModel
import systems.concurrent.crediversemobile.view_models.ViewModelUtils
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

private const val MAX_OTP_ATTEMPTS = 1

class LoginActivity : AppCompatActivityWithIdleManager() {
    private val _tag = this::class.java.kotlin.simpleName

    private lateinit var binding: ActivityLoginBinding

    private lateinit var masRepository: MasRepository

    private val loginViewModel by lazy {
        val factory = InjectorUtils.provideLoginFactory(applicationContext)
        ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }
    private val accountInfoViewModel by lazy {
        val factory = InjectorUtils.provideAccountInfoFactory(applicationContext)
        ViewModelProvider(this, factory)[AccountInfoViewModel::class.java]
    }
    private val statisticsViewModel by lazy {
        val factory = InjectorUtils.provideStatisticsFactory(applicationContext)
        ViewModelProvider(this, factory)[StatisticsViewModel::class.java]
    }

    private var otpLayoutShown = false
    private var otpAttempts = 1

    private var imsi: String? = null

    private lateinit var androidPermissionController: AndroidPermissionController
    private var observer: ContentObserver? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_login, menu)
        return true
    }

    @Override
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        return when (item.itemId) {
            R.id.about -> NavigationActivity.showAboutUsDialog(this as Activity)
            R.id.language_en -> updateActivityLocale(LocaleHelper.Language.EN)
            R.id.language_fr -> updateActivityLocale(LocaleHelper.Language.FR)
            else -> true
        }
    }

    private fun updateActivityLocale(language: LocaleHelper.Language): Boolean {
        LocaleHelper.setLanguage(this, language) { languageWasUpdated ->
            if (languageWasUpdated) recreate()
        }
        return false
    }

    private fun setupActionBar() {
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar)

        if (SandboxRepository.isSandboxEnabled) {
            binding.sandboxModeIndicator.visibility = View.VISIBLE
        }

        val title: TextView? = supportActionBar?.customView?.findViewById(R.id.actionbar_title)
        title?.text = getString(R.string.login_title, getString(R.string.app_name))
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun saveImsiIfWeHavePermission() {
        // No need to proceed if SDK >= 30 ---- see this@init
        if (Build.VERSION.SDK_INT >= 30) return

        if (!AndroidPermissionController.isGranted(Manifest.permission.READ_PHONE_STATE)) {
            imsi = "NO_IMSI"
            Log.w(_tag, "NO IMSI PERMISSION GRANTED")
            return
        }

        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        imsi = telephonyManager.subscriberId
        Log.i(_tag, "GRANTED, IMSI: $imsi")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        androidPermissionController.onRequestPermissionsResult(permissions, grantResults)
        saveImsiIfWeHavePermission()
    }

    private fun openWebPage(url: String, closeIfIncompatible: Boolean = false) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        val webPageOpened = if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            true
        } else {
            val chooser = Intent.createChooser(intent, "Open with")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(chooser)
                true
            } else {
                Log.e(_tag, "Could not resolve activity...")
                false
            }
        }

        val closeActivityIfIncompatible = { if (closeIfIncompatible) finish() }

        if (webPageOpened) closeActivityIfIncompatible()
        else {
            /**
             * Copy URL to clipboard
             */
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text_label", url)
            clipboardManager.setPrimaryClip(clipData)

            val urlOpenErrorText = getString(R.string.app_version_android_url_handling_error, url)

            val dialog = Dialog(
                this, DialogType.INFO, "",
                Formatter.fromHtml(urlOpenErrorText),
                DialogOptions().setConfirmBtnTextResource(R.string.close)
            )
            dialog.onConfirm {
                closeActivityIfIncompatible()
            }.show()
        }
    }

    private fun checkForNewAppVersion() {
        loginViewModel.getVersionStatus(this) { result ->
            result.onSuccess { versionStatus ->
                val downloadUrl = versionStatus.updateDownloadUrl ?: ""

                val dialogOptions =
                    if (downloadUrl.isEmpty()) DialogOptions()
                    else {
                        val options = DialogOptions()
                            .setDismissBtnTextResource(R.string.later)
                            .setConfirmBtnTextResource(R.string.download)

                        if (!versionStatus.updateRequired()) options.showConfirmBtn()
                        else options
                    }

                /**
                 * TODO -- i18n
                 */
                if (versionStatus.updateRequired()) {
                    Dialog(
                        this, DialogType.ERROR,
                        "New Version Available",
                        Formatter.combine(
                            "Cannot proceed.",
                            "\n\nYou must update your app version to continue!"
                        ),
                        dialogOptions.setCancellable(false).setAutoDismiss(false)
                        //.setOkBtnTextResource(R.string.download)
                    ).onConfirm {
                        if (downloadUrl.isEmpty()) {
                            Toaster.showCustomToast(this, "Cannot proceed, please update your app")
                        } else {
                            openWebPage(downloadUrl, closeIfIncompatible = true)
                        }
                    }.show()
                } else if (versionStatus.updateAvailable()) {
                    val deprecationString =
                        Formatter.combine("\n", versionStatus.deprecationString(applicationContext))
                    val versionString: CharSequence = Formatter.combine(
                        getString(R.string.newer_version) + "\n",
                        Formatter.bold(versionStatus.versionNameLatest),
                    )
                    val noUrlText = "\n\n" + getString(R.string.version_contact_operator)
                    val withUrlText = Formatter.combine(
                        "\n\n",
                        Formatter.fromHtml(
                            getString(
                                R.string.version_choose_to_install,
                                Formatter.italic(getString(R.string.download))
                            )
                        ),
                    )

                    val message: CharSequence = when {
                        deprecationString.isEmpty() && downloadUrl.isEmpty() -> {
                            Formatter.combine(versionString, noUrlText)
                        }
                        deprecationString.isEmpty() -> {
                            Formatter.combine(versionString, withUrlText)
                        }
                        downloadUrl.isEmpty() -> {
                            Formatter.combine(versionString, deprecationString, noUrlText)
                        }
                        else -> Formatter.combine(versionString, deprecationString, withUrlText)
                    }

                    val dialog = Dialog(
                        this, DialogType.WARN,
                        "New Version Available",
                        message, dialogOptions
                    )

                    if (downloadUrl.isNotEmpty()) {
                        dialog.onConfirm {
                            openWebPage(downloadUrl)
                        }
                    }

                    dialog.show()
                }
            }.onFailure {
                Log.e(_tag, "Unable to check app version... likely a problem with the MAS")
                Log.e(_tag, "App Version Check Failed because: ${it.message}")
                //Dialog(this, DialogType.WARN, "", "Unable to check app version, watch out!").show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler(this))

        /**
         * TODO
         *  Prevent the use of "day/night" mode - our profiles are not configured for this yet
         */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContentView(ActivityLoginBinding.inflate(layoutInflater).apply { binding = this }.root)

        masRepository = MasRepository(applicationContext)

        checkForNewAppVersion()

        val locationPermissionRequest = AndroidPermissionController.Permission(
            Manifest.permission.ACCESS_FINE_LOCATION, required = false,
            R.string.location, R.string.permission_reason_location
        )

        val readSmsPermissionRequest = AndroidPermissionController.Permission(
            Manifest.permission.READ_SMS, required = false,
            R.string.read_sms, R.string.permission_reason_read_sms
        )

        val permissionsToRequest =
            if (FeatureToggle.LoginPage.requestLocationPermission) {
                mutableListOf(locationPermissionRequest, readSmsPermissionRequest)
            } else {
                mutableListOf(readSmsPermissionRequest)
            }

        imsi = "NO IMSI"
        androidPermissionController = AndroidPermissionController(this, permissionsToRequest)
        /**
         *
         */

        androidPermissionController.initialiseAfterActivity()

        androidPermissionController.requestPermissions()
        saveImsiIfWeHavePermission()

        binding.msisdnField.setText(getCachedMsisdn())

        setupActionBar()
        setupListeners()
        loginButtonInputWatchers()

        setupLogo()

        LogoutManager.getLogoutStateLiveData().observe(this@LoginActivity) { logoutState ->
            if (logoutState.isForcedLogout()) {
                val message = getString(logoutState.reason!!)
                Toaster.showCustomToast(this, message)
            }
        }

        if (SandboxRepository.sandboxAutoLoginEnabled()) {
            /**
             * As a sandbox login ... this means the ENTIRE SYSTEM returns sandbox data
             *  I.E. it is not a login that will work for REAL
             *       because real data requires valid tokens which the sandbox cannot (and will not) provide
             *
             *       NOTE ... we are setting OTP field to 1 ...
             *       if sandbox were __disabled__, this `otpAuthentication()` method would fail
             */
            binding.otpField.setText("1")
            otpAuthentication()
        }

        // Hide the keyboard when first landing on the login page
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun setupLogo() {
        if (!AppFlag.LoginPage.logoHasPrimaryBackground && !AppFlag.LoginPage.logoHasSecondaryBackground)
            return

        binding.logoWithBackground.visibility = View.VISIBLE
        binding.logoWithBackgroundImage.background = AppCompatResources.getDrawable(
            applicationContext,
            if (AppFlag.LoginPage.logoHasPrimaryBackground) R.color.cs_primary
            else R.color.cs_secondary
        )
    }

    private fun getCachedMsisdn(): String {
        val msisdn = MasService.getMyMsisdn() ?: ""

        if (msisdn.isNotEmpty()) {
            binding.pinField.isFocusableInTouchMode = true
            binding.pinField.isFocusable = true
            binding.pinField.requestFocus()
        }

        return msisdn
    }

    private fun loginInputFieldsAreFilledIn(): Boolean {
        val hasMsisdn = binding.msisdnField.text?.isNotEmpty()
        val hasPin = binding.pinField.text?.isNotEmpty()
        return hasMsisdn == true && hasPin == true
    }

    private fun loginButtonInputWatchers() {
        // start in a known state
        binding.loginButton.isEnabled = loginInputFieldsAreFilledIn()

        val toggleLoginCallback = object : TextWatcher {
            // these overrides are necessary, though 'before' and 'on' are unused
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            //
            override fun afterTextChanged(s: Editable?) {
                if (otpLayoutShown) {
                    val hasOtp = binding.otpField.text?.isNotEmpty()
                    binding.loginButton.isEnabled = hasOtp == true
                } else {
                    binding.loginButton.isEnabled = loginInputFieldsAreFilledIn()
                }
            }
        }

        binding.msisdnField.addTextChangedListener(toggleLoginCallback)
        binding.pinField.addTextChangedListener(toggleLoginCallback)
        binding.otpField.addTextChangedListener(toggleLoginCallback)
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            if (androidPermissionController.isMissingRequiredPermissions()) {
                androidPermissionController.requestPermissions()
            } else {
                if (!otpLayoutShown) beginAuthentication()
                else {
                    binding.loginButton.isEnabled = false
                    otpAuthentication()
                }
            }
        }
        binding.backButton.setOnClickListener {
            toggleOtpLayout(show = false)
        }
    }

    private fun toggleOtpLayout(show: Boolean) {
        runOnUiThread(Runnable {
            if (show) {
                otpAttempts = 1
                binding.credentialLoginLayout.visibility = View.INVISIBLE
                binding.otpLoginLayout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.INVISIBLE
                binding.loginButton.visibility = View.VISIBLE
                binding.loginButton.isEnabled = false
                binding.otpField.requestFocus()
                otpLayoutShown = true
            } else {
                binding.otpField.setText("")
                binding.credentialLoginLayout.visibility = View.VISIBLE
                binding.otpLoginLayout.visibility = View.INVISIBLE
                binding.loginButton.isEnabled = true
                otpLayoutShown = false
            }
        })
    }

    private fun toggleProgressIndicator(show: Boolean) {
        runOnUiThread(Runnable {
            if (show) {
                binding.loginButton.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.loginButton.visibility = View.VISIBLE
                binding.progressBar.visibility = View.INVISIBLE
            }
        })
    }

    private fun beginAuthentication() {
        toggleProgressIndicator(show = true)

        /**
         * Because of race conditions on "logout" when resetting the "isCached" counter
         *  it's best to clear the counters "again" before the next login...
         */
        ViewModelUtils.resetAllViewModelCaches()

        val msisdn = binding.msisdnField.text.toString()
        val pin = binding.pinField.text.toString()

        val onError: (e: CSException) -> Unit = { e ->
            Log.e(_tag, e.message.toString())
            toggleProgressIndicator(show = false)
            val message = e.getStringFromResourceOrDefault(this@LoginActivity)

            if (e.isAnyOf(ErrorMessages.SYNC_CLIENT_TIME)) {
                Dialog(this@LoginActivity, DialogType.WARN, "", message).show()
            } else {
                Toaster.showCustomToast(
                    this@LoginActivity, message,
                    Toaster.Options().setType(ToasterType.ERROR)
                )
            }
        }

        masRepository.login(msisdn, pin, activity = this) {
            it.onSuccess {
                toggleOtpLayout(show = true)
                try {
                    checkForIncomingSmsAndFillOtp()
                } catch (e: Exception) {
                    Log.e(_tag, "Unable to retrieve OTP from SMS, no action: " + e.message)
                }
            }.onFailure { t ->
                onError(t)
            }
        }
    }

    private fun otpAuthentication() {
        ActivityUtils.hideKeyboard(this, findViewById(android.R.id.content))

        toggleProgressIndicator(show = true)

        val otp = binding.otpField.text.toString()

        masRepository.verifyOtp(otp, activity = this) { verifyOtpResult ->
            runOnUiThread {
                verifyOtpResult.onSuccess {
                    Log.d(_tag, "OTP login successful, AgentMSISDN: ${it.agentMsisdn}")
                    /**
                     * The purpose of making the getAccountInfo request is NOT to retrieve the data, it is to CACHE it
                     * The data will then be made available to the NavigationActivity without needing to "LOAD" further
                     */
                    val accountInfoFuture = getAccountInfoAndSetLanguage()
                    val isTeamLeadFuture = isTeamLead()

                    CompletableFuture.allOf(accountInfoFuture, isTeamLeadFuture).thenRun {
                        runOnUiThread {
                            isTeamLeadFuture.get().onSuccess { isLead ->
                                NavigationManager.setIsTeamLead(isLead)
                            }

                            LogoutManager.setLoggedIn()
                            updateLastTokenRenewalTime()
                            goToNavigationActivity()
                        }
                    }
                }.onFailure {
                    var message = it.getStringFromResourceOrDefault(
                        this@LoginActivity, R.string.unable_to_authenticate
                    )

                    binding.pinField.setText("")
                    toggleProgressIndicator(show = false)
                    if (it.isAnyOf(ErrorMessages.SYNC_CLIENT_TIME)) {
                        toggleOtpLayout(show = false)
                        Dialog(this@LoginActivity, DialogType.WARN, "", message).show()
                        return@onFailure
                    }

                    val exceededOtpMaxAttempt = otpAttempts >= MAX_OTP_ATTEMPTS

                    message = if (
                        it.isAnyOf(ErrorMessages.UNAVAILABLE, ErrorMessages.TIMEOUT)
                    ) {
                        it.getStringFromResourceOrDefault(this@LoginActivity)
                    } else {
                        getString(R.string.otp_verify_failed)
                    }

                    Toaster.showCustomToast(
                        this@LoginActivity, message,
                        Toaster.Options().setType(ToasterType.WARN)
                    )
                    if (exceededOtpMaxAttempt) toggleOtpLayout(show = false)
                    else otpAttempts += 1
                }
            }
        }
    }

    private fun extractOtpFromSmsString(smsBody: String): String? {
        val pattern = Pattern.compile("^\\s*Your One Time PIN is \\d{4,5}\\s*")
        var matcher = pattern.matcher(smsBody)

        if (!matcher.matches()) return null

        val otpPattern = Pattern.compile("(\\d{4,5})")
        matcher = otpPattern.matcher(smsBody)

        if (!matcher.find()) return null

        return matcher.group(0)
    }

    private fun checkForIncomingSmsAndFillOtp(): String? {
        if (!AndroidPermissionController.isGranted(Manifest.permission.READ_SMS)) return null

        var otp: String? = null
        val contentResolver = applicationContext.contentResolver

        // Register a content observer to monitor changes to the SMS content provider
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                val newCursor = contentResolver.query(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    arrayOf(Telephony.Sms.Inbox.BODY),
                    null, null,
                    Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
                )
                newCursor?.use { c ->
                    if (c.moveToFirst()) {
                        val bodyIndex = c.getColumnIndex(Telephony.Sms.Inbox.BODY)
                        val body = c.getString(bodyIndex)
                        otp = extractOtpFromSmsString(body)
                        binding.otpField.setText(otp)
                    } else return@use ""
                }
            }
        }
        contentResolver.unregisterContentObserver(observer!!)
        contentResolver.registerContentObserver(Telephony.Sms.Inbox.CONTENT_URI, true, observer!!)

        return otp
    }

    private fun isTeamLead(): CompletableFuture<CSResult<Boolean>> {
        val isTeamLeadFuture = CompletableFuture<CSResult<Boolean>>()
        statisticsViewModel.isTeamLead(this@LoginActivity) {
            statisticsViewModel.viewModelScope.launch {
                isTeamLeadFuture.complete(it)
            }
        }
        return isTeamLeadFuture
    }

    private fun getAccountInfoAndSetLanguage(): CompletableFuture<CSResult<AccountInfoResponseModel>> {

        val accountInfoFuture = CompletableFuture<CSResult<AccountInfoResponseModel>>()

        accountInfoViewModel.getAccountInfoLiveData { result ->
            /**
             * WE DO NOT HANDLE A FAILURE HERE ....
             *  because the account info is retrieved again when needed from cache or in the case of a failure
             */
            accountInfoViewModel.viewModelScope.launch(Dispatchers.IO) {
                result.onSuccess { accountInfo ->
                    val existingForcedLanguage =
                        LocaleHelper.Language.valueOfOrNull(AppFlag.LoginPage.forcedLanguage)

                    val appLanguage =
                        when (existingForcedLanguage) {
                            null -> LocaleHelper.Language.valueOfOrDefault(
                                accountInfo.language ?: ""
                            )
                            else -> existingForcedLanguage
                        }

                    LocaleHelper.setLanguage(this@LoginActivity, appLanguage, saveToCache = true)
                }
                accountInfoFuture.complete(result)
            }
        }

        return accountInfoFuture
    }

    private fun goToNavigationActivity() {
        startActivity(Intent(applicationContext, NavigationActivity::class.java))
        // completely closes the activity, removing history
        // prevents BACK button from coming back to the Login activity
        finish()
    }

    override fun onDestroy() {
        if (observer != null) {
            applicationContext.contentResolver.unregisterContentObserver(observer!!)
        }
        super.onDestroy()
    }
}
