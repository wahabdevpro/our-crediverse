package systems.concurrent.crediversemobile.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.*
import systems.concurrent.crediversemobile.models.AccountInfoResponseModel
import systems.concurrent.crediversemobile.models.AccountLanguage
import systems.concurrent.crediversemobile.models.AccountState
import systems.concurrent.crediversemobile.models.AgentFeedbackModel
import systems.concurrent.crediversemobile.overlays.AgentLocationOverlay
import systems.concurrent.crediversemobile.overlays.NavigationOverlay
import systems.concurrent.crediversemobile.overlays.OverlayInflatorWithBinding
import systems.concurrent.crediversemobile.overlays.TeamMemberLocationOverlay
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.repositories.SandboxRepository
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.viewMyLocationEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.viewProfileEntry
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.nowEpoch
import systems.concurrent.crediversemobile.utils.NavigationManager.Page
import systems.concurrent.crediversemobile.view_models.AccountBalancesViewModel
import systems.concurrent.crediversemobile.view_models.AccountInfoViewModel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private const val NOT_SET = "-"

class NavigationActivity : AppCompatActivityWithIdleManager() {

    private lateinit var binding: ActivityNavigationBinding

    private var _changePinBinding: ChangePinBinding? = null
    private val changePinBinding get() = _changePinBinding

    private val scheduler = Scheduler(Time.InSeconds.FIFTEEN_MINUTES)

    private val contextThemeWrapper by lazy {
        ContextThemeWrapper(this@NavigationActivity, R.style.Theme_CrediverseMobile)
    }

    private val accountInfoOverlay by lazy {
        OverlayInflatorWithBinding<AccountInfoBinding>(
            contextThemeWrapper, binding.frameLayout, R.layout.account_info
        )
    }
    private val agentLocationOverlay by lazy {
        AgentLocationOverlay<MapLayoutBinding>(
            contextThemeWrapper, binding.frameLayout, this@NavigationActivity, savedInstanceState
        )
    }
    private val teamMemberLocationOverlay by lazy {
        TeamMemberLocationOverlay<MapLayoutBinding>(
            contextThemeWrapper, binding.frameLayout, this@NavigationActivity, savedInstanceState
        )
    }

    private val navOverlay by lazy {
        NavigationOverlay(contextThemeWrapper, rootView, applicationContext)
    }

    private val uncaughtExceptionHandler by lazy { UncaughtExceptionHandler(this) }

    private var _updateAccountInfoBinding: UpdateAccountInfoBinding? = null
    private val updateAccountInfoBinding get() = _updateAccountInfoBinding

    private var _feedbackBinding: FeedbackBinding? = null
    private val feedbackBinding get() = _feedbackBinding

    private val accountInfoViewModel by lazy {
        val factory = InjectorUtils.provideAccountInfoFactory(applicationContext)
        ViewModelProvider(this, factory)[AccountInfoViewModel::class.java]
    }
    private val accountBalancesViewModel by lazy {
        val factory = InjectorUtils.provideAccountBalancesFactory(applicationContext)
        ViewModelProvider(this, factory)[AccountBalancesViewModel::class.java]
    }

    private val masRepository by lazy {
        MasRepository(applicationContext)
    }

    private var backPressedTwiceCausesLogout = true
    private var backPressedCountWithNoHistory = 0

    private val coroutineScope: CoroutineScope by lazy { MainScope() }

    private val rootView by lazy { window.decorView.rootView as ViewGroup }

    private var accountInfoFuture = CompletableFuture<CSResult<AccountInfoResponseModel>>()

    private var homePageTitle: String? = null

    private var agentFeedback: AgentFeedbackModel? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        closeAllOverlays()

        val accountInfoReadyAndSuccessful: () -> Boolean = {
            var isReadyAndSuccessful = false
            if (accountInfoFuture.isDone) {
                accountInfoFuture.get().onSuccess { isReadyAndSuccessful = true }
            }
            isReadyAndSuccessful
        }

        val showAccountInfoBusyOrFailedDialog: () -> Unit = {
            val dialogType = when {
                !accountInfoFuture.isDone -> DialogType.WARN
                else -> DialogType.ERROR
            }
            val message = when {
                !accountInfoFuture.isDone -> getString(R.string.get_account_info_busy)
                else -> getString(R.string.get_account_info_failure)
            }
            Dialog(this, dialogType, "", message).show()
        }

