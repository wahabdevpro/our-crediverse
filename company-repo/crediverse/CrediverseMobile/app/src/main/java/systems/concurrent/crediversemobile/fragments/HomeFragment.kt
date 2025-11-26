package systems.concurrent.crediversemobile.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lecho.lib.hellocharts.view.ColumnChartView
import lecho.lib.hellocharts.view.PieChartView
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentHomeBinding
import systems.concurrent.crediversemobile.models.BalancesResponseModel
import systems.concurrent.crediversemobile.models.CurrencyFormatParams
import systems.concurrent.crediversemobile.models.SalesSummaryModel
import systems.concurrent.crediversemobile.models.TeamMembership
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.services.AndroidDataService
import systems.concurrent.crediversemobile.services.InjectorUtils
import systems.concurrent.crediversemobile.services.Toaster
import systems.concurrent.crediversemobile.services.ToasterType
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.utils.Formatter.makeZeroEmpty
import systems.concurrent.crediversemobile.utils.Formatter.stripDecimalsOrNull
import systems.concurrent.crediversemobile.utils.Formatter.toStringWithPaddedDecimalPlaces
import systems.concurrent.crediversemobile.view_models.AccountBalancesViewModel
import systems.concurrent.crediversemobile.view_models.AccountInfoViewModel
import systems.concurrent.crediversemobile.view_models.StatisticsViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.CompletableFuture

