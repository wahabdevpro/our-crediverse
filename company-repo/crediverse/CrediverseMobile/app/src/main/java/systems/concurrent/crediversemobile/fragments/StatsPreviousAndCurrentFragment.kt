package systems.concurrent.crediversemobile.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentStatsPreviousAndCurrentBinding
import systems.concurrent.crediversemobile.models.*
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.repositories.MasRepository.*
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.utils.Formatter.toStringWithPaddedDecimalPlaces
import systems.concurrent.crediversemobile.view_models.StatisticsViewModel
import java.util.concurrent.CompletableFuture

/**
 * A simple [Fragment] subclass.
 * Use the [StatsPreviousAndCurrentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatsPreviousAndCurrentFragment : ViewBindingFragment<FragmentStatsPreviousAndCurrentBinding>(
    FragmentStatsPreviousAndCurrentBinding::inflate
) {
    override val thisPage: NavigationManager.Page? = null

    private val _tag = this::class.java.kotlin.simpleName

    private val coroutineScope: CoroutineScope by lazy { MainScope() }

    private var salesSummaryRequests: List<SalesSummaryRequest> = listOf()

    private lateinit var masRepository: MasRepository

    private lateinit var statisticsViewModel: StatisticsViewModel

    private var getGeneralStatsFuture = CompletableFuture<CSResult<Unit>>()
    private var getTeamFuture = CompletableFuture<CSResult<Unit>>()

    private lateinit var listOfFutures: Array<CompletableFuture<CSResult<Unit>>>

    private var numberOfMembersWithWeeklySalesTarget = 0L
    private var totalTeamMembers = 0
    private var teamWeeklySalesTarget = 0.0
    private var teamWeeklySalesTargetAchieved = 0.0
    private var teamWeeklySalesTargetAchievedPercentage = 0.0

    private val currencyFormatParams = CurrencyFormatParams().useWithoutCurrencyPattern()

    private var currentAmount = SalesSummaryValue(
        "0", 0, "0", 0,
        "0", 0, "0", 0, "0", "0", 0
    )
    private var previousAmount = SalesSummaryValue(
        "0", 0, "0", 0,
        "0", 0, "0", 0, "0", "0", 0
    )

    private lateinit var fragmentStatsPeriod: SalesSummaryPeriod
    private val fragmentStatsType by lazy {
        salesSummaryRequests.getOrNull(0)?.type
    }


    private fun showError(errorText: String) {
        useBindingOrNull { binding ->
            if (binding == null) return@useBindingOrNull
            binding.statsError.text = errorText
            binding.statsError.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
            binding.mainContent.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        throwIfBadSalesRequest(salesSummaryRequests)

        fragmentStatsPeriod = salesSummaryRequests[0].salesSummaryPeriod

        val statisticsFragmentFactory =
            InjectorUtils.provideStatisticsFactory(requireContext())
        statisticsViewModel = ViewModelProvider(
            this, statisticsFragmentFactory
        )[StatisticsViewModel::class.java]

        StatisticsViewModel.resetLoadingStates()

        showProgressBar()

        masRepository = MasRepository(requireContext())
    }

    private fun showProgressBar() {
        NavigationManager.runOnNavigationUIThread {
            useBinding { binding ->
                binding.mainContent.visibility = View.GONE
                binding.statsError.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun throwIfBadSalesRequest(salesSummaryRequest: List<SalesSummaryRequest>) {
        if (salesSummaryRequest.size != 2) throw Exception("INVALID SALES REQUEST")
    }

    override fun onResume() {
        super.onResume()

        val shouldFetchData =
            statisticsViewModel.getLoadingState(fragmentStatsPeriod) ==
                    StatisticsViewModel.LoadState.NEW

        if (shouldFetchData) {
            showProgressBar()
        }

        coroutineScope.launch {
            if (shouldFetchData) triggerDataFetch()
            observeChanges()
        }
    }

    fun updateSalesSummaryRequests(salesSummaryRequests: List<SalesSummaryRequest>) {
        throwIfBadSalesRequest(salesSummaryRequests)
        this.salesSummaryRequests = salesSummaryRequests
    }

    var isOnlyGeneralStats = true

    private fun triggerDataFetch(callback: (() -> Unit)? = null) {
        showProgressBar()

        getGeneralStatsFuture = CompletableFuture<CSResult<Unit>>()

        if (isWeeklyTeamStatsSelected()) {
            isOnlyGeneralStats = false
            getTeamFuture = CompletableFuture<CSResult<Unit>>()
            listOfFutures = arrayOf(getGeneralStatsFuture, getTeamFuture)

            statisticsViewModel.getTeam { result ->
                result
                    .onFailure {
                        getTeamFuture.complete(CSResult.Failure(it))
                    }
                    .onSuccess { teamModel ->
                        numberOfMembersWithWeeklySalesTarget =
                            teamModel.team.sumOf { if (it.salesTarget?.weeklyAmount?.isNotEmpty() == true) 1L else 0L }

                        totalTeamMembers = teamModel.count

                        teamWeeklySalesTarget =
                            teamModel.team.sumOf {
                                if (it.salesTarget?.weeklyAmount?.isNotEmpty() == true)
                                    it.salesTarget?.weeklyAmount!!.toDoubleOrNull() ?: 0.0
                                else 0.0
                            }

                        val airTimeBalance =
                            currentAmount.airtimeSalesValue.toDoubleOrNull() ?: 0.0

                        val bundleSalesBalance =
                            currentAmount.bundleSalesValue.toDoubleOrNull() ?: 0.0

                        teamWeeklySalesTargetAchieved = bundleSalesBalance.plus(airTimeBalance)

                        teamWeeklySalesTargetAchievedPercentage =
                            teamWeeklySalesTargetAchieved.div(teamWeeklySalesTarget).times(100)

                        getTeamFuture.complete(CSResult.Success(Unit))
                    }
            }

        } else {
            isOnlyGeneralStats = true
            listOfFutures = arrayOf(getGeneralStatsFuture)
        }

        statisticsViewModel.updateFragmentLiveData(fragmentStatsPeriod, salesSummaryRequests) {
            callback?.invoke()
        }

        CompletableFuture.allOf(*listOfFutures).thenRun {
            val resultsArray = mutableListOf<CSResult<Unit>>(getGeneralStatsFuture.get())
            if (isWeeklyTeamStatsSelected()) {
                resultsArray.add(getTeamFuture.get())
            }

            var hasFailure = false
            resultsArray.forEach { result ->
                result.onFailure {
                    hasFailure = true
                    Log.e(_tag, "FAILED render: ${it.message.toString()}")
                    // TODO -- handle failure
                    val errorResource = ErrorMessages.getResourceOrDefault(it.message.toString())
                    showError(getString(errorResource))
                }
            }

            useBindingOrNull { binding ->
                if (binding == null) return@useBindingOrNull

                if (!hasFailure) {
                    binding.mainContent.visibility = View.VISIBLE
                    binding.statsError.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }

            renderView()
        }
    }

    private val observeGetSalesSummaryCallback: (CSResult<List<SalesSummaryModel>>) -> Unit =
        { results ->
            results.onSuccess { salesSummary ->
                salesSummary.forEachIndexed { index, salesSummaryModel ->
                    val intervalAge = SalesSummaryIntervalAge(index.toByte())

                    when (intervalAge.age.toInt()) {
                        0 -> {
                            currentAmount = SalesSummaryValue(
                                salesSummaryModel.value.airtimeSalesValue,
                                salesSummaryModel.value.airtimeSalesCount,
                                salesSummaryModel.value.airtimeCostOfGoodsSold,
                                salesSummaryModel.value.airtimeUnknownCostCount,

                                salesSummaryModel.value.bundleSalesValue,
                                salesSummaryModel.value.bundleSalesCount,
                                salesSummaryModel.value.bundleCostOfGoodsSold,
                                salesSummaryModel.value.bundleUnknownCostCount,
                                salesSummaryModel.value.tradeBonusValue,
                                salesSummaryModel.value.inboundTransfersValue,
                                salesSummaryModel.value.inboundTransfersCount,
                                salesSummaryModel.value.stockLevel
                            )
                        }

                        1 -> {
                            previousAmount = SalesSummaryValue(
                                salesSummaryModel.value.airtimeSalesValue,
                                salesSummaryModel.value.airtimeSalesCount,
                                salesSummaryModel.value.airtimeCostOfGoodsSold,
                                salesSummaryModel.value.airtimeUnknownCostCount,

                                salesSummaryModel.value.bundleSalesValue,
                                salesSummaryModel.value.bundleSalesCount,
                                salesSummaryModel.value.bundleCostOfGoodsSold,
                                salesSummaryModel.value.bundleUnknownCostCount,
                                salesSummaryModel.value.tradeBonusValue,
                                salesSummaryModel.value.inboundTransfersValue,
                                salesSummaryModel.value.inboundTransfersCount,
                                salesSummaryModel.value.stockLevel
                            )
                        }
                    }
                }

                getGeneralStatsFuture.complete(CSResult.Success(Unit))

                // RE-render if only general stats, because it can be called more than once
                if (isOnlyGeneralStats) renderView()

            }.onFailure { getGeneralStatsFuture.complete(CSResult.Failure(it)) }
        }

    private fun isWeeklyTeamStatsSelected(): Boolean {
        return fragmentStatsType == SalesSummaryType.TEAM
                && fragmentStatsPeriod == SalesSummaryPeriod.WEEKLY
    }

    private fun observeChanges() {
        statisticsViewModel.getFragmentLiveData(fragmentStatsPeriod)
            .removeObserver(observeGetSalesSummaryCallback)

        statisticsViewModel.getFragmentLiveData(fragmentStatsPeriod)
            .observe(viewLifecycleOwner, observeGetSalesSummaryCallback)
    }

    private fun renderView() {
        useBindingOrNull { binding ->
            if (binding == null) {
                Log.e(_tag, "binding null ?")
                return@useBindingOrNull
            }

            val drawableFont = DrawableFont(requireContext())

            val airtimeSalesCard = binding.airtimeSalesCard
            val bundleSalesCard = binding.bundleSalesCard
            val creditPurchasedCard = binding.creditPurchasedCard

            airtimeSalesCard.titleIcon.setImageDrawable(drawableFont.from(Icon.RETRO_MOBILE))
            bundleSalesCard.titleIcon.setImageDrawable(drawableFont.from(Icon.GIFT))
            creditPurchasedCard.titleIcon.setImageDrawable(drawableFont.from(Icon.WALLET))
            /**
             * All these TextView's should have the currency suffix
             */
            mapOf(
                airtimeSalesCard.titleText to R.string.airtime_sales_title,
                airtimeSalesCard.profitTitle to R.string.profit,
                bundleSalesCard.titleText to R.string.bundle_sales_title,
                bundleSalesCard.profitTitle to R.string.profit,
                creditPurchasedCard.titleText to R.string.credit_purchased_title,

                ).entries.forEach {
                it.key.text =
                    Formatter.formatWithSmallCurrencySuffix(getString(it.value).uppercase())
            }

            val currentDescription = when (fragmentStatsPeriod) {
                SalesSummaryPeriod.DAILY -> getString(R.string.sales_today)
                SalesSummaryPeriod.WEEKLY -> getString(R.string.sales_this_week)
                SalesSummaryPeriod.MONTHLY -> getString(R.string.sales_this_month)
            }
            val previousDescription = when (fragmentStatsPeriod) {
                SalesSummaryPeriod.DAILY -> getString(R.string.sales_yesterday)
                SalesSummaryPeriod.WEEKLY -> getString(R.string.sales_last_week)
                SalesSummaryPeriod.MONTHLY -> getString(R.string.sales_last_month)
            }

            binding.airtimeSalesCard.presentSummaryDescription.text = currentDescription
            binding.airtimeSalesCard.pastSummaryDescription.text = previousDescription

            binding.bundleSalesCard.presentSummaryDescription.text = currentDescription
            binding.bundleSalesCard.pastSummaryDescription.text = previousDescription

            binding.creditPurchasedCard.presentSummaryDescription.text = currentDescription
            binding.creditPurchasedCard.pastSummaryDescription.text = previousDescription
            /**
             * Airtime Sales
             */
            val airtimeCurrentAmount = currentAmount.airtimeSalesValue.toDouble()
            val airtimeCurrentCount = currentAmount.airtimeSalesCount

            val airtimePreviousAmount = previousAmount.airtimeSalesValue.toDouble()
            val airtimePreviousCount = previousAmount.airtimeSalesCount

            val airtimeCurrentProfit =
                airtimeCurrentAmount - currentAmount.airtimeCostOfGoodsSold.toDouble()
            val airtimePreviousProfit =
                airtimePreviousAmount - previousAmount.airtimeCostOfGoodsSold.toDouble()
            val airtimeTotalUnknownCostCount =
                currentAmount.airtimeUnknownCostCount + previousAmount.airtimeUnknownCostCount

            val airtimeCard = binding.airtimeSalesCard

            airtimeCard.presentSummaryAmount.text =
                Formatter.formatCustomCurrency(airtimeCurrentAmount, currencyFormatParams)
            airtimeCard.presentSummaryCount.text =
                "(" + getString(R.string.number_of_sales, airtimeCurrentCount.toString()) + ")"

            airtimeCard.pastSummaryAmount.text =
                Formatter.formatCustomCurrency(airtimePreviousAmount, currencyFormatParams)
            airtimeCard.pastSummaryCount.text =
                "(" + getString(R.string.number_of_sales, airtimePreviousCount.toString()) + ")"

            airtimeCard.pastProfitValue.text =
                airtimePreviousProfit.toStringWithPaddedDecimalPlaces(2)
            airtimeCard.presentProfitValue.text =
                airtimeCurrentProfit.toStringWithPaddedDecimalPlaces(2)

            if (airtimeTotalUnknownCostCount == 0L) {
                airtimeCard.unknownProfitDescription.visibility = View.GONE
            } else {
                @SuppressLint("SetTextI18n")
                val unknownCount = airtimeTotalUnknownCostCount.toString()
                airtimeCard.unknownProfitDescription.text =
                    when (airtimeTotalUnknownCostCount) {
                        1L -> getString(R.string.unknown_profit_suffix_singular, unknownCount)
                        else -> getString(R.string.unknown_profit_suffix_plural, unknownCount)
                    }
            }


            /**
             * Bundle Sales
             */
            val bundleCurrentAmount = currentAmount.bundleSalesValue.toDouble()
            val bundleCurrentCount = currentAmount.bundleSalesCount

            val bundlePreviousAmount = previousAmount.bundleSalesValue.toDouble()
            val bundlePreviousCount = previousAmount.bundleSalesCount

            val bundleCurrentProfit =
                bundleCurrentAmount - currentAmount.bundleCostOfGoodsSold.toDouble()
            val bundlePreviousProfit =
                bundlePreviousAmount - previousAmount.bundleCostOfGoodsSold.toDouble()
            val bundleTotalUnknownCostCount =
                currentAmount.bundleUnknownCostCount + previousAmount.bundleUnknownCostCount

            val bundleCard = binding.bundleSalesCard

            bundleCard.presentSummaryAmount.text =
                Formatter.formatCustomCurrency(bundleCurrentAmount, currencyFormatParams)
            bundleCard.presentSummaryCount.text =
                "(" + getString(R.string.number_of_sales, bundleCurrentCount.toString()) + ")"

            bundleCard.pastSummaryAmount.text =
                Formatter.formatCustomCurrency(bundlePreviousAmount, currencyFormatParams)
            bundleCard.pastSummaryCount.text =
                "(" + getString(R.string.number_of_sales, bundlePreviousCount.toString()) + ")"

            bundleCard.pastProfitValue.text =
                bundlePreviousProfit.toStringWithPaddedDecimalPlaces(2)
            bundleCard.presentProfitValue.text =
                bundleCurrentProfit.toStringWithPaddedDecimalPlaces(2)

            if (bundleTotalUnknownCostCount == 0L) {
                bundleCard.unknownProfitDescription.visibility = View.GONE
            } else {
                @SuppressLint("SetTextI18n")
                val unknownCount = bundleTotalUnknownCostCount.toString()
                bundleCard.unknownProfitDescription.text =
                    when (bundleTotalUnknownCostCount) {
                        1L -> getString(R.string.unknown_profit_suffix_singular, unknownCount)
                        else -> getString(R.string.unknown_profit_suffix_plural, unknownCount)
                    }
            }

            /**
             * TODO -- we are permanently hiding this for now ... not a feature toggle, just plain hidden
             */
            val creditPurchasedCurrentValue = currentAmount.inboundTransfersValue.toDouble()
            val creditPurchasedCurrentCount = currentAmount.inboundTransfersCount

            val creditPurchasedPreviousValue = previousAmount.inboundTransfersValue.toDouble()
            val creditPurchasedPreviousCount = previousAmount.inboundTransfersCount
            val tradeBonusCurrent = Formatter.formatCustomCurrency(
                currentAmount.tradeBonusValue.toDouble(),
                currencyFormatParams
            )
            val tradeBonusPrevious = Formatter.formatCustomCurrency(
                previousAmount.tradeBonusValue.toDouble(),
                currencyFormatParams
            )

            creditPurchasedCard.presentSummaryAmount.text =
                Formatter.formatCustomCurrency(creditPurchasedCurrentValue, currencyFormatParams)

            if (creditPurchasedCurrentValue == 0.0) {
                creditPurchasedCard.presentSummaryCount.visibility = View.GONE
            } else {
                creditPurchasedCard.presentExtraInfo.visibility = View.VISIBLE
                creditPurchasedCard.presentSummaryCount.text =
                    "(" + getString(
                        R.string.number_of_transfers,
                        creditPurchasedCurrentCount.toString()
                    ) + ")"
                creditPurchasedCard.presentExtraInfo.text =
                    Formatter.combine(
                        Formatter.bold("+"),
                        Formatter.fromHtml(
                            getString(
                                R.string.trade_bonus_amount,
                                tradeBonusCurrent
                            )
                        ),
                    )
            }

            creditPurchasedCard.pastSummaryAmount.text =
                Formatter.formatCustomCurrency(creditPurchasedPreviousValue, currencyFormatParams)

            if (creditPurchasedPreviousValue == 0.0) {
                creditPurchasedCard.pastSummaryCount.visibility = View.GONE
            } else {
                creditPurchasedCard.pastExtraInfo.visibility = View.VISIBLE
                creditPurchasedCard.pastSummaryCount.text =
                    "(" + getString(
                        R.string.number_of_transfers,
                        creditPurchasedPreviousCount.toString()
                    ) + ")"
                creditPurchasedCard.pastExtraInfo.text =
                    Formatter.combine(
                        Formatter.bold("+"),
                        Formatter.fromHtml(
                            getString(
                                R.string.trade_bonus_amount,
                                tradeBonusPrevious
                            )
                        ),
                    )
            }


            /**
             * Hide the profit blocks if feature toggle not enabled
             */
            if (!FeatureToggle.Stats.showCreditPurchased) {
                creditPurchasedCard.root.visibility = View.GONE
            }
            if (!FeatureToggle.Stats.showSalesProfit) {
                airtimeCard.profitBlock.visibility = View.GONE
                bundleCard.profitBlock.visibility = View.GONE
                creditPurchasedCard.profitBlock.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment
         * @return A new instance of fragment StatsDaily.
         */
        fun newInstance() = StatsPreviousAndCurrentFragment()
    }
}
