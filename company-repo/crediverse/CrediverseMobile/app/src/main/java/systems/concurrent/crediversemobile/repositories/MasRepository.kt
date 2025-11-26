package systems.concurrent.crediversemobile.repositories

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import io.grpc.Metadata
import io.grpc.Status.Code
import io.grpc.Status.FAILED_PRECONDITION
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.*
import kotlinx.coroutines.awaitAll
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.LoginActivity
import systems.concurrent.crediversemobile.models.*
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.airtimeSaleEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.changePinEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.editProfileEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.getBalancesEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.getHistoryPageEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.loginEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.submitFeedbackEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.submitOtpEntry
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.transferEntry
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.kotlinRandom
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.metadataKeyOf
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.nowEpoch
import systems.concurrent.crediversemobile.utils.Formatter
import systems.concurrent.crediversemobile.view_models.ViewModelUtils
import java.time.*
import java.time.temporal.TemporalAdjusters.nextOrSame
import java.time.temporal.TemporalAdjusters.previousOrSame
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException

class MasRepository(val context: Context) {
    private val _tag = this::class.java.kotlin.simpleName

    private val sandboxRepository = SandboxRepository()

    private val cacheService = CacheService(context)

    enum class SalesSummaryPeriod { DAILY, WEEKLY, MONTHLY }

    class SalesSummaryIntervalAge(private var _intervalAge: Byte) {
        val age get() = _intervalAge
    }

    enum class ErrorMessages(private val _resource: Int) {
        /* GENERIC ERRORS */
        INVALID_AMOUNT(R.string.error_invalid_amount),
        INVALID_CHANNEL(R.string.error_invalid_channel),
        TRANSFER_INVALID_CHANNEL(R.string.transfer_error_invalid_channel),
        NO_TRANSFER_RULE(R.string.transfer_error_no_rule),
        INVALID_RECIPIENT(R.string.error_invalid_recipient),
        INSUFFICIENT_FUNDS(R.string.error_insufficient_funds),
        REFILL_FAILED(R.string.error_refill_failed),
        NOT_SELF(R.string.error_not_self),

        /*
        ALREADY_ADJUDICATED(0),
        ALREADY_REGISTERED(0),
        ALREADY_REVERSED(0),
        BUNDLE_SALE_FAILED(0),
        CO_AUTHORIZE(0),
        CO_SIGN_ONLY_SESSION(0),
        DAY_AMOUNT_LIMIT(0),
        DAY_COUNT_LIMIT(0),
        HISTORIC_PASSWORD(0),
        IMSI_LOCKOUT(0),
        INSUFFICIENT_PROVISN(0),
        INTRATIER_TRANSFER(0),
        INVALID_AGENT(0),
        INVALID_BUNDLE(0),
        INVALID_PASSWORD(0),
        INVALID_PIN(0),
        INVALID_STATE(0),
        INVALID_TRAN_TYPE(0),
        INVALID_VALUE(0),
        MAX_AMOUNT_LIMIT(0),
        MONTH_AMOUNT_LIMIT(0),
        MONTH_COUNT_LIMIT(0),
        NOT_ELIGIBLE(0),
        NOT_REGISTERED(0),
        NOT_WEBUSER_SESSION(0),
        NO_IMSI(0),
        NO_LOCATION(0),
        OTHER_ERROR(0),
        PASSWORD_LOCKOUT(0),
        PIN_LOCKOUT(0),
        REFILL_BARRED(0),
        REFILL_DENIED(0),
        REFILL_NOT_ACCEPTED(0),
        SESSION_EXPIRED(0),
        TECHNICAL_PROBLEM(0),
        TEMPORARY_BLOCKED(0),
        TIMED_OUT(0),
        TOO_LARGE(0),
        TOO_LONG(0),
        TOO_SHORT(0),
        TOO_SMALL(0),
        TX_NOT_FOUND(0),
        WRONG_LOCATION(0),
        */

        /* TEAM SERVICE errors */
        GET_TEAM_FAILED(R.string.get_team_failed),
        TEAM_NOT_FOUND(R.string.team_not_found),
        GET_MEMBERSHIP_FAILED(R.string.get_membership_failed),
        GET_MEMBERSHIP_NOT_FOUND(R.string.get_membership_not_found),

        /* MOBILE MONEY errors */
        DEPOSIT_FAILED(R.string.mm_deposit_error),
        WITHDRAW_FAILED(R.string.mm_withdraw_error),
        MM_UNAUTHORIZED(R.string.unable_to_authenticate),
        GET_MM_BALANCE_FAILED(R.string.buy_credit_get_balance_error),

        /* General/Auth Errors */
        TIMEOUT(R.string.connection_timeout),
        UNAVAILABLE(R.string.unable_to_connect),
        UPGRADE_REQUIRED(R.string.logout_upgrade_required),
        AUTH(R.string.unable_to_authenticate),
        FORBIDDEN(R.string.auth_forbidden),
        PERMISSION_DENIED(R.string.auth_permission_denied),
        UNAUTHORIZED(R.string.auth_forbidden),
        INTERNAL_SERVER_ERROR(R.string.internal_server_error),
        GET_VERSION_ERROR(R.string.get_version_error),