        // Handle actionbar menu item clicks
        return when (item.itemId) {
            R.id.about -> showAboutUsDialog(this)
            R.id.feedback -> {
                feedbackBinding?.feedbackMessageInput?.setText("")
                val lengthFilter = InputFilter.LengthFilter(MAX_FEEDBACK_CHARS)
                feedbackBinding?.feedbackMessageInput?.filters = arrayOf(lengthFilter)

                if (accountInfoReadyAndSuccessful()) {
                    feedbackBinding?.agentFeedbackWrapper?.visibility = View.VISIBLE
                } else {
                    showAccountInfoBusyOrFailedDialog()
                }
                true
            }
            R.id.account_info -> {
                if (accountInfoReadyAndSuccessful()) {
                    accountInfoOverlay.openOverlay()
                    AppAnalyticsService.addSuccessfulActionEvent(viewProfileEntry)
                } else showAccountInfoBusyOrFailedDialog()
                true
            }
            R.id.logout -> {
                LogoutManager.resetToNew()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupActionBar() {
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar)
    }

    private fun setActionBarTitle(newTitle: CharSequence) {
        val title: TextView? = supportActionBar?.customView?.findViewById(R.id.actionbar_title)
        title?.text = newTitle
    }

    private var savedInstanceState: Bundle? = null
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        this.savedInstanceState = savedInstanceState
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)

