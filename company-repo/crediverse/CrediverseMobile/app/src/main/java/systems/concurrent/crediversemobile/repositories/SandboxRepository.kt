package systems.concurrent.crediversemobile.repositories

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.models.*
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.fiftyFiftyChance
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.kotlinRandom
import systems.concurrent.crediversemobile.utils.Formatter.getDatestampFromEpoch
import systems.concurrent.crediversemobile.utils.Formatter.toStringWithPaddedDecimalPlaces
import systems.concurrent.masapi.MasApi
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class SandboxRepository {
    fun login(agentMsisdn: String, agentPin: String): CSResult<LoginResponseModel> {
        SandboxRepository.agentMsisdn = agentMsisdn
        SandboxRepository.agentPin = agentPin

        if (agentMsisdn.endsWith("9")) {
            return getFailureResultFromError(MasRepository.ErrorMessages.AUTH)
        }

        if (agentMsisdn.endsWith("8")) {
            return getFailureResultFromError(MasRepository.ErrorMessages.SYNC_CLIENT_TIME)
        }

        return CSResult.Success(
            LoginResponseModel(
                "1", agentMsisdn, agentPin,
                "token-here", "refresh-token-here",
                AuthStatus.REQUIRE_OTP, "empty-message"
            )
        )
    }

    fun verifyOtp(otp: String, withToken: String? = null): CSResult<LoginResponseModel> {
        if (otp.endsWith("9")) {
            return getFailureResultFromError(MasRepository.ErrorMessages.INTERNAL_SERVER_ERROR)
        }

        return CSResult.Success(
            LoginResponseModel(
                "1", agentMsisdn, agentPin,
                withToken ?: "token-here", "refresh-token-here",
                AuthStatus.AUTHENTICATED, "empty-message"
            )
        )
    }

    fun getVersionStatus(): CSResult<VersionStatus> {
        val versionCode = AppFlag.System.versionCode
        var versionName = AppFlag.System.versionName

        if (fiftyFiftyChance()) {
            return CSResult.Success(VersionStatus(true, null, versionCode, versionName))
        }

        val isOk = chanceOfZero(returnMe = 1, percentChanceOfReturning = 70) == 1L
        val deprecationEpoch = CustomUtils.nowEpoch() + (86400 / 2)
        versionName = "5.6.0"
        val downloadUrl = "http://find.apk/here/my.apk"

        val versionStatus = VersionStatus(
            isOk, deprecationEpoch, versionCode + 2, versionName,
            VersionPriority.MEDIUM, downloadUrl
        )

        return CSResult.Success(versionStatus)
    }

    fun sell(txType: TXType, amount: Double, msisdn: String): CSResult<SellAirtimeResponse> {
        if (msisdn.endsWith("9")) {
            return getFailureResultFromError(MasRepository.ErrorMessages.INVALID_RECIPIENT)
        }

        if (msisdn.endsWith("5") && CustomUtils.fiftyFiftyChance()) {
            /**
             * Simulate UPGRADE REQUIRED logout failure
             */
            LogoutManager.forceLogout(R.string.logout_upgrade_required)
            return getFailureResultFromError(MasRepository.ErrorMessages.UPGRADE_REQUIRED)
        }


        var txNo = (transactionList.last().transactionNo.toInt() + 1).toString()
        val newBalance = accountBalances.balance.toDouble() - amount

        val hasInsufficientFunds = accountBalances.balance.toDouble() < amount
        val isFollowUp = amount.toInt().toString().endsWith("7")

        // pad the tx number
        while (txNo.length < 7) txNo = "0$txNo"

        val transaction =
            if (hasInsufficientFunds) {
                TransactionModel(
                    txNo, amount.toString(), null, "0", Date().time, Date().time,
                    agentMsisdn, msisdn,
                    accountBalances.balance, accountBalances.bonusBalance,
                    accountBalances.balance, accountBalances.bonusBalance,
                    TXStatus.INSUFFICIENT_FUNDS.toString(),
                    isFollowUp, rolledBack = false,
                    listOf(), null, null, txType
                )
            } else {
                accountBalances = BalancesResponseModel(
                    newBalance.toString(),
                    accountBalances.bonusBalance, accountBalances.onHoldBalance
                )
                TransactionModel(
                    txNo, amount.toString(),
                    (amount * kotlinRandom.nextDouble(0.8, 0.9)).toString(),
                    "", Date().time, Date().time, agentMsisdn, msisdn,
                    accountBalances.balance, accountBalances.bonusBalance,
                    newBalance.toString(), accountBalances.bonusBalance,
                    TXStatus.SUCCESS.toString(),
                    isFollowUp, rolledBack = false, listOf(), null,
                    (amount * kotlinRandom.nextDouble(0.1, 0.2)).toString(), txType
                )
            }

        transactionList.add(transaction)

        return if (hasInsufficientFunds) {
            getFailureResultFromError(MasRepository.ErrorMessages.INSUFFICIENT_FUNDS)
        } else {
            CSResult.Success(SellAirtimeResponse(isFollowUp))
        }
    }

    private val getTxHistoryFailAfterPages = 3
    private var getTxHistoryPageLoadProgress = 0

    fun getTransactionHistory(): CSResult<List<TransactionModel>> {
        return if (getTxHistoryFailAfterPages == getTxHistoryPageLoadProgress) {
            CSResult.Success(listOf())
        } else {
            getTxHistoryPageLoadProgress += 1
            CSResult.Success(transactionList)
        }
    }

    fun getAccountInfo(): CSResult<AccountInfoResponseModel> {
        return CSResult.Success(accountInfo)
    }

    fun updateAccountInfo(accountInfo: AccountInfoResponseModel): CSResult<AccountInfoResponseModel> {
        SandboxRepository.accountInfo = accountInfo
        return CSResult.Success(accountInfo)
    }

    fun setSalesTarget(salesTargetModel: SalesTargetModel): CSResult<SalesTargetModel> {
        if (salesTargetModel.targetAmount?.endsWith("9") == true) {
            return getFailureResultFromError(MasRepository.ErrorMessages.SET_TARGET_FAILED)
        }

        salesTarget = salesTargetModel
        return CSResult.Success(salesTargetModel)
    }

    private val haveBalanceToggleBetweenSuccessAndFailure = false

    fun getBalances(): CSResult<BalancesResponseModel> {
        return if (haveBalanceToggleBetweenSuccessAndFailure) {
            return if (balanceFails) {
                balanceFails = false
                getFailureResultFromError(MasRepository.ErrorMessages.INTERNAL_SERVER_ERROR)
            } else {
                balanceFails = true
                CSResult.Success(accountBalances)
            }
        } else CSResult.Success(accountBalances)
    }

    fun getMobileMoneyBalance(): CSResult<MobileMoneyBalanceResponseModel> {
        return CSResult.Success(mobileMoneyBalance)
    }

    fun buyCreditWithMobileMoney(amount: Double): CSResult<Unit> {
        return if (mobileMoneyBalance.balance.toDouble() < amount) {
            getFailureResultFromError(MasRepository.ErrorMessages.INSUFFICIENT_FUNDS)
        } else if (amount.roundToInt().toString().endsWith("9")) {
            getFailureResultFromError(MasRepository.ErrorMessages.INTERNAL_SERVER_ERROR)
        } else {
            accountBalances = BalancesResponseModel(
                (accountBalances.balance.toDouble() + amount).toString(),
                accountBalances.bonusBalance, accountBalances.onHoldBalance
            )

            mobileMoneyBalance =
                MobileMoneyBalanceResponseModel((mobileMoneyBalance.balance.toDouble() - amount).toString())
            CSResult.Success(Unit)
        }
    }

    fun getTeamMembership(): CSResult<TeamMembership> {
        return CSResult.Success(teamMember)
    }

    fun getTeam(): CSResult<TeamModel> {
        return CSResult.Success(team)
    }

    fun sellBundle(
        bundleSaleData: BundleService.BundleSaleData,
        chargeAmount: Double
    ): CSResult<BundleModel> {
        if (bundleSaleData.beneficiary.endsWith("8")) {
            return getFailureResultFromError(BundleRepository.ErrorMessages.REQUEST_ERROR, 999)
        } else if (bundleSaleData.beneficiary.endsWith("7")) {
            // return getFailureResultFromError(BundleRepository.ErrorMessages.PIN_INVALID)
            //
            // PIN_INVALID would return a very special error from within the Bundle Service ...
            //  so we are simulating that here...
            return CSResult.Failure(CSException("{\"error\":{\"message\": \"pin invalid\", \"code\": 140}}"))
        }

        val newBalance = accountBalances.balance.toDouble() - chargeAmount
        accountBalances = BalancesResponseModel(
            newBalance.toString(),
            accountBalances.bonusBalance, accountBalances.onHoldBalance
        )

        val bundle = BundleModel(
            bundleSaleData.bundleCode,
            bundleSaleData.bundleCode,
            listOf(""),
            listOf(Method(MethodCode.PROVISION.name, Charge(3000.0, "SDG"), null, null)),
            listOf(
                Benefit("airtime", "Airtime", 5, "min"),
                Benefit("d_data", "Day Data", 250 * 1024 * 1024, "bytes"),
                Benefit("n_data", "Nite Data", 250 * 1024 * 1024, "bytes"),
                Benefit("sms", "SMS", 25, "sms_"),
            ),
        )

        return CSResult.Success(bundle)
    }

    fun getBundles(msisdn: String): CSResult<SmartshopBundlesListGet> {
        if (msisdn.endsWith("9")) {
            return getFailureResultFromError(BundleRepository.ErrorMessages.LISTING_FAILED, 999)
        }

        val bundleCodes = listOf(
            "bundle_250M", "bundle_500M", "bundle_10min", "bundle_20min",
            "bundle_10G_mon", // "bundle_20G_mon", "bundle_50G_mon"
        )
        val bundleNames = listOf(
            "250M Day", "500M Day", "10 Minutes", "20 Minutes",
            "10G Month", "20G Month", "50G Month"
        )
        val methods = listOf(
            listOf(
                Method(MethodCode.PROVISION.name, Charge(300.0, "SDG"), null, null),
            ),
            listOf(
                Method(MethodCode.PROVISION.name, Charge(500.0, "SDG"), null, null),
            ),
            listOf(
                Method(MethodCode.PROVISION.name, Charge(1000.0, "SDG"), null, null),
            ),
            listOf(
                Method(MethodCode.PROVISION.name, Charge(2000.0, "SDG"), null, null),
            ),
            listOf(
                Method(MethodCode.EXTEND.name, Charge(45000.0, "SDG"), null, null),
                Method(MethodCode.CANCEL.name, Charge(15000.0, "SDG"), null, null),
            ),
            listOf(
                Method(MethodCode.PROVISION.name, Charge(22000.0, "SDG"), null, null),
                Method(MethodCode.SUBSCRIBE.name, Charge(22000.0, "SDG"), null, null),
            ),
            listOf(
                Method(MethodCode.PROVISION.name, Charge(22000.0, "SDG"), null, null),
                Method(MethodCode.SUBSCRIBE.name, Charge(22000.0, "SDG"), null, null),
            )
        )
        val benefits = listOf(
            listOf(
                Benefit("airtime", "Airtime", 5, "min"),
                Benefit("d_data", "Day Data", 250 * 1024 * 1024, "bytes"),
                Benefit("n_data", "Nite Data", 250 * 1024 * 1024, "bytes"),
                Benefit("sms", "SMS", 25, "sms_"),
            ),
            listOf(
                Benefit("airtime", "Airtime", 5, "min"),
                Benefit("d_data", "Day Data", 500 * 1024 * 1024, "bytes"),
                Benefit("n_data", "Nite Data", 500 * 1024 * 1024, "bytes"),
                Benefit("sms", "SMS", 50, "sms"),
            ),
            listOf(
                Benefit("airtime", "Airtime", 10, "min"),
                Benefit("sms", "SMS", 100, "sms"),
            ),
            listOf(
                Benefit("airtime", "Airtime", 20, "min"),
                Benefit("sms", "SMS", 200, "sms"),
            ),
            listOf(
                Benefit("airtime", "Airtime", 100, "min"),
                Benefit("d_data", "Day Data", 10 * 1024 * 1024 * 1024L, "bytes"),
                Benefit("n_data", "Nite Data", 10 * 1024 * 1024 * 1024L, "bytes"),
                Benefit("sms", "SMS", 1000, "sms"),
            ),
            listOf(
                Benefit("airtime", "Airtime", 200, "min"),
                Benefit("d_data", "Day Data", 20 * 1024 * 1024 * 1024L, "bytes"),
                Benefit("n_data", "Nite Data", 20 * 1024 * 1024 * 1024L, "bytes"),
                Benefit("sms", "SMS", 2000, "sms"),
            ),
            listOf(
                Benefit("airtime", "Airtime", 500, "min"),
                Benefit("d_data", "Day Data", 50 * 1024 * 1024 * 1024L, "bytes"),
                Benefit("n_data", "Nite Data", 50 * 1024 * 1024 * 1024L, "bytes"),
                Benefit("sms", "SMS", 5000, "sms"),
            ),
        )
        val bundleList = arrayListOf<BundleModel>()
        bundleCodes.forEachIndexed { index, code ->
            bundleList.add(
                BundleModel(
                    code,
                    bundleNames[index],
                    listOf(""),
                    methods[index],
                    benefits[index]
                )
            )
        }
        return CSResult.Success(
            SmartshopBundlesListGet(
                "trace-id-abf86fe876",
                listOf(
                    BundleCategory(
                        code = "all_network_bundles",
                        name = "All Network Bundles",
                        disallowed = false,
                        listOf("bundle_10G_mon"),
                        listOf(
                            BundleCategory(
                                code = "data_bundles",
                                name = "Data Bundles",
                                disallowed = false,
                                listOf(
                                    "bundle_250M",
                                    "bundle_500M",
                                ),
                                null
                            ),
                            BundleCategory(
                                code = "voice_bundles",
                                name = "Voice Bundles",
                                disallowed = false,
                                listOf(
                                    "bundle_10min",
                                    "bundle_20min",
                                ),
                                null
                            ),
                        )
                    )
                ),
                bundleList
            )
        )
    }

    fun getHourlySalesSummary(): CSResult<List<HourlySalesSummaryModel>> {
        var todayData = listOf(
            849954, 187800, 63850, 26950, 99100, 301429,
            4390649, 14145123, 24444552, 27277284, 29755881, 21747684,
            20442872, 19913596, 18090835,
            // no need for data for the rest of the day
            // 18215129, 19376840, 28387928,
            // 32797145, 35797155, 34797155, 24797155, 11797155, 2997155,
        )
        var yesterdayData = listOf(
            809954, 167800, 66850, 28950, 85100, 251429, 4090649, 15145123,
            23444552, 26277284, 25755881, 22747684, 19442872, 16913596,
            17090835, 17215129, 20376840, 26387928, 29797145, 34797155,
            32797155, 25797155, 12797155, 2497155,
        )

        /**
         * Sandbox, every 3 calls, graph is ZERO
         */
        if (hourlyGraphIsZeroCounter == 3) {
            todayData = listOf(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            )
            yesterdayData = todayData
            hourlyGraphIsZeroCounter = 0
        } else {
            hourlyGraphIsZeroCounter += 1
        }

        val mapIndex: (index: Int, value: Int) -> HourlySalesSummaryValue = { index, value ->
            HourlySalesSummaryValue(
                Date().time, (index + 1).toLong(),
                value.toString(), 0, "0", 0
            )
        }

        val hourlyYesterday = yesterdayData.mapIndexed(mapIndex)
        val hourlyToday = todayData.mapIndexed(mapIndex)


        return CSResult.Success(
            listOf(
                HourlySalesSummaryModel(
                    Date().time,
                    Date().time,
                    hourlyToday
                ),
                HourlySalesSummaryModel(
                    Date().time,
                    Date().time,
                    hourlyYesterday
                )
            )
        )
    }

    fun getSalesSummary(
        context: Context,
        salesSummaryRequest: List<MasRepository.SalesSummaryRequest>,
    ): CSResult<List<SalesSummaryModel>> {

        var multiplier = 1.0
        var withStock = false
        val withDelay = 100L
        val withCache = true

        when (salesSummaryRequest[0].type) {
            MasRepository.SalesSummaryType.MEMBER -> withStock = true
            MasRepository.SalesSummaryType.TEAM -> {
                multiplier = 7.0
                withStock = true
            }

            MasRepository.SalesSummaryType.EXEC -> {
                // for "global" sales, they are quite large, so we want to simulate a large number
                multiplier *= (111 * (111 * randomDouble()))
            }

            else -> {}
        }

        val stockAmount = when (withStock) {
            false -> null
            true -> (50 + (airtimeAmountYesterday * randomDouble() * multiplier)).toString()
        }
        val tradeBonus =
            (50 + (inboundTransfersValueToday * randomDouble() * multiplier)).roundToLong()
        val results = when (salesSummaryRequest[0].salesSummaryPeriod) {
            MasRepository.SalesSummaryPeriod.DAILY -> {
                salesSummaryRequest.map {
                    if (it.salesSummaryIntervalAge.age.toInt() == 0) {
                        SalesSummaryValue(
                            (airtimeAmountToday * multiplier * getVariance(20)).toString(),
                            (airtimeCountToday * multiplier * getVariance(20)).roundToLong(),
                            (airtimeCostOfGoodsSoldToday * multiplier * getVariance(10)).toString(),
                            chanceOfZero((airtimeUnknownCostToday * multiplier).roundToLong()),

                            (bundleAmountToday * multiplier * getVariance(20)).toString(),
                            (bundleCountToday * multiplier * getVariance(20)).roundToLong(),
                            (bundleCostOfGoodsSoldToday * multiplier * getVariance(10)).toString(),
                            chanceOfZero((bundleUnknownCostToday * multiplier).roundToLong()),
                            tradeBonus.toString(),
                            (inboundTransfersValueToday * multiplier * getVariance(20)).toString(),
                            (inboundTransfersCountToday * multiplier * getVariance(20)).roundToLong(),
                            stockAmount
                        )
                    } else {
                        SalesSummaryValue(
                            (airtimeAmountYesterday * multiplier * getVariance(20)).toString(),
                            (airtimeCountYesterday * multiplier * getVariance(20)).roundToLong(),
                            (airtimeCostOfGoodsSoldYesterday * multiplier * getVariance(10)).toString(),
                            chanceOfZero((airtimeUnknownCostYesterday * multiplier).roundToLong()),

                            (bundleAmountYesterday * multiplier * getVariance(20)).toString(),
                            (bundleCountYesterday * multiplier * getVariance(20)).roundToLong(),
                            (bundleCostOfGoodsSoldYesterday * multiplier * getVariance(10)).toString(),
                            chanceOfZero((bundleUnknownCostYesterday * multiplier).roundToLong()),
                            tradeBonus.toString(),
                            (inboundTransfersValueYesterday * multiplier * getVariance(20)).toString(),
                            (inboundTransfersCountYesterday * multiplier * getVariance(20)).roundToLong(),
                            stockAmount
                        )
                    }
                }
            }

            MasRepository.SalesSummaryPeriod.WEEKLY -> {
                salesSummaryRequest.map {
                    if (it.salesSummaryIntervalAge.age.toInt() == 0) {
                        SalesSummaryValue(
                            (airtimeAmountToday * multiplier * 3 * getVariance(20)).toString(),
                            (airtimeCountToday * multiplier * 3 * getVariance(20)).roundToLong(),
                            (airtimeCostOfGoodsSoldToday * multiplier * 3 * getVariance(10)).toString(),
                            chanceOfZero((airtimeUnknownCostToday * multiplier * 3).roundToLong()),

                            (bundleAmountToday * multiplier * 3 * getVariance(20)).toString(),
                            (bundleCountToday * multiplier * 3 * getVariance(20)).roundToLong(),
                            (bundleCostOfGoodsSoldToday * multiplier * 3 * getVariance(10)).toString(),
                            chanceOfZero((bundleUnknownCostToday * multiplier * 3).roundToLong()),
                            tradeBonus.toString(),
                            (inboundTransfersValueToday * multiplier * getVariance(20)).toString(),
                            (inboundTransfersCountToday * multiplier * getVariance(20)).roundToLong(),
                            stockAmount
                        )
                    } else {
                        SalesSummaryValue(
                            (airtimeAmountYesterday * multiplier * 7 * getVariance(20)).toString(),
                            (airtimeCountYesterday * multiplier * 7 * getVariance(20)).roundToLong(),
                            (airtimeCostOfGoodsSoldYesterday * multiplier * 7 * getVariance(10)).toString(),
                            chanceOfZero((airtimeUnknownCostYesterday * multiplier * 7).roundToLong()),

                            (bundleAmountYesterday * multiplier * 7 * getVariance(20)).toString(),
                            (bundleCountYesterday * multiplier * 7 * getVariance(20)).roundToLong(),
                            (bundleCostOfGoodsSoldYesterday * multiplier * 7 * getVariance(10)).toString(),
                            chanceOfZero((bundleUnknownCostYesterday * multiplier * 7).roundToLong()),
                            tradeBonus.toString(),
                            (inboundTransfersValueYesterday * multiplier * getVariance(20)).toString(),
                            (inboundTransfersCountYesterday * multiplier * getVariance(20)).roundToLong(),
                            stockAmount
                        )
                    }
                }
            }

            MasRepository.SalesSummaryPeriod.MONTHLY -> {
                salesSummaryRequest.map {
                    if (it.salesSummaryIntervalAge.age.toInt() == 0) {
                        SalesSummaryValue(
                            (airtimeAmountToday * multiplier * 9 * getVariance(20)).toString(),
                            (airtimeCountToday * multiplier * 9 * getVariance(20)).roundToLong(),
                            (airtimeCostOfGoodsSoldToday * multiplier * 9).toString(),
                            chanceOfZero((airtimeUnknownCostToday * multiplier * 9).roundToLong()),

                            (bundleAmountToday * multiplier * 9 * getVariance(20)).toString(),
                            (bundleCountToday * multiplier * 9 * getVariance(20)).roundToLong(),
                            (bundleCostOfGoodsSoldToday * multiplier * 9 * getVariance(10)).toString(),
                            chanceOfZero((bundleUnknownCostToday * multiplier * 9).roundToLong()),
                            tradeBonus.toString(),
                            (inboundTransfersValueToday * multiplier * getVariance(20)).toString(),
                            (inboundTransfersCountToday * multiplier * getVariance(20)).roundToLong(),
                            stockAmount
                        )
                    } else {
                        SalesSummaryValue(
                            (airtimeAmountYesterday * multiplier * 30 * getVariance(20)).toString(),
                            (airtimeCountYesterday * multiplier * 30 * getVariance(20)).roundToLong(),
                            (airtimeCostOfGoodsSoldYesterday * multiplier * 30 * getVariance(10)).toString(),
                            chanceOfZero((airtimeUnknownCostYesterday * multiplier * 30).roundToLong()),

                            (bundleAmountYesterday * multiplier * 30 * getVariance(20)).toString(),
                            (bundleCountYesterday * multiplier * 30 * getVariance(20)).roundToLong(),
                            (bundleCostOfGoodsSoldYesterday * multiplier * 30 * getVariance(10)).toString(),
                            chanceOfZero((bundleUnknownCostYesterday * multiplier * 30).roundToLong()),
                            tradeBonus.toString(),
                            (inboundTransfersValueYesterday * multiplier * getVariance(20)).toString(),
                            (inboundTransfersCountYesterday * multiplier * getVariance(20)).roundToLong(),
                            stockAmount
                        )
                    }
                }
            }
        }

        val requestPeriods = salesSummaryRequest.map {
            MasRepository.getSalesPeriods(it.salesSummaryPeriod, it.salesSummaryIntervalAge)
        }

        val salesSummaryModels: MutableList<SalesSummaryModel> = mutableListOf()

        val cacheService = CacheService(context)

        runBlocking {
            val deferredDays = requestPeriods.mapIndexed requestPeriods@{ index, periods ->
                async {
                    periods.map { salesPeriod ->
                        val isToday = MasRepository.isToday(salesPeriod.startTime)

                        if (!isToday && withCache) {
                            var cacheSaveName =
                                getDatestampFromEpoch(salesPeriod.startTime) + "_" + salesSummaryRequest[0].type
                            cacheSaveName += salesSummaryRequest[0].msisdn ?: ""

                            val cachedValue =
                                cacheService.getOrNull<SalesSummaryValue>(cacheSaveName)

                            if (cachedValue != null) {
                                return@map async {
                                    SalesSummaryModel(
                                        salesPeriod.startTime, salesPeriod.endTime,
                                        cachedValue
                                    )
                                }
                            }
                        }

                        async {
                            delay(withDelay)
                            val finalResult = SalesSummaryModel(
                                salesPeriod.startTime, salesPeriod.endTime,
                                results[index]
                            )

                            var cacheSaveName =
                                getDatestampFromEpoch(salesPeriod.startTime) + "_" + salesSummaryRequest[0].type
                            cacheSaveName += salesSummaryRequest[0].msisdn ?: ""

                            if (!MasRepository.isToday(salesPeriod.startTime) && withCache) {
                                cacheService.save(
                                    results[index], CacheService.lifetimeDays(days = 95),
                                    cacheSaveName
                                )
                            }

                            finalResult
                        }
                    }.awaitAll()
                }
            }
            val resultDays = deferredDays.awaitAll()

            resultDays.forEachIndexed { index, days ->
                try {
                    val sumOfAirtime =
                        days.sumOf { it.value.airtimeSalesValue.toDoubleOrNull() ?: 0.0 }
                    val sumOfAirtimeCostOfGoodsSold = days.sumOf {
                        it.value.airtimeCostOfGoodsSold.toDoubleOrNull() ?: 0.0
                    }
                    val sumOfBundles =
                        days.sumOf { it.value.bundleSalesValue.toDoubleOrNull() ?: 0.0 }
                    val sumOfBundleCostOfGoodsSold = days.sumOf {
                        it.value.bundleCostOfGoodsSold.toDoubleOrNull() ?: 0.0
                    }

                    val countOfAirtime = days.sumOf { it.value.airtimeSalesCount }
                    val countOfAirtimeUnknownCost = days.sumOf { it.value.airtimeUnknownCostCount }
                    val countOfBundles = days.sumOf { it.value.bundleSalesCount }
                    val countOfBundleUnknownCost = days.sumOf { it.value.bundleUnknownCostCount }
                    val sumOfTradeBonus =
                        days.sumOf { it.value.tradeBonusValue.toDoubleOrNull() ?: 0.0 }
                    val sumOfInboundTransfersValue = days.sumOf {
                        it.value.inboundTransfersValue.toDoubleOrNull() ?: 0.0
                    }
                    val countOfInboundTransfersCount = days.sumOf { it.value.inboundTransfersCount }

                    var sumOfStock: Double? =
                        days.sumOf { it.value.stockLevel?.toDoubleOrNull() ?: 0.0 }
                    if (sumOfStock == 0.0) sumOfStock = null

                    salesSummaryModels.add(
                        index,
                        SalesSummaryModel(
                            days.first().startTime,
                            days.last().endTime,
                            SalesSummaryValue(
                                sumOfAirtime.toString(),
                                countOfAirtime,
                                sumOfAirtimeCostOfGoodsSold.toString(),
                                countOfAirtimeUnknownCost,
                                sumOfBundles.toString(),
                                countOfBundles,
                                sumOfBundleCostOfGoodsSold.toString(),
                                countOfBundleUnknownCost,
                                sumOfTradeBonus.toString(),
                                sumOfInboundTransfersValue.toString(),
                                countOfInboundTransfersCount,
                                sumOfStock?.toString()
                            )
                        )
                    )
                } catch (e: Exception) {
                    /**
                     * If this block of code is reached ... then we must reset the phone Cache
                     */
                    NavigationManager.runOnNavigationUIThread {
                        Dialog(
                            it, DialogType.ERROR, "", it.getString(R.string.cache_fault),
                            DialogOptions().setCancellable(false)
                        ).onConfirm { /* Close the Navigation activity */ it.finish() }.show()
                    }
                }
            }
        }


        return CSResult.Success(salesSummaryModels)
    }

    companion object {
        var balanceFails = false

        var hourlyGraphIsZeroCounter = 1

        private val _tag = this::class.java.kotlin.simpleName

        private var accountInfo = AccountInfoResponseModel(
            "555 999 333", "55558888", "Mr",
            "John", "", "Smith",
            "EN", "", "j.smith@example.com",
            //"FR", "", "j.smith@example.com",
            AccountState.ACTIVE, 0, "USD",
            "eCabine"
        )

        private var salesTarget = SalesTargetModel(
            "55558888", MasApi.Period.WEEK, "20000"
        )

        // starts between 500 and 1000 -- subsequently, adjusts based on Dummy actions
        private var _accountBalances = BalancesResponseModel(
            (400 + kotlinRandom.nextInt(100, 501)).toStringWithPaddedDecimalPlaces(4),
            nonZeroDoubleBetween(60, 130).toStringWithPaddedDecimalPlaces(4),
            chanceOfZero(30, percentChanceOfReturning = 50).toStringWithPaddedDecimalPlaces(4)
        )

        private const val autoIncrementBalanceOnRefresh = false

        private var accountBalances: BalancesResponseModel
            // RUNNING UPDATE BY +5 WHENEVER WE GET IT
            get() {
                val incrementAmount =
                    if (autoIncrementBalanceOnRefresh) 5
                    else 0

                _accountBalances = BalancesResponseModel(
                    (_accountBalances.balance.toDouble() + incrementAmount)
                        .toStringWithPaddedDecimalPlaces(4),
                    _accountBalances.bonusBalance, _accountBalances.onHoldBalance
                )
                return _accountBalances
            }
            set(value) {
                _accountBalances = value
            }

        private val salesTargets
            get() = SalesTargets(
                kotlinRandom.nextInt(3000, 5501).toString(),
                kotlinRandom.nextInt(30000, 55001).toString(),
                kotlinRandom.nextInt(300000, 550001).toString()
            )

        private var agentMsisdn = ""
        private var agentPin = ""

        @OptIn(DelicateCoroutinesApi::class)
        fun delayedCallback(milliseconds: Long? = null, callback: () -> Unit) {
            // vary the delay between 100 and 500 ms
            val delayMs = milliseconds ?: kotlinRandom.nextLong(100, 500)

            GlobalScope.launch(Dispatchers.Main) {
                delay(delayMs)
                callback()
            }
        }

        private val nowEpoch get() = Date().time / 1000

        private var transactionList = mutableListOf(
            TransactionModel(
                "0000001", "182.0000",
                (182 * kotlinRandom.nextDouble(0.8, 0.9)).toStringWithPaddedDecimalPlaces(4),
                "0", nowEpoch,
                nowEpoch, "0101948116", "0843334444",
                "200", "100",
                "382", "100",
                "SUCCESS", followUpRequired = false,
                rolledBack = false, listOf("message1", "message2"), "My Kewl Bundle",
                (182 * kotlinRandom.nextDouble(0.1, 0.2)).toStringWithPaddedDecimalPlaces(4),
                TXType.NON_AIRTIME_DEBIT
            ),
            TransactionModel(
                "0000002", "402.0000",
                (402 * kotlinRandom.nextDouble(0.8, 0.9)).toStringWithPaddedDecimalPlaces(4),
                "0", nowEpoch,
                nowEpoch, "0101948116", "0843334444",
                "600", "100",
                "198", "100",
                "SUCCESS", followUpRequired = false,
                rolledBack = false, listOf("message1", "message2"), "My Giant Bundle",
                (402 * kotlinRandom.nextDouble(0.1, 0.2)).toStringWithPaddedDecimalPlaces(4),
                TXType.NON_AIRTIME_REFUND
            ),
            TransactionModel(
                "0000003", "100.0000",
                (100 * kotlinRandom.nextDouble(0.8, 0.9)).toStringWithPaddedDecimalPlaces(4),
                "0", nowEpoch,
                nowEpoch, "0843334444", "0101948116",
                "200", "100",
                "300", "100",
                "SUCCESS", followUpRequired = false,
                rolledBack = false, listOf("message1", "message2"),
                null, null, TXType.TRANSFER
            ),
            TransactionModel(
                "0000004", "200.0000", null,
                "0", (nowEpoch - (24 * 3600)),
                (nowEpoch - (24 * 3600)), "0843334444", "0101948116",
                "200", "100",
                "200", "100",
                "INSUFFICIENT_FUNDS", followUpRequired = false,
                rolledBack = false, listOf("message1", "message2"),
                null, null, TXType.TRANSFER
            ),
            TransactionModel(
                "0000005", "50.0000",
                (50 * kotlinRandom.nextDouble(0.8, 0.9)).toStringWithPaddedDecimalPlaces(4),
                "0", (nowEpoch - (24 * 3600)),
                (nowEpoch - (24 * 3600)), "0101948116", "0843334444",
                "200", "100",
                "150", "100",
                "SUCCESS", followUpRequired = false,
                rolledBack = false, listOf("message1", "message2"),
                null, null, TXType.SELL
            ),
            TransactionModel(
                "0000006", "-200.0000", null,
                "0", (nowEpoch - (24 * 3600)),
                (nowEpoch - (24 * 3600)), "0101948116", "123456789",
                "200", "100",
                "400", "100",
                "INSUFFICIENT_FUNDS", followUpRequired = false,
                rolledBack = false, listOf("message1", "message2"),
                null, null, TXType.TRANSFER
            ),
            TransactionModel(
                "0000007", "200.0000",
                (200 * kotlinRandom.nextDouble(0.8, 0.9)).toStringWithPaddedDecimalPlaces(4),
                "0", (nowEpoch - (48 * 3600)),
                (nowEpoch - (48 * 3600)), "0101948116", "123456789",
                "300", "100",
                "100", "100",
                "SUCCESS", followUpRequired = true,
                rolledBack = false, listOf("message1", "message2"),
                null, null, TXType.SELL
            )
        )

        private var mobileMoneyBalance = MobileMoneyBalanceResponseModel("500600")

        private fun generateRandomName(): String {
            val vowels = listOf("a", "e", "i", "o", "u")
            val consonants = listOf(
                "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n",
                "p", "q", "r", "s", "t", "v", "w", "x", "y", "z"
            )

            var name = ""
            // random name length
            for (i in 1..(4..8).random()) {
                name += if (i % 2 == 0) vowels.random() else consonants.random()
            }

            return name.replaceFirstChar { it.uppercase() }
        }

        private val team
            get() = run {
                val teamMembers = mutableListOf<TeamMemberModel>()

                val msisdns: List<String> =
                    (1..20).map { (0..999999999).random().toString().padStart(9, '0') }

                val firstNames: List<String> = (1..20).map { generateRandomName() }
                val surnamesNames: List<String> = (1..20).map { generateRandomName() }

                (0..19).forEach { i ->
                    val accountBalance = BalancesResponseModel(
                        (600 + kotlinRandom.nextInt(-130, 50010)).toString(), "0", "0"
                    )
                    teamMembers.add(
                        TeamMemberModel(
                            msisdns[i], firstNames[i], surnamesNames[i], accountBalance,
                            if (i % 2 == 0) salesTargets else SalesTargets(null, null, null)
                        )
                    )
                }

                TeamModel(teamMembers.size, teamMembers)
            }

        val target: SalesTargets = SalesTargets("", "80000", "")
        val teamMember: TeamMembership get() = TeamMembership("88887777", "77778888", target)

        // between 0.0 (inc) and 1.0 (exc)
        fun randomDouble(): Double {
            return kotlinRandom.nextDouble()
        }

        private fun nonZeroDoubleBetween(a: Int, b: Int): Double {
            /**
             * multiplying and dividing by 100 adds 2 decimals to the result
             */
            val first = a * 100
            val second = b * 100

            val final = kotlinRandom.nextInt(first, second).toDouble() / 100.0

            return if (final == 0.0) 1.0 else final
        }

        private fun chanceOfZero(returnMe: Long, percentChanceOfReturning: Int = 30): Long {
            val randomFraction = kotlinRandom.nextDouble()
            val percent = percentChanceOfReturning.toDouble() / 100
            val chanceReached = randomFraction < percent

            return when {
                chanceReached -> returnMe
                else -> 0
            }
        }

        private fun nonZeroLongBetween(a: Int, b: Int): Long {
            return nonZeroDoubleBetween(a, b).toLong()
        }

        private fun getVariance(variance: Int): Double {
            return kotlinRandom.nextDouble(
                1.0 - variance.toDouble() / 100,
                1.0 + variance.toDouble() / 100
            )
        }

        private val airtimeAmountToday = 1320.87 + nonZeroDoubleBetween(100, 300)
        private val airtimeCountToday = 132L + nonZeroLongBetween(10, 30)
        private val airtimeCostOfGoodsSoldToday =
            airtimeAmountToday * kotlinRandom.nextDouble(0.6, 0.7)
        private var airtimeUnknownCostToday = kotlinRandom.nextInt(0, 10)

        private val airtimeAmountYesterday = 2530.24 + nonZeroDoubleBetween(300, 600)
        private val airtimeCountYesterday = 253L + nonZeroLongBetween(30, 50)
        private val airtimeCostOfGoodsSoldYesterday =
            airtimeAmountYesterday * kotlinRandom.nextDouble(0.6, 0.7)
        private var airtimeUnknownCostYesterday = kotlinRandom.nextInt(0, 10)

        private val bundleAmountToday = 850.82 + nonZeroDoubleBetween(50, 150)
        private val bundleCountToday = 8L + nonZeroLongBetween(3, 5)
        private val bundleCostOfGoodsSoldToday =
            bundleAmountToday * kotlinRandom.nextDouble(0.6, 0.7)
        private var bundleUnknownCostToday = kotlinRandom.nextInt(0, 10)

        private val bundleAmountYesterday = 780.11 + nonZeroDoubleBetween(-150, 100)
        private val bundleCountYesterday = 7L + nonZeroLongBetween(-2, 2)
        private val bundleCostOfGoodsSoldYesterday =
            bundleAmountYesterday * kotlinRandom.nextDouble(0.6, 0.7)
        private var bundleUnknownCostYesterday = kotlinRandom.nextInt(0, 10)
        private val inboundTransfersValueToday = 850.82 + nonZeroDoubleBetween(50, 150)
        private val inboundTransfersValueYesterday = 780.11 + nonZeroDoubleBetween(-150, 100)
        private val inboundTransfersCountToday = 132L + nonZeroLongBetween(10, 30)
        private val inboundTransfersCountYesterday = 253L + nonZeroLongBetween(30, 50)

        private fun logSandboxError() {
            val e = java.lang.Exception("for getting the stack")

            val stackItem = e.stackTrace[2]

            val classNameParts = stackItem.className.split(".")
            val className = classNameParts[classNameParts.size - 1]
            val methodName = stackItem.methodName.replace(Regex("-.*"), "")
            val lineNumber = stackItem.lineNumber
            Log.e(
                _tag,
                "Simulated Sandbox error, found here: \n        -> " +
                        "$className->$methodName( ... ):$lineNumber"
            )
        }

        fun getFailureResultFromError(
            error: BundleRepository.ErrorMessages, code: Int
        ): CSResult<Nothing> {
            logSandboxError()
            return CSResult.Failure(CSException(error, code))
        }

        fun getFailureResultFromError(error: MasRepository.ErrorMessages): CSResult<Nothing> {
            logSandboxError()
            return CSResult.Failure(CSException(error))
        }

        /** ******************
         ** ******************
         ** ******************
         *
         *   WARNING ::: DO NOT commit any of these in GITHUB as 'true'
         *               They are used for Sandbox Testing only
         *
         ** *************** **
         ** *************** **
         ** *************** **/
        //
        private const val sandboxEnabledWhileDeveloping = false
        private const val ENABLE_AUTO_LOGIN = false

        /**/// Either the Build configuration (via AppFlag class) enables sandbox OR we enabled it above while developing
        val isSandboxEnabled get() = AppFlag.Sandbox.sandboxEnabled || sandboxEnabledWhileDeveloping

        // just a helper function --- 'sandboxEnabled' must be true before auto-login will work
        fun sandboxAutoLoginEnabled(): Boolean {
            return isSandboxEnabled && (AppFlag.Sandbox.sandboxAutoLogin || ENABLE_AUTO_LOGIN)
        }
        /** ***************** **/
        /** ***************** **/
    }
}
