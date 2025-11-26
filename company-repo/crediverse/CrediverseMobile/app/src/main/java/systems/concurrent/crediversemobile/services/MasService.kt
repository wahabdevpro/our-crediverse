package systems.concurrent.crediversemobile.services

import android.util.Log
import com.google.common.util.concurrent.ListenableFuture
import io.grpc.*
import io.grpc.ClientInterceptors.CheckedForwardingClientCall
import systems.concurrent.crediversemobile.App
import systems.concurrent.crediversemobile.models.*
import systems.concurrent.crediversemobile.utils.AppFlag
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.metadataKeyOf
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.nowEpoch
import systems.concurrent.crediversemobile.utils.Formatter.makeEmptyNull
import systems.concurrent.crediversemobile.utils.JwtHelper
import systems.concurrent.crediversemobile.utils.NavigationManager
import systems.concurrent.masapi.MasApi
import systems.concurrent.masapi.MasApi.AnalyticsRequest
import systems.concurrent.masapi.MasApi.BuyWithMobileMoneyRequest
import systems.concurrent.masapi.MasGrpc
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class MasService {

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        private var msisdn: String? = null
        private var loginToken: String? = null
        private var agentId: String? = null

        private var agentPin: String? = null
        private var agentPinValidated = false

        fun getAgentPin(): String? {
            if (!agentPinValidated) return null
            return agentPin
        }

        fun getLoginTokenClaim() = loginToken?.let {
            JwtHelper.getClaim(it)
        }

        fun getLoginToken() = loginToken
        fun getMyMsisdn() = msisdn

        fun hasMobileMoneyToken(): Boolean {
            return getLoginTokenClaim()?.mobileMoneyToken?.isNotEmpty() ?: false
        }

        // equivalent of 'timeout'
        private const val deadlineMs: Long = 20000

        fun authDestroy() {
            // we don't clear the msisdn, it is reused for caching
            agentId = null
            loginToken = null
            agentPin = null
            agentPinValidated = false
        }
    }

    private var loginStub: MasGrpc.MasFutureStub? = null
    private var asyncStub: MasGrpc.MasFutureStub? = null
    private var _channelBuilder: ManagedChannelBuilder<*>? = null

    private val emptyRequest = MasApi.NoParam.newBuilder().build()

    private fun getChannel(): ManagedChannelBuilder<*> {
        if (_channelBuilder != null) return _channelBuilder!!

        val caPath = AppFlag.Network.masCAPath
        val hostname = AppFlag.Network.masHostname
        val port = AppFlag.Network.masPort

        val getSignedTlsChannelBuilder = {
            _channelBuilder =
                ManagedChannelBuilder.forAddress(hostname, port).useTransportSecurity()
            _channelBuilder!!
        }

        if (caPath.isEmpty()) return getSignedTlsChannelBuilder()

        /**
         * Assuming the CA path is set ... let's try to open the certificate...
         *  Failing that, we fallback to accepting only SIGNED TLS certificates
         */
        return try {
            val caCertStream = App.context.assets.open(caPath)
            _channelBuilder = Grpc.newChannelBuilder(
                "${hostname}:${port}",
                TlsChannelCredentials.newBuilder().trustManager(caCertStream).build()
            )
            _channelBuilder!!
        } catch (e: Exception) {
            Log.e(_tag, "Unable to load CA Certificate from path: $caPath")
            Log.w(_tag, "Falling back to allow SIGNED certificates...")
            getSignedTlsChannelBuilder()
        }
    }

    class MetadataExtractor : ClientInterceptor {
        override fun <ReqT, RespT> interceptCall(
            method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions, next: Channel
        ): ClientCall<ReqT, RespT> {
            val delegate = next.newCall(method, callOptions)

            return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(delegate) {
                override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                    val capturingListener = ExtractMetadataListener(responseListener)
                    super.start(capturingListener, headers)
                }
            }
        }
    }

    class ExtractMetadataListener<RespT>(private val delegate: ClientCall.Listener<RespT>) :
        ClientCall.Listener<RespT>() {

        override fun onHeaders(headers: Metadata) {
            /**
             * TODO: Get metadata from MAS responses, use the data for something
             */
            // val authMetadataValue = headers.get(metadataKeyOf("authorization"))
            super.onHeaders(headers)
        }

        override fun onMessage(message: RespT) {
            delegate.onMessage(message)
        }

        override fun onClose(status: Status, trailers: Metadata) {
            delegate.onClose(status, trailers)
        }
    }

    class MetadataInjector : ClientInterceptor {
        override fun <ReqT : Any?, RespT : Any?> interceptCall(
            method: MethodDescriptor<ReqT, RespT>?, callOptions: CallOptions?, next: Channel?
        ): ClientCall<ReqT, RespT> {
            val call = next?.newCall(method, callOptions)
            val callForwarding = object : CheckedForwardingClientCall<ReqT, RespT>(call) {
                override fun checkedStart(responseListener: Listener<RespT>?, headers: Metadata) {
                    val headerMap = mapOf(
                        "version_code" to AppFlag.System.versionCode.toString(),
                        "client_time" to nowEpoch().toString(),
                        "authorization" to loginToken,
                    )

                    headerMap.forEach {
                        if (!headers.containsKey(metadataKeyOf(it.key)) && it.value != null)
                            headers.put(metadataKeyOf(it.key), it.value)
                    }

                    call?.start(responseListener, headers)
                }
            }
            return callForwarding
        }
    }

    private fun getWhitelistedAsyncStub(): MasGrpc.MasFutureStub {
        return getAsyncStub(verifyLoginToken = false)
    }

    private fun getAsyncStub(verifyLoginToken: Boolean = true): MasGrpc.MasFutureStub {
        val stubExists = asyncStub != null

        // Missing token should be treated as UNAUTHENTICATED
        if (verifyLoginToken && loginToken == null) {
            NavigationManager.runOnNavigationUIThread { LogoutManager.forceLogout() }
        }

        if (!stubExists) {
            val mChannel = getChannel().intercept(MetadataInjector()).build()
            asyncStub = MasGrpc.newFutureStub(mChannel)
        }

        return asyncStub!!.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS)
            .withInterceptors(MetadataExtractor())
    }

    @Suppress("FunctionName")
    fun sandboxLogin_not_to_be_used_in_production(inputMsisdn: String) {
        msisdn = inputMsisdn
        loginToken = "Just-a-dummy-token.that-can't-validate-against-MAS-anyway"
    }

    fun login(inputMsisdn: String, pin: String, callback: (Result<LoginResponseModel>) -> Unit) {
        val loginRequest = MasApi.LoginRequest
            .newBuilder().setMsisdn(inputMsisdn).setPin(pin).build()

        val futureResult = getWhitelistedAsyncStub().login(loginRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            if (result.authenticationStatus != MasApi.AuthenticationStatus.REQUIRE_OTP) {
                callback(
                    Result.failure(Throwable(AuthStatus.UNKNOWN_AUTHENTICATION_STATUS.toString()))
                )
                return@thenAcceptAsync
            }

            val loginResponseModel = LoginResponseModel(
                result.agentId, result.agentMsisdn, pin, result.loginToken, result.refreshToken,
                AuthStatus.AUTHENTICATED, result.message
            )

            // Agent PIN remains invalid until after the OTP step
            agentPinValidated = false
            agentPin = pin
            msisdn = inputMsisdn
            loginToken = result.loginToken

            callback(Result.success(loginResponseModel))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun verifyOtp(otp: String, callback: (Result<LoginResponseModel>) -> Unit) {
        val otpLoginRequest = MasApi.LoginRequest
            .newBuilder().setMsisdn(msisdn).setOneTimePin(otp).build()

        val futureResult = getAsyncStub().submitOtp(otpLoginRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            val authStatus = when (result.authenticationStatus) {
                MasApi.AuthenticationStatus.AUTHENTICATED -> AuthStatus.AUTHENTICATED
                else -> AuthStatus.UNKNOWN_AUTHENTICATION_STATUS
            }

            // Authentication is validated ... the PIN can be used
            agentPinValidated = true

            val loginResponseModel = LoginResponseModel(
                result.agentId, result.agentMsisdn, agentPin ?: "",
                result.loginToken, result.refreshToken, authStatus, result.message
            )

            Log.i(_tag, "MAS (submitOtp): Auth Status: ${result.authenticationStatus}")
            val finalResult = when (result.authenticationStatus) {
                MasApi.AuthenticationStatus.AUTHENTICATED -> {
                    agentId = result.agentId
                    Result.success(loginResponseModel)
                }
                else -> Result.failure(Exception("Status is not 'AUTHENTICATED'"))
            }

            callback(finalResult)
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun updateLoginToken(callback: (Result<Unit>) -> Unit) {
        val futureResult = getAsyncStub().updateLoginToken(emptyRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            loginToken = result.loginToken
            callback(Result.success(Unit))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun getVersionStatus(callback: (Result<VersionStatus>) -> Unit) {
        val versionStatusRequest = MasApi.VersionStatusRequest
            .newBuilder().setAppVersionCode(AppFlag.System.versionCode).build()

        val futureResult = getWhitelistedAsyncStub().getVersionStatus(versionStatusRequest)

        asCompletableFuture(futureResult).thenAcceptAsync {
            val deprecationDate =
                if (it.hasAppVersionDeprecationDate()) it.appVersionDeprecationDate else null
            val priority = if (it.hasAppUpdatePriority()) it.appUpdatePriority else null
            val url = if (it.hasAppUpdateDownloadUrl()) it.appUpdateDownloadUrl else null

            val result = VersionStatus(
                it.isAppVersionOk, deprecationDate,
                it.appVersionCodeLatest, it.appVersionNameLatest,
                VersionPriority.fromCode(priority), url
            )
            callback(Result.success(result))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun setAuthFromCache(loginResponseModel: LoginResponseModel) {
        agentId = loginResponseModel.agentId
        msisdn = loginResponseModel.agentMsisdn
        loginToken = loginResponseModel.loginToken
        agentPin = loginResponseModel.agentPin
    }

    fun sellAirtime(
        amount: Double, msisdn: String, coordinates: GrpcCoordinatesModel?,
        callback: (Result<SellAirtimeResponse>) -> Unit
    ) {
        val sellAirtimeRequestBuilder = MasApi.SellAirtimeRequest.newBuilder()
            .setAmount(amount.toString()).setMsisdn(msisdn)

        if (coordinates != null) {
            sellAirtimeRequestBuilder.latitude = coordinates.latitude
            sellAirtimeRequestBuilder.longitude = coordinates.longitude
        }

        val sellAirtimeRequest = sellAirtimeRequestBuilder.build()

        val futureResult = getAsyncStub().sellAirtime(sellAirtimeRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            callback(Result.success(SellAirtimeResponse(result.followUpRequired)))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun transferCredit(
        amount: Double, msisdn: String, coordinates: GrpcCoordinatesModel?,
        callback: (Result<SellAirtimeResponse>) -> Unit
    ) {
        val transferCreditRequestBuilder = MasApi.SellAirtimeRequest.newBuilder()
            .setAmount(amount.toString()).setMsisdn(msisdn)

        if (coordinates != null) {
            transferCreditRequestBuilder.latitude = coordinates.latitude
            transferCreditRequestBuilder.longitude = coordinates.longitude
        }

        val transferCreditRequest = transferCreditRequestBuilder.build()

        val futureResult = getAsyncStub().sellAirtimeWholesale(transferCreditRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            callback(Result.success(SellAirtimeResponse(result.followUpRequired)))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun agentFeedback(
        agentFeedbackModel: AgentFeedbackModel, callback: ((Result<Unit>) -> Unit)? = null
    ) {
        val agentFeedbackRequest = MasApi.FeedBackRequest.newBuilder()
            .setAgentId(agentId).setName(agentFeedbackModel.name)
            .setTier(agentFeedbackModel.tier)
            .setFeedBackRequestMsg(agentFeedbackModel.feedBackRequestMsg)
            .build()

        val futureResult = getAsyncStub().getAgentFeedback(agentFeedbackRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { _ ->
            callback?.invoke(Result.success(Unit))
        }.exceptionally {
            callback?.invoke(Result.failure(it))
            null
        }

    }

    fun getTransactionHistory(
        transactionsPerPage: Int, pageNumber: Int,
        callback: (Result<List<TransactionModel>>) -> Unit
    ) {
        val transactionsRequest = MasApi.GetTransactionsRequest
            .newBuilder().setTransactionsPerPage(transactionsPerPage).setStartPage(pageNumber)
            .build()

        val futureResult = getAsyncStub().getTransactions(transactionsRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            val txList = mutableListOf<TransactionModel>()
            result.transactionsList.forEach { transaction ->
                txList.add(
                    TransactionModel(
                        transaction.transactionNo, transaction.amount,
                        transaction.costOfGoodsSold, transaction.bonus,
                        transaction.transactionStarted, transaction.transactionEnded,
                        transaction.sourceMsisdn, transaction.recipientMsisdn,
                        transaction.balanceBefore, transaction.bonusBalanceBefore,
                        transaction.balanceAfter, transaction.bonusBalanceAfter,
                        transaction.status, transaction.followUpRequired,
                        transaction.rolledBack, transaction.messagesList,
                        transaction.itemDescription, transaction.commissionAmount,
                        TXType.valueOfOrUnknown(transaction.transactionType.name)
                    )
                )
            }
            callback(Result.success(txList))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun changePin(oldPin: String, newPin: String, callback: (Result<Unit>) -> Unit) {
        val currentPin = getAgentPin()
        if (oldPin != currentPin) {
            callback(Result.failure(Exception("INVALID_OLD_PIN")))
            return
        }

        val changePinRequest = MasApi.ChangePinRequest.newBuilder().setNewPin(newPin).build()

        val futureResult = getAsyncStub().changePin(changePinRequest)

        asCompletableFuture(futureResult).thenAcceptAsync {
            agentPin = newPin
            callback(Result.success(Unit))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun getAccountInfo(callback: (Result<AccountInfoResponseModel>) -> Unit) {
        val getAccountInfoRequest = MasApi.AgentId.newBuilder().setAgentId(agentId).build()

        val futureResult = getAsyncStub().getAccountInfo(getAccountInfoRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            // We are sure the TOKEN is valid, so that means the Agent PIN can be reused
            agentPinValidated = true

            /**
             * Quite likely we only need 'ACTIVE' here.
             * A user should not be able to login if they are in any other state
             */
            val accountState = when (result.state) {
                "A" -> AccountState.ACTIVE
                "D" -> AccountState.DEACTIVATED
                "S" -> AccountState.SUSPENDED
                "P" -> AccountState.PERMANENT
                else -> AccountState.UNKNOWN
            }

            callback(
                Result.success(
                    AccountInfoResponseModel(
                        result.accountNumber, result.msisdn,
                        result.title, result.firstName, result.initials, result.surname,
                        result.language.uppercase(),
                        result.altPhoneNumber, result.email,
                        accountState, result.activationDate, result.countryCode, result.tier
                    )
                )
            )
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun updateAccountInfo(
        accountInfo: AccountInfoResponseModel,
        callback: (Result<AccountInfoResponseModel>) -> Unit
    ) {
        val updateProfileRequest =
            MasApi.UpdateProfileRequest.newBuilder().setTitle(accountInfo.title)
                .setLanguage(accountInfo.language).setFirstName(accountInfo.firstName)
                .setSurname(accountInfo.surname).setEmail(accountInfo.email).setAgentId(agentId)
                .build()

        val futureResult = getAsyncStub().updateProfile(updateProfileRequest)

        asCompletableFuture(futureResult).thenAcceptAsync {
            callback(Result.success(accountInfo))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun getBalances(msisdn: String? = null, callback: (Result<BalancesResponseModel>) -> Unit) {
        var balanceRequestBuilder = MasApi.GetStockBalanceRequest.newBuilder()

        if (!msisdn.isNullOrEmpty())
            balanceRequestBuilder = balanceRequestBuilder.setMsisdn(msisdn)

        val balanceRequest = balanceRequestBuilder.build()

        val futureResult = getAsyncStub().getStockBalance(balanceRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            callback(
                Result.success(
                    BalancesResponseModel(
                        result.balance, result.bonusBalance, result.onHoldBalance,
                    )
                )
            )
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun mobileMoneyLogin(username: String, password: String, callback: (Result<Unit>) -> Unit) {
        val mobileMoneyLoginRequest =
            MasApi.MobileMoneyLoginRequest.newBuilder()
                .setUsername(username).setPassword(password).build()

        val futureResult = getAsyncStub().mobileMoneyLogin(mobileMoneyLoginRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            when (result.status) {
                MasApi.MobileMoneyResponseStatus.MM_SUCCESS -> {
                    loginToken = result.loginToken
                    callback(Result.success(Unit))
                }
                else -> callback(Result.failure(Throwable(result.status.toString())))
            }
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun mobileMoneyDeposit(
        amount: Double, recipient: String,
        callback: (Result<MobileMoneyDepositResponse>) -> Unit
    ) {
        val mobileMoneyDepositRequest =
            MasApi.MobileMoneyDepositRequest.newBuilder()
                .setAmount(amount.toString()).setDestinationMsisdn(recipient).build()

        val futureResult = getAsyncStub().mobileMoneyDeposit(mobileMoneyDepositRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            val status = when (result.status) {
                MasApi.MobileMoneyResponseStatus.MM_SUCCESS -> MobileMoneyDepositStatus.SUCCESS
                else -> {
                    callback(Result.failure(Throwable(result.status.toString())))
                    return@thenAcceptAsync
                }
            }
            callback(Result.success(MobileMoneyDepositResponse(status)))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun getMobileMoneyBalance(callback: (Result<MobileMoneyBalanceResponseModel>) -> Unit) {
        val futureResult = getAsyncStub().getMobileMoneyBalance(emptyRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            callback(Result.success(MobileMoneyBalanceResponseModel(result.mobileMoneyBalance)))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun buyAirtimeWithMobileMoney(amount: Double, callback: (Result<Unit>) -> Unit) {
        val buyAirtimeWithMobileMoneyRequest =
            BuyWithMobileMoneyRequest.newBuilder()
                .setMobileMoneyAmount(amount.toString()).build()

        val futureResult =
            getAsyncStub().buyAirtimeWithMobileMoney(buyAirtimeWithMobileMoneyRequest)

        asCompletableFuture(futureResult).thenAcceptAsync {
            callback(Result.success(Unit))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun setSalesTarget(
        salesTargetModel: SalesTargetModel,
        callback: (Result<SalesTargetModel>) -> Unit
    ) {
        val setSalesTargetRequest = MasApi.SetTeamMemberSalesTargetRequest.newBuilder()
            .setMsisdn(salesTargetModel.msisdn).setPeriod(salesTargetModel.period)
            .setTargetAmount(salesTargetModel.targetAmount)
            .build()

        val futureResult = getAsyncStub().setTeamMemberSalesTarget(setSalesTargetRequest)

        asCompletableFuture(futureResult).thenAcceptAsync {
            callback(Result.success(salesTargetModel))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }

    }

    fun getTeam(callback: (Result<TeamModel>) -> Unit) {
        val getTeamRequest = MasApi.NoParam.newBuilder().build()

        val futureResult = getAsyncStub().getTeam(getTeamRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            val teamMembers =
                result.membersList.map { masApiTeamMember ->
                    val salesTargetsIsNull = !masApiTeamMember.hasSalesTargets()
                    val salesTargets = when {
                        salesTargetsIsNull -> null
                        else -> SalesTargets(
                            masApiTeamMember.salesTargets?.dailyAmount?.makeEmptyNull(),
                            masApiTeamMember.salesTargets?.weeklyAmount?.makeEmptyNull(),
                            masApiTeamMember.salesTargets?.monthlyAmount?.makeEmptyNull()
                        )
                    }

                    TeamMemberModel(
                        masApiTeamMember.msisdn,
                        masApiTeamMember.firstName,
                        masApiTeamMember.surname,
                        BalancesResponseModel(
                            masApiTeamMember.stockBalance.balance,
                            masApiTeamMember.stockBalance.bonusBalance,
                            masApiTeamMember.stockBalance.onHoldBalance,
                        ),
                        salesTargets
                    )
                }

            callback(Result.success(TeamModel(result.membersCount, teamMembers)))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun getTeamMembership(callback: (Result<TeamMembership>) -> Unit) {
        val getTeamMembershipRequest = MasApi.NoParam.newBuilder().build()

        val futureResult = getAsyncStub().getTeamMembership(getTeamMembershipRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            val salesTargetsIsNull = !result.hasSalesTargets()
            val salesTargets = when {
                salesTargetsIsNull -> null
                else -> SalesTargets(
                    result.salesTargets?.dailyAmount,
                    result.salesTargets?.weeklyAmount,
                    result.salesTargets?.monthlyAmount
                )
            }

            callback(
                Result.success(
                    TeamMembership(result.memberMsisdn, result.teamLeadMsisdn, salesTargets)
                )
            )
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun isTeamLead(callback: (Result<Boolean>) -> Unit) {
        val futureResult = getAsyncStub().isTeamLead(emptyRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            callback(Result.success(result.isTeamLead))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun getSingleSalesSummary(
        startTime: Long, endTime: Long, msisdn: String? = null,
        callback: (Result<SalesSummaryModel>) -> Unit
    ) {
        var activitySummaryBuilder = MasApi.SalesSummaryRequest.newBuilder()

        if (!msisdn.isNullOrEmpty())
            activitySummaryBuilder = activitySummaryBuilder.setMsisdn(msisdn)

        val activitySummaryRequest =
            activitySummaryBuilder.setStartTime(startTime).setEndTime(endTime).build()

        val futureResult = getAsyncStub().getSalesSummary(activitySummaryRequest)

        asCompletableFuture(futureResult).thenAcceptAsync { result ->
            val salesSummary = SalesSummaryModel(
                result.startTime, result.endTime,
                SalesSummaryValue(
                    result.salesSummary.airtimeSalesValue,
                    result.salesSummary.airtimeSalesCount,
                    result.salesSummary.airtimeCostOfGoodsSold,
                    result.salesSummary.airtimeUnknownCostCount,

                    result.salesSummary.bundleSalesValue,
                    result.salesSummary.bundleSalesCount,
                    result.salesSummary.bundleCostOfGoodsSold,
                    result.salesSummary.bundleUnknownCostCount,
                    result.salesSummary.tradeBonusValue,
                    result.salesSummary.inboundTransfersValue,
                    result.salesSummary.inboundTransfersCount
                )
            )

            if (msisdn.isNullOrEmpty()) {
                callback(Result.success(salesSummary))
            } else {
                getBalances(msisdn) { balancesResult ->
                    balancesResult
                        .onFailure { callback(Result.failure(it)) }
                        .onSuccess {
                            salesSummary.value.stockLevel = it.balance
                            callback(Result.success(salesSummary))
                        }

                }
            }
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    fun getTeamSalesSummary(
        startTime: Long, endTime: Long,
        callback: (Result<SalesSummaryModel>) -> Unit
    ) {
        val activitySummaryRequest = MasApi.SalesSummaryRequest
            .newBuilder().setStartTime(startTime).setEndTime(endTime).build()

        val futureTeamStockBalanceResult = getAsyncStub().getTeamStockBalance(emptyRequest)
        val futureTeamSalesSummaryResult =
            getAsyncStub().getTeamSalesSummary(activitySummaryRequest)

        asCompletableFuture(futureTeamSalesSummaryResult)
            .thenAcceptAsync { teamSalesSummaryResult ->

                asCompletableFuture(futureTeamStockBalanceResult)
                    .thenApply { teamStock ->
                        callback(
                            Result.success(
                                SalesSummaryModel(
                                    teamSalesSummaryResult.startTime,
                                    teamSalesSummaryResult.endTime,
                                    SalesSummaryValue(
                                        teamSalesSummaryResult.salesSummary.airtimeSalesValue,
                                        teamSalesSummaryResult.salesSummary.airtimeSalesCount,
                                        teamSalesSummaryResult.salesSummary.airtimeCostOfGoodsSold,
                                        teamSalesSummaryResult.salesSummary.airtimeUnknownCostCount,

                                        teamSalesSummaryResult.salesSummary.bundleSalesValue,
                                        teamSalesSummaryResult.salesSummary.bundleSalesCount,
                                        teamSalesSummaryResult.salesSummary.bundleCostOfGoodsSold,
                                        teamSalesSummaryResult.salesSummary.bundleUnknownCostCount,
                                        teamSalesSummaryResult.salesSummary.tradeBonusValue,
                                        teamSalesSummaryResult.salesSummary.inboundTransfersValue,
                                        teamSalesSummaryResult.salesSummary.inboundTransfersCount,
                                        teamStock.balance
                                    )
                                )
                            )
                        )
                    }.exceptionally {
                        Log.e(_tag, "getTeamStock Failed: ${it.message}")
                        callback(Result.failure(it))
                    }
            }.exceptionally {
                Log.e(_tag, "getTeamSalesSummary Failed: ${it.message}")
                callback(Result.failure(it))
                null
            }
    }

    fun getGlobalHourlySalesSummary(
        startTime: Long, endTime: Long,
        callback: (Result<HourlySalesSummaryModel>) -> Unit
    ) {
        val salesSummaryRequest = MasApi.SalesSummaryRequest
            .newBuilder().setStartTime(startTime).setEndTime(endTime).build()

        val futureGlobalHourlySalesSummaryResult =
            getAsyncStub().getGlobalHourlySalesSummary(salesSummaryRequest)

        asCompletableFuture(futureGlobalHourlySalesSummaryResult)
            .thenAcceptAsync { hourlySalesSummaryResult ->
                try {
                    val hourlySalesSummaryValues = hourlySalesSummaryResult.salesSummaryList.map {
                        HourlySalesSummaryValue(
                            it.date, it.hour,
                            it.airtimeSalesValue, it.airtimeSalesCount,
                            it.bundleSalesValue, it.bundleSalesCount
                        )
                    }

                    callback(
                        Result.success(
                            HourlySalesSummaryModel(
                                hourlySalesSummaryResult.startTime,
                                hourlySalesSummaryResult.endTime,
                                hourlySalesSummaryValues
                            )
                        )
                    )
                } catch (e: Exception) {
                    callback(Result.failure(e))
                }
            }.exceptionally {
                Log.e(_tag, "getGlobalSalesSummary Failed: ${it.message}")
                callback(Result.failure(it))
                null
            }
    }

    fun getGlobalSalesSummary(
        startTime: Long, endTime: Long,
        callback: (Result<SalesSummaryModel>) -> Unit
    ) {
        val salesSummaryRequest = MasApi.SalesSummaryRequest
            .newBuilder().setStartTime(startTime).setEndTime(endTime).build()

        val futureGlobalSalesSummaryResult =
            getAsyncStub().getGlobalSalesSummary(salesSummaryRequest)

        asCompletableFuture(futureGlobalSalesSummaryResult)
            .thenAcceptAsync { salesSummaryResult ->
                callback(
                    Result.success(
                        SalesSummaryModel(
                            salesSummaryResult.startTime,
                            salesSummaryResult.endTime,
                            SalesSummaryValue(
                                salesSummaryResult.salesSummary.airtimeSalesValue,
                                salesSummaryResult.salesSummary.airtimeSalesCount,
                                salesSummaryResult.salesSummary.airtimeCostOfGoodsSold,
                                salesSummaryResult.salesSummary.airtimeUnknownCostCount,

                                salesSummaryResult.salesSummary.bundleSalesValue,
                                salesSummaryResult.salesSummary.bundleSalesCount,
                                salesSummaryResult.salesSummary.bundleCostOfGoodsSold,
                                salesSummaryResult.salesSummary.bundleUnknownCostCount,
                                salesSummaryResult.salesSummary.tradeBonusValue,
                                salesSummaryResult.salesSummary.inboundTransfersValue,
                                salesSummaryResult.salesSummary.inboundTransfersCount
                            )
                        )
                    )
                )
            }.exceptionally {
                Log.e(_tag, "getGlobalSalesSummary Failed: ${it.message}")
                callback(Result.failure(it))
                null
            }
    }

    fun submitAnalytics(
        version: String, events: List<AppAnalyticsService.AnalyticsEvent>,
        callback: (Result<Int>) -> Unit
    ) {
        val analyticsRequest = AnalyticsRequest.newBuilder().setAppVersion(version)

        analyticsRequest.addAllEvents(events.map { it.toMasApiAnalyticsEvent() })

        val futureResult = getAsyncStub().submitAnalytics(analyticsRequest.build())

        asCompletableFuture(futureResult).thenAcceptAsync {
            callback(Result.success(events.size))
        }.exceptionally {
            callback(Result.failure(it))
            null
        }
    }

    private fun <T> asCompletableFuture(listenableFuture: ListenableFuture<T>): CompletableFuture<T> {
        return CompletableFuture.supplyAsync { listenableFuture.get() }
    }
}