        this.savedInstanceState = savedInstanceState

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SandboxRepository.isSandboxEnabled) {
            binding.sandboxModeIndicator.visibility = View.VISIBLE
        }

        setupActionBar()

        NavigationManager.setNavigationActivity(this)

        drawBottomNavigationIcons()

        navigationMenuClickListener()

        LocationService.updateLocation(applicationContext)

        accountInfoViewModel.getAccountInfo()?.onSuccess {
            homePageTitle = getString(R.string.hello_prefix, "${it.firstName} ${it.surname}")
        }

        // we start on the home page
        NavigationManager.setPage(Page.HOME.setActionBarTitle(homePageTitle), addToHistory = true)

        // navigation observer (herein) should listen only after setting the page and title for the first time
        initializeInflatorsAndObservers()

        // If a logout is triggered anywhere in the app ... we observe the event from here
        LogoutManager.getLogoutStateLiveData()
            .observe(this@NavigationActivity) { logoutState ->
                if (logoutState.isLoggedOut()) {
                    val logoutReason = when {
                        logoutState.isForcedLogoutUpgradeRequired() -> AppAnalyticsService.Event.UPGRADE_REQUIRED
                        logoutState.isForcedLogout() -> AppAnalyticsService.Event.FORCED_LOGOUT
                        else -> null
                    }
                    AppAnalyticsService.addLogoutEvent(logoutReason)
                    flushEventsQueueBlocking()

                    MasRepository.logout()
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                    NavigationManager.clearHistory()
                    // completely closes the activity, removing history
                    // prevents BACK button from coming back to the navigation activity
                    finish()
                }
            }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!NavigationManager.hasHistory()) backPressedCountWithNoHistory += 1

                if (backPressedCountWithNoHistory == 2 && backPressedTwiceCausesLogout) {
                    backPressedCountWithNoHistory = 0
                    LogoutManager.resetToNew()
                    return
                }

                val previousPage = NavigationManager.gotoPreviousPage()
                if (previousPage == null) {
                    Toaster.showCustomToast(
                        this@NavigationActivity,
                        Formatter.fromHtml(getString(R.string.nav_history_limit_reached)),
                    )
                } else backPressedCountWithNoHistory = 0
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        AppAnalyticsService.setWriteCallback { events, onDone ->
            masRepository.writeEvents(events) { onDone(it) }
        }

        val crashReport = uncaughtExceptionHandler.getCrashReportIfExists()
        crashReport?.let {
            AppAnalyticsService.addCrashEvent(it)
            GlobalScope.launch {
                // run this _blocking_ action in the background
                //  *does not need to be blocking for this call*
                flushEventsQueueBlocking()
            }
            uncaughtExceptionHandler.deleteCrashReport()
        }

        scheduler.method {
            GlobalScope.launch { flushEventsQueueBlocking() }
        }
        scheduler.start()
    }

    private var lastAnalyticsWrite = nowEpoch()

    private fun flushEventsQueueBlocking() {
        val future = CompletableFuture<Unit>()
        AppAnalyticsService.submitAnalyticsEvents { future.complete(Unit) }
        try {
            future.get(1000, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            Log.e(
                _tag, "Writing Analytics took too long... timed out. " +
                        "Likely lost some analytics events"
            )
        }

        lastAnalyticsWrite = nowEpoch()
    }

    override fun onStop() {
        scheduler.stop()
        flushEventsQueueBlocking()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        scheduler.start()
        agentLocationOverlay.onResume()
        teamMemberLocationOverlay.onResume()
    }

    override fun onPause() {
        super.onPause()
        agentLocationOverlay.onPause()
        teamMemberLocationOverlay.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        savedInstanceState = null
        coroutineScope.cancel()
        agentLocationOverlay.onDestroy()
        teamMemberLocationOverlay.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        agentLocationOverlay.onLowMemory()
        teamMemberLocationOverlay.onLowMemory()
    }

    private fun hideKeyboard() {
        ActivityUtils.hideKeyboard(this, findViewById(android.R.id.content))
    }

    private fun drawBottomNavigationIcons() {
        // redraw icons for each bottom navigation fragment
        // - because we don't have drawable versions of these :/
        // TODO - extract these Font Awesome icons into DRAWABLE resources
        //        so that we don't have to do this dance
        // NOTE: To be fair, it's not the end of the world doing this dance, just not preferable

        val drawableFont = DrawableFont(applicationContext)

        val allNavMenus = listOf(
            R.id.first_nav, R.id.second_nav, R.id.third_nav, R.id.fourth_nav, R.id.fifth_nav
        )
        val navigationPages = NavigationManager.navigationPages

        // hide non-existent nav menus
        allNavMenus.forEach {
            if (!navigationPages.containsKey(it))
                binding.bottomNavigationView.menu.findItem(it)?.isVisible = false
        }

        navigationPages.entries.forEach {
            binding.bottomNavigationView.menu.findItem(it.key)?.title = getString(it.value.navLabel)
            binding.bottomNavigationView.menu.findItem(it.key)?.icon =
                drawableFont.from(it.value.navIcon)
        }
    }

    data class CustomNav(
        val highlightNavIcon: Boolean, val pageId: NavigationManager.PageID,
        val action: () -> Unit
    )

    private var overlayLoadingBusy = false

    @SuppressLint("ClickableViewAccessibility")
    private fun navigationMenuClickListener() {
        val navigationPages = NavigationManager.navigationPages

        val customNavigationPages = listOf(
            CustomNav(false, NavigationManager.PageID.TEAM_MEMBER_MAP) {
                teamMemberLocationOverlay.openOverlay()
                overlayLoadingBusy = false
            },
            CustomNav(false, NavigationManager.PageID.BUY_WITH_MOBILE_MONEY) {
                overlayLoadingBusy = true
                toggleProgressBar(this, show = true)
                masRepository.getMobileMoneyBalance { mmBalanceResult ->
                    toggleProgressBar(this, show = false)
                    mmBalanceResult
                        .onSuccess { mmBalanceModel ->
                            openBuyCreditDialog(mmBalanceModel.balance.toDoubleOrNull())
                            AppAnalyticsService.addNavigationEvent("${Page.BUY_MOBILE_MONEY}")
                        }.onFailure {
                            runOnUiThread {
                                val finalString =
                                    it.getStringFromResourceOrDefault(applicationContext)

                                Toaster.showCustomToast(
                                    NavigationManager.getActivity(),
                                    finalString,
                                    Toaster.Options()
                                        .setTime(Snackbar.LENGTH_LONG)
                                        .setType(ToasterType.ERROR)
                                )
                            }
                        }
                    overlayLoadingBusy = false
                }
            }
        )

        // Apply custom navigation handlers to respective pages ...
        navigationPages.forEach { (_, overlayData) ->
            overlayData.pages.forEach { page ->
                customNavigationPages.find { it.pageId == page.pageId }?.let {
                    page.setCustomNavigationAction(it.highlightNavIcon, it.action)
                }
            }
        }

        // if the bottom nav button is clicked, trigger a page change
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            if (overlayLoadingBusy) return@setOnItemSelectedListener EventUtil.EVENT_NOT_CONSUMED
            overlayLoadingBusy = true
            ActivityUtils.hideKeyboard(this)

            if (!navigationPages.containsKey(menuItem.itemId)) {
                return@setOnItemSelectedListener EventUtil.EVENT_NOT_CONSUMED
            }

            val overlayData = navigationPages[menuItem.itemId]!!

            val isSinglePageNotSpecial =
                overlayData.specialPage == null && overlayData.pages.size == 1
            val isOnlySpecialPage = overlayData.specialPage != null && overlayData.pages.isEmpty()

            val onlyOnePage = isSinglePageNotSpecial || isOnlySpecialPage

            if (onlyOnePage) {
                val page = when (overlayData.specialPage) {
                    null -> overlayData.pages[0]
                    else -> overlayData.specialPage
                }

                val notACustomPage = customNavigationPages.find { it.pageId == page.pageId } == null

                // stop the 'busy' indicator for all pages that don't handle it themselves (non-custom)
                if (notACustomPage) overlayLoadingBusy = false

                return@setOnItemSelectedListener page.doNavigationAction()
            } else {
                navOverlay.openNavigationOverlay(overlayData)
                overlayLoadingBusy = false
                return@setOnItemSelectedListener EventUtil.EVENT_NOT_CONSUMED
            }
        }
    }

    private fun openBuyCreditDialog(mmBalance: Double?) {
        runOnUiThread {
            val finalBalance =
                if (mmBalance == null) getString(R.string.error_balance)
                else Formatter.formatCustomCurrency(mmBalance)

            val dialog = Dialog(
                NavigationManager.getActivity(),
                DialogType.CONFIRM,
                getString(R.string.buy_credit_from_mm_title),
                Formatter.combine(
                    Formatter.bold(getString(R.string.buy_credit_amount_prefix) + "\n"),
                    Formatter.adjustSizeRelative(finalBalance, 1.6F), "\n\n\n",
                    Formatter.italic(getString(R.string.buy_credit_description))
                ),
                DialogOptions().withInput(true, getString(R.string.buy_credit_amount_hint))
                    .setSize(DialogSize.LARGE).setConfirmBtnTextResource(R.string.buy)
                    .setAutoDismiss(false)
            )

            dialog.onConfirmGetInput { inputText ->
                dialog.getDialogRootView()?.let {
                    ActivityUtils.hideKeyboard(this, it)
                }
                val inputValue = inputText.toDoubleOrNull() ?: 0.0
                if (inputValue <= 0) {
                    dialog.show()
                    Toaster.showCustomToast(
                        NavigationManager.getActivity(),
                        getString(R.string.buy_credit_invalid_amount),
                        Toaster.Options().setType(ToasterType.WARN)
                            .setRootView(dialog.getDialogRootView())
                    )
                    return@onConfirmGetInput
                }
                Log.e(_tag, "Valid numeric value: $inputText")

                dialog.toggleInputProgress(show = true)
                buyAirtimeWithMobileMoney(inputValue,
                    onSuccess = {
                        dialog.dismiss()
                    }, onCompleted = {
                        dialog.toggleInputProgress(show = false)
                    })
            }
            dialog.onDismiss {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun buyAirtimeWithMobileMoney(
        amount: Double, onSuccess: () -> Unit, onCompleted: (() -> Unit)? = null
    ) {
        masRepository.buyAirtimeWithMobileMoney(amount) { result ->
            runOnUiThread {
                result.onSuccess {
                    accountBalancesViewModel.updateBalancesLiveData()

                    val formattedAmount = Formatter.formatCustomCurrency(amount)
                    Toaster.showCustomToast(
                        NavigationManager.getActivity(),
                        Formatter.fromHtml(getString(R.string.buy_credit_success, formattedAmount)),
                        Toaster.Options().setType(ToasterType.SUCCESS)
                    )
                    onSuccess()
                }.onFailure {
                    val finalString = it.getStringFromResourceOrDefault(applicationContext)
                    Dialog(
                        NavigationManager.getActivity(), DialogType.ERROR, "", finalString
                    ).show()
                }
                onCompleted?.invoke()
            }
        }
    }

    private fun observePageNavigation() {
        // If the page changes, then navigate to that fragment
        NavigationManager.getPage().observe(this) {
            if (it == null) return@observe

            // always close overlays when navigating between pages
            closeAllOverlays()

            AppAnalyticsService.addNavigationEvent(it.toString())

            replaceFragment(it)
            NavigationManager.setPageTitle(it.actionBarTitle)
        }

        NavigationManager.getPageTitle().observe(this) { newTitle ->
            setActionBarTitle(newTitle)
        }
    }

    private fun closeAllOverlays() {
        // Custom Overlays
        listOf(accountInfoOverlay, agentLocationOverlay, teamMemberLocationOverlay).forEach {
            it.closeOverlay()
        }

        /**
         * TODO : Convert these to custom overlays like the above
         */
        listOf(
            changePinBinding?.changePinWrapper,
            feedbackBinding?.agentFeedbackWrapper,
            updateAccountInfoBinding?.updateAccountWrapper,
        ).forEach { it?.visibility = View.INVISIBLE }

        hideKeyboard()
    }

    private fun initializeInflatorsAndObservers() {
        observePageNavigation()
        addInflatedLayoutsToActivity()
        observerAccountInfoLiveData()
        setupInflatedLayouts()
    }

    private fun addInflatedLayoutsToActivity() {
        val parent = binding.frameLayout

        // Inflate Update Account Info layout
        val updateAccountLayout =
            LayoutInflater.from(contextThemeWrapper)
                .inflate(R.layout.update_account_info, null, false)
        _updateAccountInfoBinding = DataBindingUtil.bind(updateAccountLayout)

        // Inflate Change PIN layout
        val changePinLayout =
            LayoutInflater.from(contextThemeWrapper).inflate(R.layout.change_pin, null, false)
        _changePinBinding = DataBindingUtil.bind(changePinLayout)


        // Inflate Feedback layout
        val feedbackLayout =
            LayoutInflater.from(contextThemeWrapper).inflate(R.layout.feedback, null)
        _feedbackBinding = DataBindingUtil.bind(feedbackLayout)

        // Add the inflated layouts to the parent view
        parent.addView(updateAccountLayout)
        parent.addView(changePinLayout)
        parent.addView(feedbackLayout)

        listOf(
            changePinBinding?.changePinClose,
            updateAccountInfoBinding?.updateAccountClose,
            feedbackBinding?.feedbackClose,
        ).forEach { it?.setOnClickListener { closeAllOverlays() } }

        val drawableFont = DrawableFont(this)
        accountInfoOverlay.binding.changePinButton.icon = drawableFont.from(Icon.FINGERPRINT)
        accountInfoOverlay.binding.updateAgentButton.icon = drawableFont.from(Icon.USER_PEN)
        accountInfoOverlay.binding.mapLocatorIcon.icon = drawableFont.from(Icon.MAP_LOCATION_DOT)

        if (!FeatureToggle.AgentProfile.showsMyLocationButton) {
            accountInfoOverlay.binding.mapLocatorIcon.visibility = View.GONE
        }
        if (!FeatureToggle.AgentProfile.showsUpdateMyProfileButton) {
            accountInfoOverlay.binding.updateAgentButton.visibility = View.GONE
        }
        if (!FeatureToggle.AgentProfile.showsChangeMyPinButton) {
            accountInfoOverlay.binding.changePinButton.visibility = View.GONE
        }
    }

    private fun observerAccountInfoLiveData() {
        accountInfoViewModel.getAccountInfoLiveData().observe(this) { accountInfoResult ->
            accountInfoResult
                .onSuccess { accountInfo ->
                    agentFeedback = AgentFeedbackModel(
                        accountInfo.firstName,
                        accountInfo.accountNumber.toString(),
                        accountInfo.tierName,
                        feedBackRequestMsg = "" // starts empty - checked prior to submitting feedback
                    )

                    updateAccountInfoBinding?.emailInput?.setText(accountInfo.email ?: "")
                    updateAccountInfoBinding?.firstNameInput?.setText(accountInfo.firstName)
                    updateAccountInfoBinding?.lastNameInput?.setText(accountInfo.surname ?: "")

                    updateAccountInfoBinding?.accountNumberText?.text =
                        getString(R.string.account_number, accountInfo.accountNumber)

                    changePinBinding?.accountNumberText?.text =
                        getString(R.string.account_number, accountInfo.accountNumber)

                    accountInfoOverlay.binding.accountNumberText.text =
                        getString(R.string.account_number, accountInfo.accountNumber)

                    val fullName =
                        "${accountInfo.title} ${accountInfo.firstName} ${accountInfo.surname}"

                    accountInfoOverlay.binding.accountName.text = fullName
                    accountInfoOverlay.binding.accountEmail.text = accountInfo.email ?: NOT_SET

                    val language = getString(
                        AccountLanguage.getResourceOrUnknown(
                            accountInfo.language ?: AccountLanguage.UN.toString()
                        )
                    )
                    accountInfoOverlay.binding.accountLanguage.text = language
                    val accountState = accountInfo.accountState
                    accountInfoOverlay.binding.accountState.text =
                        getString(accountState.resource)
                    if (accountState == AccountState.ACTIVE) accountInfoOverlay.binding.accountState.setTextColor(
                        getColor(R.color.success)
                    )
                    else accountInfoOverlay.binding.accountState.setTextColor(getColor(R.color.danger))

                    accountInfoOverlay.binding.accountTier.text = accountInfo.tierName

                    accountInfoOverlay.binding.accountInfoProgressBar.visibility = View.GONE
                }.onFailure {
                    Log.w(_tag, "Get Account Info failed ... ${it.message.toString()}")
                }
            accountInfoFuture.complete(accountInfoResult)
        }
    }

    private fun setupInflatedLayouts() {
        changePinInputWatcher()
        updateAccountSetupAndWatchers()

        feedbackBinding?.feedbackSubmitButton?.setOnClickListener {
            agentFeedback()
        }

        accountInfoOverlay.binding.changePinButton.setOnClickListener {
            closeAllOverlays()
            changePinBinding?.changePinWrapper?.visibility = View.VISIBLE
        }
        accountInfoOverlay.binding.updateAgentButton.setOnClickListener {
            closeAllOverlays()
            if (FeatureToggle.AgentProfile.canChangeProfileNameFields) {
                updateAccountInfoBinding?.profileNameFields?.visibility = View.VISIBLE
            } else {
                updateAccountInfoBinding?.profileNameFields?.visibility = View.GONE
            }
            updateAccountInfoBinding?.updateAccountWrapper?.visibility = View.VISIBLE
        }
        accountInfoOverlay.binding.mapLocatorIcon.setOnClickListener {
            closeAllOverlays()
            agentLocationOverlay.openOverlay()
            AppAnalyticsService.addSuccessfulActionEvent(viewMyLocationEntry)
        }
    }

    private fun areChangePinFieldsFilledIn(binding: ChangePinBinding): Boolean {
        val hasOldPin = binding.oldPin.text?.isNotEmpty()
        val hasNewPin = binding.newPin.text?.isNotEmpty()
        return hasOldPin == true && hasNewPin == true
    }

    private fun changePinInputWatcher() {
        if (changePinBinding == null) return
        val binding = changePinBinding!!
        // start in a known state
        binding.changePinButton.isEnabled = areChangePinFieldsFilledIn(binding)

        val toggleLoginCallback = object : TextWatcher {
            // these overrides are necessary, though 'before' and 'on' are unused
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            //
            override fun afterTextChanged(s: Editable?) {
                binding.changePinButton.isEnabled =
                    areChangePinFieldsFilledIn(binding) == true
            }
        }

        binding.changePinButton.setOnClickListener { _ ->
            hideKeyboard()
            if (!areChangePinFieldsFilledIn(binding)) return@setOnClickListener

            val oldPin = binding.oldPin.text.toString().trim()
            val newPin = binding.newPin.text.toString().trim()

            val tooShort: (String) -> Boolean = { str -> str.length < PIN_MIN_LENGTH }
            val tooLong: (String) -> Boolean = { str -> str.length > PIN_MAX_LENGTH }
            val notANumber: (String) -> Boolean = { str -> !str.matches(Regex("^[0-9]+")) }

            if (oldPin == newPin) {
                /**
                 * FIXME - i18n
                 */
                Toaster.showCustomToast(
                    this, "Both pins the same, please change the new pin",
                    Toaster.Options().setType(ToasterType.WARN)
                )

                return@setOnClickListener
            }

            if (tooLong(newPin) || tooShort(newPin) || notANumber(newPin)) {
                /**
                 * FIXME - i18n
                 */
                Toaster.showCustomToast(
                    this, "New pin should be between 3 and 5 numbers",
                    Toaster.Options().setType(ToasterType.WARN)
                )

                return@setOnClickListener
            }

            toggleProgressBar(this, show = true)

            masRepository.changePin(oldPin, newPin) { result ->
                runOnUiThread {
                    result.onSuccess {

                        Toaster.showCustomToast(
                            this, getString(R.string.change_pin_success),
                            Toaster.Options().setType(ToasterType.SUCCESS)
                        )
                        closeAllOverlays()
                        binding.oldPin.setText("")
                        binding.newPin.setText("")
                    }.onFailure {
                        val finalString = it.getStringFromResourceOrDefault(applicationContext)
                        Dialog(this, DialogType.ERROR, "", finalString).show()
                        //Toaster.showCustomToast(this, finalString, ToasterOptions(ToastType.ERROR))
                    }
                    toggleProgressBar(this, show = false)
                }
            }
        }

        binding.oldPin.addTextChangedListener(toggleLoginCallback)
        binding.newPin.addTextChangedListener(toggleLoginCallback)
    }

    /**
     * FIXME -- probably a terrible way of doing it, should find out if Crediverse supports saving FRENCH titles?
     *
     * We need to convert it to english when STORING in crediverse ....
     * so here we map the English to the translated text
     * once the agent has "chosen" their title ...
     * we convert to english when Saving...
     */
    private val titleOptions
        get() = mapOf(
            "Mr" to R.string.title_mr, "Mrs" to R.string.title_mrs, "Miss" to R.string.title_miss
        )

    private fun titleOptionsIndexOf(matchingTitle: String): String? {
        return titleOptions.entries.find { getString(it.value).lowercase() == matchingTitle.lowercase() }?.key
    }

    private fun setTitleOfUpdateAccountInfoOverlay() {
        val result = accountInfoViewModel.getAccountInfo()

        result?.onSuccess { accountInfo ->
            runOnUiThread {
                var indexOfMatchingTitle =
                    titleOptions.values.indexOfFirst { getString(it).lowercase() == accountInfo.title?.lowercase() }

                if (indexOfMatchingTitle == -1) indexOfMatchingTitle = 0

                updateAccountInfoBinding?.titleSpinnerInput?.setSelection(indexOfMatchingTitle)
            }
        }
    }

    private fun updateAccountSetupAndWatchers() {
        if (updateAccountInfoBinding == null) return
        val binding = updateAccountInfoBinding!!

        /**
         * Language and Title "spinner" bindings
         */
        val languageOptions = LocaleHelper.Language.toList()
        val languageAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, languageOptions.map { getString(it) }
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinnerInput.adapter = languageAdapter
        binding.languageSpinnerInput.setSelection(
            LocaleHelper.Language.currentLanguageIndex(
                applicationContext
            )
        )

        val titleAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, titleOptions.values.map { getString(it) }
        )
        titleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.titleSpinnerInput.adapter = titleAdapter

        setTitleOfUpdateAccountInfoOverlay()

        /**
         *  Limit FirstName and LastName to 30 characters
         */
        val maxLength = 30
        val inputFilter = InputFilter.LengthFilter(maxLength)
        binding.firstNameInput.filters = arrayOf(inputFilter)
        binding.lastNameInput.filters = arrayOf(inputFilter)

        /**
         *  **************************
         */

        val showFailure: (csException: CSException) -> Unit = { csException ->
            runOnUiThread {
                val finalString = csException.getStringFromResourceOrDefault(applicationContext)
                Toaster.showCustomToast(
                    this@NavigationActivity, finalString,
                    Toaster.Options().setType(ToasterType.ERROR)
                )
            }
        }

        binding.updateInfoButton.setOnClickListener { _ ->
            hideKeyboard()
            val result = accountInfoViewModel.getAccountInfo()
            result
                ?.onFailure { showFailure(it) }
                ?.onSuccess { accountInfo ->
                    runOnUiThread {
                        val newTitle =
                            titleOptionsIndexOf(binding.titleSpinnerInput.selectedItem.toString())
                                ?: accountInfo.title ?: ""

                        val chosenLanguage =
                            LocaleHelper.Language.valueFromIndexOrDefault(binding.languageSpinnerInput.selectedItemId.toInt())

                        val email = binding.emailInput.text.toString().lowercase().trim()

                        val emailPattern = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$"
                        val isEmailValid = email.matches(emailPattern.toRegex())

                        if (!isEmailValid) {
                            Toaster.showCustomToast(
                                this, getString(R.string.error_invalid_email),
                                Toaster.Options().setType(ToasterType.ERROR)
                            )
                            return@runOnUiThread
                        }

                        accountInfo.title = newTitle
                        accountInfo.firstName = binding.firstNameInput.text.toString()
                        accountInfo.surname = binding.lastNameInput.text.toString()
                        accountInfo.email = email
                        accountInfo.language = chosenLanguage.localeCode.uppercase()

                        binding.progressBar.visibility = View.VISIBLE

                        accountInfoViewModel.updateAccountInfo(accountInfo) { result ->
                            result.onSuccess {
                                accountInfoViewModel.viewModelScope.launch {
                                    Toaster.showCustomToast(
                                        this@NavigationActivity,
                                        getString(R.string.update_profile_success),
                                        Toaster.Options().setType(ToasterType.SUCCESS)
                                    )

                                    val lastLanguage = LocaleHelper.getCurrentLanguage(
                                        applicationContext
                                    )

                                    if (chosenLanguage != lastLanguage) {
                                        LocaleHelper.setLanguage(
                                            this@NavigationActivity, chosenLanguage,
                                            saveToCache = true
                                        )
                                        NavigationManager.setPage(Page.HOME)
                                        recreate() // automatically closes all overlays
                                    } else {
                                        closeAllOverlays()
                                        binding.progressBar.visibility = View.GONE
                                    }
                                }
                            }.onFailure { showFailure(it) }
                        }
                    }
                }
        }
    }

    private fun agentFeedback() {
        agentFeedback?.feedBackRequestMsg = feedbackBinding?.feedbackMessageInput?.text.toString()

        Log.e(_tag, "Feedback message: ${agentFeedback?.feedBackRequestMsg}")
        if (agentFeedback?.feedBackRequestMsg?.isBlank() == true) {
            Dialog(this, DialogType.WARN, "", getString(R.string.invalid_feedback_message)).show()
            return
        }

        hideKeyboard()
        toggleProgressBar(this, show = true)

        masRepository.agentFeedback(agentFeedback!!) { result ->
            runOnUiThread {
                result.onSuccess {
                    Dialog(
                        this, DialogType.SUCCESS, getString(R.string.feedback_success_title),
                        getString(R.string.feedback_success_text)
                    ).show()
                    feedbackBinding?.agentFeedbackWrapper?.visibility = View.GONE
                    feedbackBinding?.feedbackMessageInput?.setText("")
                }.onFailure {
                    val finalString = it.getStringFromResourceOrDefault(applicationContext)

                    val errorDialog = Dialog(
                        this, DialogType.ERROR,
                        getString(R.string.feedback_failure_title), finalString
                    )
                    errorDialog.show()
                }
                toggleProgressBar(this, show = false)
            }
        }
    }

    private fun replaceFragment(page: Page) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        page.fragment?.let { fragmentTransaction.replace(R.id.frame_layout, it) }
        fragmentTransaction.commit()

        // make sure the associated icon is made 'active'
        //  this change wont happen automatically if we navigate programmatically
        binding.bottomNavigationView.menu.findItem(page.fragmentId)?.isChecked = true
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        const val MAX_FEEDBACK_CHARS: Int = 600
        private const val PIN_MIN_LENGTH = 3
        private const val PIN_MAX_LENGTH = 5

        fun toggleProgressBar(
            activity: NavigationActivity, show: Boolean,
            progressTextTop: CharSequence? = null, progressTextBottom: CharSequence? = null,
            animate: Boolean = true
        ) {
            activity.runOnUiThread {
                val binding = activity.binding
                val progress = binding.rootProgress
                val progressTextFieldBottom = binding.bottomProgressText
                val progressTextFieldTop = binding.topProgressText

                val showIfNotShown = show && progress.visibility != View.VISIBLE
                val hideIfNotHidden = !show && progress.visibility == View.VISIBLE

                if (showIfNotShown) {
                    if (animate) {
                        val animIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
                        progress.startAnimation(animIn)
                    }
                    progress.visibility = View.VISIBLE

                    if (progressTextTop?.isNotEmpty() == true) {
                        progressTextFieldTop.visibility = View.VISIBLE
                        progressTextFieldTop.text = progressTextTop
                    }

                    if (progressTextBottom?.isNotEmpty() == true) {
                        progressTextFieldBottom.text = progressTextBottom
                    }

                } else if (hideIfNotHidden) {
                    if (animate) {
                        val animOut = AnimationUtils.loadAnimation(activity, R.anim.fade_out)
                        progress.startAnimation(animOut)
                    }
                    progress.visibility = View.GONE

                    // reset to default busy string
                    progressTextFieldBottom.text = activity.getString(R.string.busy)
                    progressTextFieldTop.visibility = View.GONE

                }
            }
        }

        fun showAboutUsDialog(activity: Activity): Boolean {
            val versionString = Formatter.fromHtml(
                activity.getString(R.string.version_string, AppFlag.System.versionName)
            )

            var buildNumber = AppFlag.System.buildNumber

            val buildString = if (buildNumber.isNotEmpty() || SandboxRepository.isSandboxEnabled) {
                if (buildNumber.isEmpty()) {
                    buildNumber = CustomUtils.getUuid().split("")
                        .reduce { acc, s -> acc + ("-".takeIf { acc.length == 4 } ?: "") + s }
                }

                Formatter.combine(
                    "\n\n", Formatter.fromHtml(
                        activity.getString(R.string.build_string, buildNumber)
                    )
                )
            } else ""

            Dialog(
                activity, DialogType.INFO,
                activity.getString(R.string.about_us),
                Formatter.combine(versionString, buildString),
                DialogOptions().setConfirmBtnTextResource(R.string.close)
            ).show()

            return true
        }

        fun renewJwtToken(callback: (CSResult<Unit>) -> Unit) {
            val activity = NavigationManager.getActivity()
            activity.masRepository.updateLoginToken { callback(it) }
        }
    }
}