        /* Specific errors */
        NO_HOURLY_SALES_DATA(R.string.no_hourly_sales_data),
        SET_TARGET_FAILED(R.string.sales_target_update_failed),
        GET_ACCOUNT_INFO_FAILURE(R.string.get_account_info_failure),
        INVALID_OLD_PIN(R.string.invalid_old_pin),
        CHANGE_PIN_FAILED(R.string.change_pin_failure),
        SYNC_CLIENT_TIME(R.string.invalid_time);

        val resource get() = _resource

        companion object {
            fun getResourceOrDefault(
                name: String, defaultResource: Int = INTERNAL_SERVER_ERROR.resource
            ): Int {
                return values().firstOrNull { it.name == name }?.resource ?: defaultResource
            }

            fun getResourceOrDefault(
                t: Throwable, defaultResource: Int = INTERNAL_SERVER_ERROR.resource
            ): Int {
                return values().firstOrNull { it.name == t.message.toString() }?.resource
                    ?: defaultResource
            }

            fun throwableMatchesError(t: Throwable, error: ErrorMessages): Boolean {
                return stringMatchesError(t.message.toString(), error)
            }

            private fun stringMatchesError(string: String, error: ErrorMessages): Boolean {
                return string.lowercase().contains(error.toString().lowercase())
            }

            fun errorMessageExistsInStringOrNull(string: String): ErrorMessages? {
                return values().firstOrNull {
                    string.lowercase().contains(it.toString().lowercase())
                }
            }
        }
    }

    private fun extractOriginalException(exception: Throwable): Throwable {
        var currentException = exception
        while (currentException is CompletionException || currentException is ExecutionException) {
            currentException = currentException.cause ?: break
        }
        return currentException
    }

    private fun handleSpecialResultOrReason(
        throwable: Throwable, reason: ErrorMessages = ErrorMessages.INTERNAL_SERVER_ERROR,
        activity: Activity = NavigationManager.getActivity()
    ): CSResult<Nothing> {
        val e = extractOriginalException(throwable)

        val isLoginActivity = activity is LoginActivity

        // This is a gRPC error response.
        //  See codes here - https://grpc.github.io/grpc/core/md_doc_statuscodes.html
        if (e is StatusRuntimeException) {
            val errorFromStatusRuntimeException = when (e.status.code) {
                Code.FAILED_PRECONDITION -> {
                    when (e.trailers?.get(metadataKeyOf("reason_code"))) {
                        UPGRADE_REQUIRED -> {
                            MasService.authDestroy()
                            activity.runOnUiThread {
                                LogoutManager.forceLogout(R.string.logout_upgrade_required)
                            }
                            ErrorMessages.UPGRADE_REQUIRED
                        }
                        else -> ErrorMessages.INTERNAL_SERVER_ERROR
                    }
                }
                Code.DEADLINE_EXCEEDED -> ErrorMessages.TIMEOUT
                in listOf(Code.PERMISSION_DENIED, Code.UNAUTHENTICATED) -> {
                    val thisReason = when (e.status.code) {
                        Code.PERMISSION_DENIED -> ErrorMessages.PERMISSION_DENIED
                        else -> ErrorMessages.AUTH
                    }
                    /**
                     * these nice requests causes the entire system to logout if we hit a "PERMISSION_DENIED" error
                     * That's exactly what we want because if you get this error, the token expired or is invalid
                     */
                    MasService.authDestroy()
                    activity.runOnUiThread {
                        if (isLoginActivity) LogoutManager.resetToNew()
                        else LogoutManager.forceLogout()
                    }
                    thisReason
                }

                else -> ErrorMessages.errorMessageExistsInStringOrNull(e.message.toString())
            }

            if (errorFromStatusRuntimeException != null) {
                return CSResult.MasFailure(errorFromStatusRuntimeException)
            }
        }

        val masErrorMessage = ErrorMessages.errorMessageExistsInStringOrNull(e.message.toString())
        if (masErrorMessage == null || masErrorMessage == ErrorMessages.INTERNAL_SERVER_ERROR) {
            AppAnalyticsService.addExceptionEvent(e)
        }

        val finalReason = masErrorMessage ?: reason

        Log.w(_tag, "handleSpecialResultOrReason(...) Error: $finalReason")

        return CSResult.MasFailure(finalReason)
    }

    private fun addActionEvent(
        eventEntry: AppAnalyticsService.AnalyticsEntry,
        result: CSResult<*>,
        failData: Map<String, *>? = null,
        failDataFromException: Boolean = false
    ) {
        result
            .onSuccess { AppAnalyticsService.addSuccessfulActionEvent(eventEntry) }
            .onFailure {
                val actualFailData =
                    if (failDataFromException) mapOf(
                        "reason" to (it.message
                            ?: ErrorMessages.INTERNAL_SERVER_ERROR.toString())
                    ) else failData

                AppAnalyticsService.addFailedActionEvent(eventEntry, actualFailData)
            }
    }