/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home : ViewBindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    // This is updated manually in our local `onResume` method
    override val thisPage: NavigationManager.Page? = null

    private lateinit var masRepository: MasRepository

    private lateinit var accountInfoViewModel: AccountInfoViewModel
    private lateinit var accountBalancesViewModel: AccountBalancesViewModel
    private lateinit var statisticsViewModel: StatisticsViewModel
    private val androidStore by lazy { AndroidDataService(requireContext()) }

    private val defaultBalanceVisibilityState = false

    // 13 days ... going backward from YESTERDAY (inclusive)
    private val lastTwoWeeksSalesSummaryRequests = (13 downTo 1).map {
        MasRepository.SalesSummaryRequest(
            MasRepository.SalesSummaryType.SELF,
            MasRepository.SalesSummaryPeriod.DAILY,
            MasRepository.SalesSummaryIntervalAge(it.toByte()),
        )
    }

    // TODAY
    private val todaySalesSummaryRequest = MasRepository.SalesSummaryRequest(
        MasRepository.SalesSummaryType.SELF,
        MasRepository.SalesSummaryPeriod.DAILY,
        MasRepository.SalesSummaryIntervalAge(0),
    )

    private data class SalesData(
        val pastAirtime: List<Float>,
        val pastBundles: List<Float>,
        val presentAirtime: List<Float>,
        val presentBundles: List<Float>
    )

    private lateinit var weeklySalesChart: ColumnChartView
    private lateinit var salesTargetChart: PieChartView

    private var lastKnownBalance: BalancesResponseModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accountInfoFactory = InjectorUtils.provideAccountInfoFactory(requireContext())
        accountInfoViewModel =
            ViewModelProvider(this, accountInfoFactory)[AccountInfoViewModel::class.java]

        val balancesFactory = InjectorUtils.provideAccountBalancesFactory(requireContext())
        accountBalancesViewModel =
            ViewModelProvider(this, balancesFactory)[AccountBalancesViewModel::class.java]

        val statsFactory = InjectorUtils.provideStatisticsFactory(requireContext())
        statisticsViewModel =
            ViewModelProvider(this, statsFactory)[StatisticsViewModel::class.java]

        masRepository = MasRepository(requireContext())
    }

    override fun onResume() {
        super.onResume()
        var homePageTitle: String? = null
        accountInfoViewModel.getAccountInfo()?.onSuccess {
            homePageTitle = getString(R.string.hello_prefix, "${it.firstName} ${it.surname}")
        }

        NavigationManager.resumePage(NavigationManager.Page.HOME.setActionBarTitle(homePageTitle))
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        useBinding { binding ->
            // we don't enable editing your target on the HOME screen
            binding.salesTargetChartCard.editSalesTargetButton.visibility = View.GONE

            // setup currency suffix
            binding.stockBalanceCard.totalBalanceLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.total_balance))
            binding.stockBalanceCard.stockLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.balance))
            binding.stockBalanceCard.bonusBalanceLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.bonus_balance))
            binding.stockBalanceCard.onHoldLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.on_hold_balance))

            binding.totalAndBreakdownSalesCard.salesTodayLabel.text =
                Formatter.formatWithSmallCurrencySuffix(
                    getString(R.string.total_sales_today), "&thinsp;&nbsp;", 0.8f
                )
            binding.weeklySalesBarChart.weeklySalesChartLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.weekly_sales))
            binding.salesTargetChartCard.salesTargetLabel.text =
                Formatter.formatWithSmallCurrencySuffix(
                    getString(R.string.sales_target_title), "&thinsp;&nbsp;", 0.8f
                )

            // progress bars
            binding.salesTargetChartCard.salesTargetContent.visibility = View.GONE
            binding.weeklySalesBarChart.weeklySalesChart.visibility = View.GONE
            binding.totalAndBreakdownSalesCard.totalAndBreakdownSalesContent.visibility = View.GONE

            binding.salesTargetChartCard.salesTargetTachometerChartProgressBar.visibility =
                View.VISIBLE
            binding.weeklySalesBarChart.salesTargetColumnChartProgressBar.visibility = View.VISIBLE
            binding.totalAndBreakdownSalesCard.progressBar.visibility = View.VISIBLE
            // ///////////////////

            salesTargetChart = binding.salesTargetChartCard.salesTargetTachometerChart
            salesTargetChart.isZoomEnabled = false
            salesTargetChart.isScrollEnabled = false
            salesTargetChart.isChartRotationEnabled = false

            weeklySalesChart = binding.weeklySalesBarChart.weeklySalesChart
            weeklySalesChart.isZoomEnabled = false
            weeklySalesChart.isScrollEnabled = false

            // MAIN progress bar displayed until ready
            binding.homeContent.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            // weekly sales with target, progress bar until ready

            val salesTodayFuture = getSalesToday()

            salesTodayFuture.thenApplyAsync {
                statisticsViewModel.viewModelScope.launch {
                    /**
                     * - TAKE NOTE -
                     *
                     * Showing SalesToday breakdown card ...
                     *  Requires 'showWeeklySalesBarGraph' feature toggle
                     */
                    if (it.isSuccess && FeatureToggle.HomePage.showTodaySalesBreakdown) {
                        binding.totalAndBreakdownSalesCard.progressBar.visibility = View.GONE
                        binding.totalAndBreakdownSalesCard.totalAndBreakdownSalesContent.visibility =
                            View.VISIBLE
                    } else {
                        binding.totalAndBreakdownSalesCard.root.visibility = View.GONE
                    }
                }
            }

            /**
             * Don't let us retain the cached balance, home page must always update when landed on
             */
            AccountBalancesViewModel.resetCache()

            val balancesFuture = getBalancesFuture()
            val teamMembershipFuture = getTeamMembership()
            val weeklySalesChartDataFuture = getWeeklySalesChartData()

            CompletableFuture.allOf(balancesFuture, teamMembershipFuture).thenRun {
                val teamMembership = teamMembershipFuture.get()
                val balances = balancesFuture.get()

                statisticsViewModel.viewModelScope.launch {
                    if (teamMembership.isFailure) {
                        binding.salesTargetChartCard.root.visibility = View.GONE
                    }
                }


                accountBalancesViewModel.viewModelScope.launch {
                    /**
                     * Special case ... we WANT to show the content with or without a failure ...
                     *  if failed... each balance will have `ERR` in it
                     */
                    binding.stockBalanceCard.stockBalanceContent.visibility = View.VISIBLE
                    binding.stockBalanceCard.progressBar.visibility = View.GONE

                    if (balances.isSuccess || lastKnownBalance != null) {
                        binding.stockBalanceCard.balanceSwitchButton.setOnClickListener {
                            val newState =
                                binding.stockBalanceCard.balanceSwitchButton.isChecked
                            val balancesToToggle =
                                if (balances.isSuccess) balances.getOrThrow()
                                else lastKnownBalance!!
                            toggleBalanceVisible(balancesToToggle, newState)
                        }
                    } else {
                        binding.stockBalanceCard.balanceVisibilityBlock.visibility = View.GONE
                    }

                    // Done loading ... show page
                    binding.homeContent.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }

            CompletableFuture.allOf(teamMembershipFuture, weeklySalesChartDataFuture).thenRun {
                val teamMembership = teamMembershipFuture.get()
                val weeklySalesChartData = weeklySalesChartDataFuture.get()

                statisticsViewModel.viewModelScope.launch {
                    if (weeklySalesChartData.isSuccess) {
                        // Progress Toggles
                        binding.salesTargetChartCard.salesTargetContent.visibility =
                            View.VISIBLE
                        binding.salesTargetChartCard.salesTargetTachometerChartProgressBar.visibility =
                            View.GONE

                        binding.weeklySalesBarChart.weeklySalesChart.visibility =
                            View.VISIBLE
                        binding.weeklySalesBarChart.salesTargetColumnChartProgressBar.visibility =
                            View.GONE
                    } else {
                        binding.salesTargetChartCard.root.visibility = View.GONE
                        binding.weeklySalesBarChart.root.visibility = View.GONE

                        binding.pageError.visibility = View.VISIBLE
                        binding.pageError.text = getString(R.string.load_dashboard_error)
                    }
                }

                applyWeeklyData(binding, teamMembership, weeklySalesChartData)
            }

            CompletableFuture.allOf(
                // we wait for `weeklySalesChartDataFuture`, so all handling is done after it
                //  but it's got it's own error handling, so we don't process it's error here
                salesTodayFuture, balancesFuture, teamMembershipFuture, weeklySalesChartDataFuture
            ).thenRun {
                val balances = balancesFuture.get()
                val teamMembership = teamMembershipFuture.get()
                val salesToday = salesTodayFuture.get()

                var isRealTeamMembershipFailure = false
                teamMembership.onFailure {
                    val membershipNotFound = MasRepository.ErrorMessages.throwableMatchesError(
                        it, MasRepository.ErrorMessages.GET_MEMBERSHIP_NOT_FOUND
                    )

                    // Check is inverse
                    if (!membershipNotFound) isRealTeamMembershipFailure = true
                }

                /**
                 * weeklyChartData - failures are handled separately
                 */
                var withError = isRealTeamMembershipFailure || salesToday.isFailure

                /**
                 * custom balances error handling ...
                 * if it's the FIRST TIME loading balances (i.e. lastKnownBalance == null)
                 * then this is a full error
                 */
                if (!withError) {
                    withError = balances.isFailure && lastKnownBalance == null
                }

                if (withError) {
                    Log.e(_tag, "isTeamMembership.failure? $isRealTeamMembershipFailure")
                    Log.e(_tag, "isSalesToday.failure? ${salesToday.isFailure}")
                    Toaster.showCustomToast(
                        NavigationManager.getActivity(),
                        getString(R.string.load_dashboard_error),
                        Toaster.Options().setType(ToasterType.WARN)
                    )
                }
            }
        }
    }

    private fun updateBalancesOnView(totalBalanceResult: CSResult<BalancesResponseModel>) {
        useBinding { binding ->
            totalBalanceResult
                .onFailure {
                    if (lastKnownBalance != null) {
                        binding.stockBalanceCard.lastUpdated.visibility = View.VISIBLE
                        binding.stockBalanceCard.lastUpdated.text = resources.getString(
                            R.string.last_updated_prefix,
                            CustomUtils.timeAgo(this.resources, lastKnownBalance!!.lastUpdated)
                        )
                    }
                }
                .onSuccess {
                    binding.stockBalanceCard.lastUpdated.visibility = View.GONE
                    lastKnownBalance = it
                }

            if (lastKnownBalance == null) return@useBinding

            val balanceModel = lastKnownBalance!!

            val totalBalance = balanceModel.totalBalance
            val balance = balanceModel.balance.toDoubleOrNull() ?: 0.0
            val bonusBalance = balanceModel.bonusBalance.toDoubleOrNull() ?: 0.0
            val onHoldBalance = balanceModel.onHoldBalance.toDoubleOrNull() ?: 0.0

            val formatParams =
                CurrencyFormatParams().useWithoutCurrencyPattern().withTwoDecimalPlaces()

            // Total
            binding.stockBalanceCard.totalBalanceValue.text =
                Formatter.formatCustomCurrency(totalBalance, formatParams)

            // Stock balance
            binding.stockBalanceCard.stockValue.text = Formatter.formatCustomCurrency(
                balance, formatParams
            )

            // Bonus balance
            binding.stockBalanceCard.bonusBalanceValue.text = Formatter.formatCustomCurrency(
                bonusBalance, formatParams
            )

            // On Hold balance
            if (onHoldBalance == 0.0) {
                binding.stockBalanceCard.onHoldBalanceWrapper.visibility = View.GONE
            } else {
                binding.stockBalanceCard.onHoldBalanceWrapper.visibility = View.VISIBLE
            }
            binding.stockBalanceCard.onHoldValue.text = Formatter.formatCustomCurrency(
                onHoldBalance, formatParams
            )

            if (FeatureToggle.HomePage.showTotalAndTradeBonus) {
                binding.stockBalanceCard.totalBalanceWrapper.visibility = View.VISIBLE
                binding.stockBalanceCard.bonusBalanceWrapper.visibility = View.VISIBLE
            }

            if (!FeatureToggle.HomePage.showWeeklySalesBarGraph) {
                binding.weeklySalesBarChart.root.visibility = View.GONE
            }

            toggleBalanceVisible(balanceModel)
        }
    }

    private fun getBalancesFuture(): CompletableFuture<CSResult<BalancesResponseModel>> {
        val balancesFuture = CompletableFuture<CSResult<BalancesResponseModel>>()

        accountBalancesViewModel.getBalancesLiveData()
            .observe(viewLifecycleOwner) { totalBalanceResult ->
                updateBalancesOnView(totalBalanceResult)
                balancesFuture.complete(totalBalanceResult)
            }

        return balancesFuture
    }

    private fun toggleBalanceVisible(balances: BalancesResponseModel, newState: Boolean? = null) {
        useBinding { binding ->
            val storedBalanceState = androidStore.getBalanceVisibility()
            // Fallback to default if no explicit state is set or found
            val newBalanceVisibilityState =
                newState ?: storedBalanceState ?: defaultBalanceVisibilityState

            if (newBalanceVisibilityState) {
                val formatParams =
                    CurrencyFormatParams().useWithoutCurrencyPattern().withTwoDecimalPlaces()

                androidStore.saveBalanceVisibility(true)
                binding.stockBalanceCard.balanceSwitchButton.isChecked = true
                binding.stockBalanceCard.totalBalanceValue.text = Formatter.formatCustomCurrency(
                    balances.totalBalance, formatParams
                )
                binding.stockBalanceCard.stockValue.text = Formatter.formatCustomCurrency(
                    balances.balance.toDoubleOrNull() ?: 0.0, formatParams
                )
                binding.stockBalanceCard.bonusBalanceValue.text = Formatter.formatCustomCurrency(
                    balances.bonusBalance.toDoubleOrNull() ?: 0.0, formatParams
                )
                binding.stockBalanceCard.onHoldValue.text = Formatter.formatCustomCurrency(
                    balances.onHoldBalance.toDoubleOrNull() ?: 0.0, formatParams
                )
            } else {
                binding.stockBalanceCard.totalBalanceValue.text = "********"
                binding.stockBalanceCard.stockValue.text = "********"
                binding.stockBalanceCard.bonusBalanceValue.text = "********"
                binding.stockBalanceCard.onHoldValue.text = "********"
                binding.stockBalanceCard.balanceSwitchButton.isChecked = false
                androidStore.saveBalanceVisibility(false)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getSalesToday(): CompletableFuture<CSResult<SalesSummaryModel>> {
        val salesTodayFuture = CompletableFuture<CSResult<SalesSummaryModel>>()

        statisticsViewModel.getSingleSaleLiveData(todaySalesSummaryRequest)
            .observe(viewLifecycleOwner) { salesTodayResult ->
                useBinding { binding ->
                    salesTodayResult
                        .onSuccess {
                            // Applicable going forward...
                            val currencyFormatParams =
                                CurrencyFormatParams().useWithoutCurrencyPattern()
                                    .withNoDecimalPlaces()

                            val combinedSales =
                                (it.value.airtimeSalesValue.toDoubleOrNull() ?: 0.0) +
                                        (it.value.bundleSalesValue.toDoubleOrNull() ?: 0.0)
                            val totalSales =
                                Formatter.formatCustomCurrency(combinedSales, currencyFormatParams)

                            val combinedCounts =
                                it.value.airtimeSalesCount + it.value.bundleSalesCount

                            val airtimeSales = Formatter.formatCustomCurrency(
                                it.value.airtimeSalesValue.toDoubleOrNull() ?: 0.0,
                                currencyFormatParams
                            )
                            val bundleSales = Formatter.formatCustomCurrency(
                                it.value.bundleSalesValue.toDoubleOrNull() ?: 0.0,
                                currencyFormatParams
                            )

                            binding.totalAndBreakdownSalesCard.totalSalesToday.text = totalSales
                            binding.totalAndBreakdownSalesCard.airtimeSalesToday.text = airtimeSales
                            binding.totalAndBreakdownSalesCard.bundlesSalesToday.text = bundleSales

                            binding.totalAndBreakdownSalesCard.numberOfTotalSales.text =
                                "(" + getString(
                                    R.string.number_of_sales, combinedCounts.toString()
                                ) + ")"
                            binding.totalAndBreakdownSalesCard.numberOfAirtimeSales.text =
                                "(" + getString(
                                    R.string.number_of_sales, it.value.airtimeSalesCount.toString()
                                ) + ")"
                            binding.totalAndBreakdownSalesCard.numberOfBundleSales.text =
                                "(" + getString(
                                    R.string.number_of_sales, it.value.bundleSalesCount.toString()
                                ) + ")"
                        }

                    salesTodayFuture.complete(salesTodayResult)
                }
            }

        return salesTodayFuture
    }

    private fun getWeeklySalesChartData(): CompletableFuture<Result<SalesData>> {
        val salesData = CompletableFuture<Result<SalesData>>()

        statisticsViewModel.getSalesLiveData(lastTwoWeeksSalesSummaryRequests + todaySalesSummaryRequest)
            .observe(viewLifecycleOwner) { weeklySalesRequest ->
                weeklySalesRequest
                    .onFailure {
                        salesData.complete(Result.failure(it))
                    }
                    .onSuccess { weeklySales ->
                        /**
                         * weeklySales contains 14 days going backward from today (inclusive)
                         *
                         * this allows us to get LAST week and THIS week data for the graph
                         *
                         * the data starts from 14th day ago, until today
                         */

                        val weekStartDay = MasRepository.weekStartDay

                        // If we start the week on MONDAYS ... get the indexes for both MONDAYS in the last 14 days...
                        val lastWeekStartDayIndex =
                            weeklySales.mapIndexedNotNull { index, weeklySale ->
                                val localDateTime = LocalDateTime.ofInstant(
                                    Instant.ofEpochSecond(weeklySale.startTime),
                                    ZoneId.systemDefault()
                                )
                                if (localDateTime.dayOfWeek == weekStartDay) index else null
                            }.first()

                        val fromTwoWeeksAgoToNowInDays =
                            weeklySales.mapIndexedNotNull { index, salesSummaryModel ->
                                if (index >= lastWeekStartDayIndex) salesSummaryModel else null
                            }

                        val lastWeekAirtimeSales = mutableListOf<Float>()
                        val lastWeekBundleSales = mutableListOf<Float>()
                        val thisWeekAirtimeSales = mutableListOf<Float>()
                        val thisWeekBundleSales = mutableListOf<Float>()

                        var days = 1
                        fromTwoWeeksAgoToNowInDays.forEach {
                            val airtimeSales = it.value.airtimeSalesValue.toDoubleOrNull() ?: 0.0
                            val bundleSales = it.value.bundleSalesValue.toDoubleOrNull() ?: 0.0

                            if (days <= 7) {
                                lastWeekAirtimeSales.add(airtimeSales.toFloat())
                                lastWeekBundleSales.add(bundleSales.toFloat())
                            } else {
                                thisWeekAirtimeSales.add(airtimeSales.toFloat())
                                thisWeekBundleSales.add(bundleSales.toFloat())
                            }

                            days += 1
                        }

                        salesData.complete(
                            Result.success(
                                SalesData(
                                    lastWeekAirtimeSales, lastWeekBundleSales,
                                    thisWeekAirtimeSales, thisWeekBundleSales
                                )
                            )
                        )
                    }
            }

        return salesData
    }

    private fun getTeamMembership(): CompletableFuture<CSResult<TeamMembership>> {
        val teamMembership = CompletableFuture<CSResult<TeamMembership>>()

        if (FeatureToggle.Nav.hasTeamPages) {
            statisticsViewModel.viewModelScope.launch {
                statisticsViewModel.getTeamMembership {
                    teamMembership.complete(it)
                }
            }
        } else {
            // Explicit "not found" response
            //  prevents any warnings/errors, makes sure to HIDE the team related parts of the page
            teamMembership.complete(
                CSResult.Failure(CSException(MasRepository.ErrorMessages.GET_MEMBERSHIP_NOT_FOUND))
            )
        }

        return teamMembership
    }

    private fun applyWeeklyData(
        binding: FragmentHomeBinding,
        teamMembership: CSResult<TeamMembership>,
        weeklyChartData: Result<SalesData>,
    ) {
        statisticsViewModel.viewModelScope.launch {
            weeklyChartData
                .onSuccess { salesData ->
                    val totalPastSales =
                        salesData.pastAirtime.zip(salesData.pastBundles) { a, b -> a + b }
                    val totalPresentSales =
                        salesData.presentAirtime.zip(salesData.presentBundles) { a, b -> a + b }

                    val columnData = ChartingUtils.getSideBySideBarChartData(
                        requireContext(),
                        totalPastSales, totalPresentSales,
                        ChartingUtils.getWeeklyLabels(requireContext()),
                        ChartingUtils.Colors(
                            R.color.medium_gray, R.color.cs_primary, R.color.cs_primary,
                        )
                    )

                    weeklySalesChart.setPadding(columnData?.chartYPadding ?: 0, 24, 0, 0)
                    weeklySalesChart.columnChartData = columnData?.mainChartData

                    val totalOfWeeklySales = totalPresentSales.sumOf { it.toDouble() }

                    val currencyFormatParams =
                        CurrencyFormatParams().useWithoutCurrencyPattern().withNoDecimalPlaces()

                    binding.salesTargetChartCard.weeklySalesValue.text =
                        Formatter.formatCustomCurrency(totalOfWeeklySales, currencyFormatParams)

                    teamMembership
                        .onSuccess { teamMembership ->
                            val target =
                                teamMembership.salesTarget
                                    ?.weeklyAmount?.stripDecimalsOrNull()?.makeZeroEmpty() ?: ""

                            if (target.isEmpty()) {
                                binding.salesTargetChartCard.weeklyTargetValue.text =
                                    getString(R.string.c_not_set)
                            } else {
                                binding.salesTargetChartCard.weeklyTargetValue.text =
                                    Formatter.formatCustomCurrency(
                                        target.toDouble(), currencyFormatParams
                                    )
                            }

                            val percentAchieved: Float? =
                                if (target.isNotEmpty()) (totalOfWeeklySales * 100 / target.toDouble()).toFloat()
                                else null

                            val tachometerData = ChartingUtils.getTachometerChartData(
                                requireContext(), percentAchieved ?: 0f
                            )

                            var achieved =
                                percentAchieved?.toStringWithPaddedDecimalPlaces(1)
                            if (achieved == null)
                                binding.salesTargetChartCard.achievedPercent.text =
                                    getString(R.string.not_applicable)
                            else {
                                achieved =
                                    if (achieved.toFloat() > 1000f) "1000%+"
                                    else "$achieved%"
                                binding.salesTargetChartCard.achievedPercent.text = achieved
                            }
                            salesTargetChart.pieChartData = tachometerData.mainChartData
                        }
                }
        }
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        /**
         * Use this factory method to create a new instance of this fragment
         * @return A new instance of fragment Home.
         */
        @JvmStatic
        fun newInstance() = Home()
    }
}