    fun login(
        msisdn: String, pin: String, activity: Activity,
        callback: (CSResult<LoginResponseModel>) -> Unit
    ) {
        val analyticsFailData = mapOf("msisdn" to msisdn)

        if (SandboxRepository.isSandboxEnabled) {
            masService.sandboxLogin_not_to_be_used_in_production(msisdn)
            SandboxRepository.delayedCallback {
                val result = sandboxRepository.login(msisdn, pin)
                addActionEvent(loginEntry, result, analyticsFailData)
                callback(result)
            }
            return
        }

        masService.login(msisdn, pin) { result ->
            addActionEvent(loginEntry, result.toCSResult(), analyticsFailData)
            result
                .onFailure {
                    callback(handleSpecialResultOrReason(it, ErrorMessages.AUTH, activity))
                }
                .onSuccess {
                    callback(result.toCSResult())
                }
        }
    }

    fun verifyOtp(
        otp: String, activity: Activity, callback: (CSResult<LoginResponseModel>) -> Unit
    ) {
        val analyticsFailData = mapOf("msisdn" to (MasService.getMyMsisdn() ?: "-err-"))

        try {
            if (SandboxRepository.isSandboxEnabled) {
                SandboxRepository.delayedCallback {
                    val result = sandboxRepository.verifyOtp(otp, MasService.getLoginToken())
                    addActionEvent(submitOtpEntry, result, analyticsFailData)
                    callback(sandboxRepository.verifyOtp(otp, MasService.getLoginToken()))
                }
                return
            }

            val msisdn = MasService.getMyMsisdn() ?: throw Exception("Empty Msisdn")
            val loginToken = MasService.getLoginToken() ?: throw Exception("Empty LoginToken")
            JwtHelper.getClaim(loginToken) ?: throw Exception("Failed JWT claim retrieval")

            masService.verifyOtp(otp) { loginResult ->
                addActionEvent(submitOtpEntry, loginResult.toCSResult(), analyticsFailData)
                loginResult
                    .onFailure {
                        callback(handleSpecialResultOrReason(it, ErrorMessages.AUTH, activity))
                    }
                    .onSuccess { loginResponse ->
                        val result = CSResult.Success(
                            LoginResponseModel(
                                loginResponse.agentId, msisdn, loginResponse.agentPin,
                                loginToken, loginResponse.refreshToken,
                                loginResponse.authenticationStatus,
                                loginResponse.message
                            )
                        )
                        callback(result)
                    }

            }
        } catch (e: Exception) {
            Log.e(_tag, "verifyOtp(...) Error: ${e.message}")
            AppAnalyticsService.addExceptionEvent(e, submitOtpEntry)
            callback(handleSpecialResultOrReason(e, ErrorMessages.AUTH, activity))
        }
    }

    fun updateLoginToken(callback: (CSResult<Unit>) -> Unit) {
        if (SandboxRepository.isSandboxEnabled) {
            callback(CSResult.Success(Unit))
            return
        }

        masService.updateLoginToken { result ->
            result.onSuccess {
                Log.i(_tag, "Successful renewal of JWT and Crediverse Session ID")
            }.onFailure { throwable ->
                /**
                 * Special handling will correctly LOGOUT if the error is _authentication_ or _permission_ related
                 */
                if (throwable is StatusRuntimeException) {
                    /**
                     * NOTE --- we _intentionally_ DO NOT use the callback here.
                     *          the handler below is PURELY to logout on a permission/auth related error
                     */
                    NavigationManager.runOnNavigationUIThread {
                        handleSpecialResultOrReason(throwable)
                    }
                }

                Log.e(
                    _tag,
                    "Could not update login token and crediverse session: ${throwable.message}"
                )
            }
            callback(result.toCSResult())
        }
    }

    fun getVersionStatus(activity: Activity, callback: (CSResult<VersionStatus>) -> Unit) {
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                callback(sandboxRepository.getVersionStatus())
            }
            return
        }

        masService.getVersionStatus { result ->
            result.onSuccess {
                callback(CSResult.Success(it))
            }.onFailure {
                callback(
                    handleSpecialResultOrReason(it, ErrorMessages.INTERNAL_SERVER_ERROR, activity)
                )
            }
        }
    }

    private val addSellEvent =
        { analyticsEntry: AppAnalyticsService.AnalyticsEntry, result: CSResult<SellAirtimeResponse>, failData: Map<String, *>? ->
            result
                .onFailure {
                    AppAnalyticsService.addFailedActionEvent(analyticsEntry, failData)
                }.onSuccess {
                    AppAnalyticsService.addSuccessfulActionEvent(
                        if (it.followUpRequired) analyticsEntry.withData(mapOf("followUp" to true))
                        else analyticsEntry
                    )
                }
        }

    fun transferCredit(
        amount: Double, msisdn: String,
        callback: (CSResult<SellAirtimeResponse>) -> Unit
    ) {
        val coordinates = LocationService.getGrpcLocation()
        val failData = mapOf("recipient" to msisdn, "amount" to amount)

        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                val result = sandboxRepository.sell(TXType.TRANSFER, amount, msisdn)
                addSellEvent(transferEntry, result, failData)
                callback(result)
            }
            return
        }

        masService.transferCredit(amount, msisdn, coordinates) { transferCreditResponse ->
            addSellEvent(transferEntry, transferCreditResponse.toCSResult(), failData)
            transferCreditResponse
                .onFailure {
                    callback(handleSpecialResultOrReason(it))
                }
                .onSuccess {
                    callback(transferCreditResponse.toCSResult())
                }
        }

    }

    fun sellAirtime(
        amount: Double, msisdn: String,
        callback: (CSResult<SellAirtimeResponse>) -> Unit
    ) {
        val coordinates = LocationService.getGrpcLocation()
        val failData = mapOf("recipient" to msisdn, "amount" to amount)

        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                val result = sandboxRepository.sell(TXType.SELL, amount, msisdn)
                addSellEvent(airtimeSaleEntry, result, failData)
                callback(result)
            }

            return
        }

        masService.sellAirtime(amount, msisdn, coordinates) { sellAirtimeResponse ->
            addSellEvent(airtimeSaleEntry, sellAirtimeResponse.toCSResult(), failData)
            sellAirtimeResponse
                .onFailure {
                    callback(handleSpecialResultOrReason(it))
                }
                .onSuccess {
                    callback(sellAirtimeResponse.toCSResult())
                }
        }
    }

    fun agentFeedback(
        agentFeedback: AgentFeedbackModel, callback: ((CSResult<Unit>) -> Unit)? = null
    ) {
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                val result = CSResult.Success(Unit)
                addActionEvent(submitFeedbackEntry, result)
                callback?.invoke(result)
            }
            return
        }

        masService.agentFeedback(agentFeedback) { agentFeedbackResponse ->
            addActionEvent(submitFeedbackEntry, agentFeedbackResponse.toCSResult())
            agentFeedbackResponse
                .onFailure { callback?.invoke(handleSpecialResultOrReason(it)) }
                .onSuccess {
                    callback?.invoke(agentFeedbackResponse.toCSResult())
                }
        }
    }

    fun getTransactionHistory(
        transactionsPerPage: Int, pageNumber: Int,
        callback: (CSResult<List<TransactionModel>>) -> Unit
    ) {
        val analyticsPageNoData = mapOf("pageNo" to pageNumber)

        val addEvent = { result: CSResult<List<TransactionModel>> ->
            result
                .onSuccess {
                    if (it.isEmpty() && pageNumber == 0) {
                        AppAnalyticsService.addFailedActionEvent(
                            getHistoryPageEntry.withReason("ZERO_TRANSACTIONS")
                        )
                    } else if (it.isNotEmpty()) {
                        AppAnalyticsService.addSuccessfulActionEvent(
                            getHistoryPageEntry, analyticsPageNoData
                        )
                    }
                }.onFailure {
                    AppAnalyticsService.addFailedActionEvent(
                        getHistoryPageEntry.withReason(
                            it.message ?: ErrorMessages.INTERNAL_SERVER_ERROR.toString()
                        ), analyticsPageNoData
                    )
                }
        }

        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback(1000) {
                val result = sandboxRepository.getTransactionHistory()
                addEvent(result)
                callback(result)
            }
            return
        }

        masService.getTransactionHistory(transactionsPerPage, pageNumber) { txListResult ->
            addEvent(txListResult.toCSResult())
            txListResult
                .onFailure {
                    callback(handleSpecialResultOrReason(it))
                }
                .onSuccess {
                    callback(txListResult.toCSResult())
                }
        }
    }

    fun getAccountInfo(callback: (CSResult<AccountInfoResponseModel>) -> Unit) {
        if (SandboxRepository.isSandboxEnabled) {
            callback(sandboxRepository.getAccountInfo())
            return
        }

        masService.getAccountInfo { accountInfoResult ->
            accountInfoResult.onFailure {
                callback(handleSpecialResultOrReason(it, ErrorMessages.GET_ACCOUNT_INFO_FAILURE))
            }.onSuccess {
                callback(accountInfoResult.toCSResult())
            }
        }
    }

    fun updateAccountInfo(
        accountInfo: AccountInfoResponseModel,
        callback: (CSResult<AccountInfoResponseModel>) -> Unit
    ) {
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                val result = sandboxRepository.updateAccountInfo(accountInfo)
                addActionEvent(
                    editProfileEntry, result,
                    failData = null, failDataFromException = true
                )
                callback(result)
            }
            return
        }

        masService.updateAccountInfo(accountInfo) { updateAccountInfoResult ->
            addActionEvent(
                editProfileEntry, updateAccountInfoResult.toCSResult(),
                failData = null, failDataFromException = true
            )
            updateAccountInfoResult
                .onFailure { callback(handleSpecialResultOrReason(it)) }
                .onSuccess {
                    callback(updateAccountInfoResult.toCSResult())
                }
        }
    }

    fun setSalesTarget(
        salesTargetModel: SalesTargetModel,
        callback: (CSResult<SalesTargetModel>) -> Unit
    ) {
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                callback(sandboxRepository.setSalesTarget(salesTargetModel))
            }
            return
        }

        masService.setSalesTarget(salesTargetModel) { salesTargetResponse ->
            salesTargetResponse
                .onFailure {
                    callback(handleSpecialResultOrReason(it, ErrorMessages.SET_TARGET_FAILED))
                }
                .onSuccess {
                    callback(salesTargetResponse.toCSResult())
                }
        }
    }

    fun changePin(oldPin: String, newPin: String, callback: (CSResult<Unit>) -> Unit) {
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                //callback(CSResult.Failure(CSException("INVALID_OLD_PIN")))
                val result = CSResult.Success(Unit)
                addActionEvent(changePinEntry, result)
                callback(result)
            }
            return
        }

        masService.changePin(oldPin, newPin) { pinChangeResult ->
            addActionEvent(changePinEntry, pinChangeResult.toCSResult())
            pinChangeResult
                .onFailure {
                    callback(handleSpecialResultOrReason(it, ErrorMessages.CHANGE_PIN_FAILED))
                }.onSuccess {
                    callback(pinChangeResult.toCSResult())
                }
        }
    }

    fun getBalances(callback: (CSResult<BalancesResponseModel>) -> Unit) {
        Log.e(_tag, "RUNNING getBalances()")

        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                val result = sandboxRepository.getBalances()
                addActionEvent(getBalancesEntry, result)
                callback(result)
            }
            return
        }

        masService.getBalances { balancesResult ->
            addActionEvent(getBalancesEntry, balancesResult.toCSResult())
            balancesResult
                .onFailure {
                    callback(handleSpecialResultOrReason(it))
                }
                .onSuccess {
                    callback(balancesResult.toCSResult())
                }
        }
    }

    fun getTeamMembership(callback: (CSResult<TeamMembership>) -> Unit) {
        if (SandboxRepository.isSandboxEnabled) {
            callback(sandboxRepository.getTeamMembership())
            return
        }

        masService.getTeamMembership { teamResult ->
            teamResult
                .onFailure {
                    callback(handleSpecialResultOrReason(it, ErrorMessages.GET_MEMBERSHIP_FAILED))
                }
                .onSuccess {
                    callback(teamResult.toCSResult())
                }
        }
    }

    fun getTeam(callback: (CSResult<TeamModel>) -> Unit) {
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                callback(sandboxRepository.getTeam())
            }
            return
        }

        masService.getTeam { teamResult ->
            teamResult
                .onFailure { callback(handleSpecialResultOrReason(it)) }
                .onSuccess {
                    callback(teamResult.toCSResult())
                }
        }
    }

    fun isTeamLead(activity: Activity, callback: (CSResult<Boolean>) -> Unit) {
        if (SandboxRepository.isSandboxEnabled) {
            callback(CSResult.Success(true))
            return
        }

        masService.isTeamLead { isTeamLeadResult ->
            isTeamLeadResult
                .onFailure {
                    Log.e(_tag, "IsTeamLead unsuccessful: ${it.message}")
                    callback(
                        handleSpecialResultOrReason(
                            it, ErrorMessages.INTERNAL_SERVER_ERROR, activity
                        )
                    )
                }
                .onSuccess {
                    callback(isTeamLeadResult.toCSResult())
                }
        }
    }

    fun mobileMoneyLogin(
        username: String, password: String, callback: (CSResult<Unit>) -> Unit
    ) {
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                if (username.endsWith("9")) {
                    callback(SandboxRepository.getFailureResultFromError(ErrorMessages.MM_UNAUTHORIZED))
                } else {
                    callback(CSResult.Success(Unit))
                }
            }
            return
        }

        masService.mobileMoneyLogin(username, password) { mmLoginResult ->
            mmLoginResult.onSuccess {
                callback(mmLoginResult.toCSResult())
            }.onFailure {
                callback(handleSpecialResultOrReason(it, ErrorMessages.MM_UNAUTHORIZED))
            }
        }
    }

    fun buyAirtimeWithMobileMoney(amount: Double, callback: (CSResult<Unit>) -> Unit) {
        Log.e(_tag, "RUNNING getMobileMoneyBalance()")
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                callback(sandboxRepository.buyCreditWithMobileMoney(amount))
            }
            return
        }

        masService.buyAirtimeWithMobileMoney(amount) { mobileMoneyBalanceResult ->
            mobileMoneyBalanceResult
                .onFailure { callback(handleSpecialResultOrReason(it)) }
                .onSuccess {
                    callback(mobileMoneyBalanceResult.toCSResult())
                }
        }
    }

    fun getMobileMoneyBalance(callback: (CSResult<MobileMoneyBalanceResponseModel>) -> Unit) {
        Log.e(_tag, "RUNNING getMobileMoneyBalance()")
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                callback(sandboxRepository.getMobileMoneyBalance())
            }
            return
        }

        masService.getMobileMoneyBalance { mobileMoneyBalanceResult ->
            mobileMoneyBalanceResult
                .onFailure {
                    callback(handleSpecialResultOrReason(it, ErrorMessages.GET_MM_BALANCE_FAILED))
                }
                .onSuccess {
                    callback(mobileMoneyBalanceResult.toCSResult())
                }
        }
    }

    fun mobileMoneyDeposit(
        amount: Double, msisdn: String, callback: (CSResult<MobileMoneyDepositResponse>) -> Unit
    ) {
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                if (msisdn.endsWith("9")) {
                    callback(SandboxRepository.getFailureResultFromError(ErrorMessages.DEPOSIT_FAILED))
                } else if (msisdn.endsWith("7")) {
                    callback(SandboxRepository.getFailureResultFromError(ErrorMessages.MM_UNAUTHORIZED))
                } else {
                    callback(CSResult.Success(MobileMoneyDepositResponse(MobileMoneyDepositStatus.SUCCESS)))
                }
            }
            return
        }

        masService.mobileMoneyDeposit(amount, msisdn) { mmDepositResult ->
            mmDepositResult
                .onFailure {
                    callback(handleSpecialResultOrReason(it, ErrorMessages.DEPOSIT_FAILED))
                }
                .onSuccess {
                    callback(mmDepositResult.toCSResult())
                }
        }
    }

    fun mobileMoneyWithdrawal(
        amount: Double, fromMsisdn: String, callback: (CSResult<Unit>) -> Unit
    ) {
        Log.e(_tag, "Begin withdrawal: $amount")
        //if (SandboxRepository.sandboxEnabled) {
        val randomDelay = kotlinRandom.nextLong(5000, 15000)
        SandboxRepository.delayedCallback(randomDelay) {
            if (fromMsisdn.endsWith("9")) {
                callback(SandboxRepository.getFailureResultFromError(ErrorMessages.WITHDRAW_FAILED))
            } else if (fromMsisdn.endsWith("7")) {
                callback(SandboxRepository.getFailureResultFromError(ErrorMessages.MM_UNAUTHORIZED))
            } else {
                callback(CSResult.Success(Unit))
            }
        }
        return
        //}

        /*
        masService.mobileMoneyWithdrawal(amount, fromMsisdn) { mmWithdrawalResult ->
            mmWithdrawalResult
                .onFailure { callback(handleSpecialResultOrReason(it)) }
                .onSuccess {
                    callback(mmWithdrawalResult)
                }
        }
        */
    }

    fun writeEvents(
        events: List<AppAnalyticsService.AnalyticsEvent>, onReady: (CSResult<Int>) -> Unit
    ) {
        if (events.isEmpty()) return

        if (SandboxRepository.isSandboxEnabled) {
            var hasFailure = false
            events.find { hasFailure = it.entry.isFailure(); hasFailure }
            when {
                hasFailure -> onReady(CSResult.Failure(CSException("Had some failures")))
                else -> onReady(CSResult.Success(events.size))
            }
            return
        }

        val version = AppFlag.System.versionName
        masService.submitAnalytics(version, events) { result ->
            result
                .onFailure {
                    onReady(handleSpecialResultOrReason(it))
                }.onSuccess {
                    onReady(result.toCSResult())
                }
        }
    }

    private fun getFinalSalesSummary(
        salesSummaryRequest: SalesSummaryRequest,
        salesPeriod: SalesPeriod,
        callback: (Result<SalesSummaryModel>) -> Unit
    ) {
        val finalCallback: (Result<SalesSummaryModel>) -> Unit = { result ->
            var cacheSaveName =
                Formatter.getDatestampFromEpoch(salesPeriod.startTime) + "_" + salesSummaryRequest.type
            if (salesSummaryRequest.msisdn != null)
                cacheSaveName += "_" + salesSummaryRequest.msisdn

            result.onSuccess { salesSummary ->
                // Only cache if successful and NOT today
                if (!isToday(salesPeriod.startTime)) {
                    cacheService.save(
                        salesSummary.value, CacheService.lifetimeDays(days = 95), cacheSaveName
                    )
                }
                Log.w(_tag, "$cacheSaveName - " + Gson().toJson(result))
            }.onFailure {
                Log.w(_tag, "Failed to save to cache: $cacheSaveName")
                Log.w(_tag, "Cache saving error: ${it.message.toString()}")
            }

            callback(result)
        }

        when (salesSummaryRequest.type) {
            SalesSummaryType.EXEC -> masService.getGlobalSalesSummary(
                salesPeriod.startTime, salesPeriod.endTime, finalCallback
            )
            SalesSummaryType.TEAM -> masService.getTeamSalesSummary(
                salesPeriod.startTime, salesPeriod.endTime, finalCallback
            )
            else -> masService.getSingleSalesSummary(
                salesPeriod.startTime, salesPeriod.endTime, salesSummaryRequest.msisdn,
                finalCallback
            )
        }
    }

    enum class SalesSummaryType { SELF, TEAM, MEMBER, EXEC }
    data class SalesSummaryRequest(
        val type: SalesSummaryType,
        val salesSummaryPeriod: SalesSummaryPeriod,
        val salesSummaryIntervalAge: SalesSummaryIntervalAge,
        val msisdn: String? = null,
    )

    fun getHourlySalesSummary(
        intervals: List<SalesSummaryIntervalAge>,
        callback: (CSResult<List<HourlySalesSummaryModel>>) -> Unit
    ) {
        Log.e(_tag, "RUNNING getHourlySalesSummary()")
        if (SandboxRepository.isSandboxEnabled) {
            callback(sandboxRepository.getHourlySalesSummary())
            return
        }

        val todayHourlyFuture = CompletableFuture<Result<HourlySalesSummaryModel>>()
        val yesterdayHourlyFuture = CompletableFuture<Result<HourlySalesSummaryModel>>()

        val intervalToday = intervals[0]
        val intervalYesterday = intervals[1]

        val todayPeriod = getDaySalesPeriod(intervalToday)
        masService.getGlobalHourlySalesSummary(todayPeriod.startTime, todayPeriod.endTime) {
            todayHourlyFuture.complete(it)
        }

        val yesterdayPeriod = getDaySalesPeriod(intervalYesterday)
        masService.getGlobalHourlySalesSummary(yesterdayPeriod.startTime, yesterdayPeriod.endTime) {
            yesterdayHourlyFuture.complete(it)
        }

        CompletableFuture.allOf(todayHourlyFuture, yesterdayHourlyFuture).thenRun {
            val today = todayHourlyFuture.get()
            val yesterday = yesterdayHourlyFuture.get()

            try {
                callback(CSResult.Success(listOf(today.getOrThrow(), yesterday.getOrThrow())))
            } catch (e: Throwable) {
                callback(handleSpecialResultOrReason(e))
            }
        }

    }

    fun getSalesSummary(
        salesSummaryRequest: List<SalesSummaryRequest>,
        callback: (CSResult<List<SalesSummaryModel>>) -> Unit
    ) {
        Log.e(_tag, "RUNNING getSalesSummary()")
        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                callback(sandboxRepository.getSalesSummary(context, salesSummaryRequest))
            }
            return
        }

        val salesRequestPeriods = salesSummaryRequest.map {
            getSalesPeriods(it.salesSummaryPeriod, it.salesSummaryIntervalAge)
        }

        val salesSummaryModels: MutableList<SalesSummaryModel> = mutableListOf()

        runBlocking {
            val deferredDays =
                salesRequestPeriods.mapIndexed { index, salesPeriods ->
                    async {
                        salesPeriods.map { salesPeriod ->
                            val isToday = isToday(salesPeriod.startTime)

                            if (!isToday) {
                                var cacheSaveName =
                                    Formatter.getDatestampFromEpoch(salesPeriod.startTime) + "_" + salesSummaryRequest[0].type

                                if (salesSummaryRequest[0].msisdn != null)
                                    cacheSaveName += "_" + salesSummaryRequest[0].msisdn

                                val cachedValue =
                                    cacheService.getOrNull<SalesSummaryValue>(cacheSaveName)

                                if (cachedValue != null) {
                                    return@map async {
                                        Result.success(
                                            SalesSummaryModel(
                                                salesPeriod.startTime, salesPeriod.endTime,
                                                cachedValue
                                            )
                                        )
                                    }
                                }
                            }

                            return@map async deferred@{
                                val deferred = CompletableDeferred<Result<SalesSummaryModel>>()
                                withContext(Dispatchers.Default) {
                                    getFinalSalesSummary(
                                        salesSummaryRequest[index], salesPeriod
                                    ) {
                                        deferred.complete(it)
                                    }
                                }
                                return@deferred deferred.await()
                            }
                        }.awaitAll()
                    }
                }

            try {
                val resultDays = deferredDays.awaitAll()

                resultDays.forEachIndexed { index, days ->
                    val sumOfAirtime = days.sumOf {
                        it.getOrThrow().value.airtimeSalesValue.toDoubleOrNull() ?: 0.0
                    }
                    val sumOfAirtimeCostOfGoodsSold = days.sumOf {
                        it.getOrThrow().value.airtimeCostOfGoodsSold.toDoubleOrNull() ?: 0.0
                    }
                    val sumOfBundles = days.sumOf {
                        it.getOrThrow().value.bundleSalesValue.toDoubleOrNull() ?: 0.0
                    }
                    val sumOfBundleCostOfGoodsSold = days.sumOf {
                        it.getOrThrow().value.bundleCostOfGoodsSold.toDoubleOrNull() ?: 0.0
                    }

                    val countOfAirtime = days.sumOf { it.getOrThrow().value.airtimeSalesCount }
                    val countOfAirtimeUnknownCost =
                        days.sumOf { it.getOrThrow().value.airtimeUnknownCostCount }
                    val countOfBundles = days.sumOf { it.getOrThrow().value.bundleSalesCount }
                    val countOfBundleUnknownCost =
                        days.sumOf { it.getOrThrow().value.bundleUnknownCostCount }

                    val totalStock = if (salesSummaryRequest[index].type != SalesSummaryType.SELF) {
                        days.sumOf { it.getOrThrow().value.stockLevel?.toDoubleOrNull() ?: 0.0 }
                    } else null
                    val sumOfTradeBonus = days.sumOf {
                        it.getOrThrow().value.tradeBonusValue.toDoubleOrNull() ?: 0.0
                    }
                    val sumOfInboundTransfersValue = days.sumOf {
                        it.getOrThrow().value.inboundTransfersValue.toDoubleOrNull() ?: 0.0
                    }
                    val countOfInboundTransfersCount =
                        days.sumOf { it.getOrThrow().value.inboundTransfersCount }
                    salesSummaryModels.add(
                        index, SalesSummaryModel(
                            days.first().getOrThrow().startTime,
                            days.last().getOrThrow().endTime,
                            SalesSummaryValue(
                                sumOfAirtime.toString(), countOfAirtime,
                                sumOfAirtimeCostOfGoodsSold.toString(), countOfAirtimeUnknownCost,

                                sumOfBundles.toString(), countOfBundles,
                                sumOfBundleCostOfGoodsSold.toString(), countOfBundleUnknownCost,
                                sumOfTradeBonus.toString(), sumOfInboundTransfersValue.toString(),
                                countOfInboundTransfersCount,
                                totalStock?.toString()
                            )
                        )
                    )
                }

                callback(CSResult.Success(salesSummaryModels))
            } catch (e: Exception) {
                callback(handleSpecialResultOrReason(e))
            }
        }
    }

    data class SalesPeriod(val startTime: Long, val endTime: Long)

    companion object {
        const val UPGRADE_REQUIRED = "426"

        private val _weekStartDay by lazy {
            val weekDaysMap = mapOf(
                "monday" to 1, "tuesday" to 2, "wednesday" to 3,
                "thursday" to 4, "friday" to 5, "saturday" to 6, "sunday" to 7,
            )
            val dayOfWeek = AppFlag.Stats.startOfWeek.lowercase()
            val configuredStartingDay = weekDaysMap.map {
                when (it.key) {
                    dayOfWeek -> it.value
                    else -> null
                }
            }.firstNotNullOfOrNull { it } ?: 1
            DayOfWeek.of(configuredStartingDay)
        }
        private val weekEndDay by lazy { _weekStartDay.plus(6) }

        val weekStartDay: DayOfWeek by lazy { _weekStartDay }
        private val zoneId = ZoneId.systemDefault()

        fun isToday(epoch: Long): Boolean {
            val dateTime = Instant.ofEpochMilli(epoch * 1000).atZone(zoneId).toLocalDateTime()
            val now = LocalDateTime.now(zoneId)
            return dateTime.toLocalDate() == now.toLocalDate()
        }

        private fun getSalesPeriodsForMonth(monthsAgo: Byte): List<SalesPeriod> {
            val now = LocalDate.now()
            val startDate =
                now.minusMonths(monthsAgo.toLong()).withDayOfMonth(1)
                    .atStartOfDay(zoneId).toEpochSecond()
            val endDate =
                now.minusMonths(monthsAgo.toLong()).plusMonths(1).withDayOfMonth(1)
                    .atStartOfDay(zoneId).toEpochSecond()

            val salesPeriods = mutableListOf<SalesPeriod>()

            var currentDate = startDate
            while (currentDate < endDate) {
                val startOfToday = currentDate
                val startOfTomorrow = currentDate + 86400
                salesPeriods.add(SalesPeriod(startOfToday, startOfTomorrow))
                currentDate += 86400
                if (isToday(startOfToday)) break
            }
            return salesPeriods
        }

        private fun getSalesPeriodsForWeek(weeksAgo: Byte): List<SalesPeriod> {
            val baseTime = LocalDate.now().atStartOfDay(zoneId).minusWeeks(weeksAgo.toLong())

            val startOfWeek = baseTime.with(previousOrSame(weekStartDay)).toEpochSecond()
            val endOfWeek = baseTime.with(nextOrSame(weekEndDay)).toEpochSecond()

            val periods = mutableListOf<SalesPeriod>()

            var currentDay = startOfWeek
            while (currentDay < endOfWeek) {
                val startOfDay = currentDay
                val startOfNextDay = currentDay + 86400
                periods.add(SalesPeriod(startOfDay, startOfNextDay))
                currentDay += 86400
                if (isToday(startOfDay)) break
            }

            return periods
        }

        fun getDaySalesPeriod(salesSummaryIntervalAge: SalesSummaryIntervalAge): SalesPeriod {
            return getSalesPeriods(SalesSummaryPeriod.DAILY, salesSummaryIntervalAge)[0]
        }

        fun getSalesPeriods(
            salesSummaryPeriod: SalesSummaryPeriod,
            salesSummaryIntervalAge: SalesSummaryIntervalAge
        ): List<SalesPeriod> {
            val salesPeriods = when (salesSummaryPeriod) {
                SalesSummaryPeriod.DAILY -> {
                    val start =
                        LocalDate.now().minusDays(salesSummaryIntervalAge.age.toLong())
                            .atStartOfDay(zoneId)
                    val end = start.plusDays(1)
                    return listOf(SalesPeriod(start.toEpochSecond(), end.toEpochSecond()))
                }
                SalesSummaryPeriod.WEEKLY -> getSalesPeriodsForWeek(
                    salesSummaryIntervalAge.age
                )
                SalesSummaryPeriod.MONTHLY -> getSalesPeriodsForMonth(
                    salesSummaryIntervalAge.age
                )
            }

            return salesPeriods
        }

        fun logout() {
            ViewModelUtils.resetAllViewModelCaches()
            MasService.authDestroy()
        }

        private val masService = MasService()
    }
}
