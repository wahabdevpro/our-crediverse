package systems.concurrent.crediversemobile.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import lecho.lib.hellocharts.view.ColumnChartView
import lecho.lib.hellocharts.view.PieChartView
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentTeamMemberStatsBinding
import systems.concurrent.crediversemobile.models.CurrencyFormatParams
import systems.concurrent.crediversemobile.models.SalesSummaryModel
import systems.concurrent.crediversemobile.models.SalesTargetModel
import systems.concurrent.crediversemobile.models.TeamMemberModel
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.utils.Formatter.makeZeroEmpty
import systems.concurrent.crediversemobile.utils.Formatter.stripDecimalsOrNull
import systems.concurrent.crediversemobile.utils.Formatter.toStringWithPaddedDecimalPlaces
import systems.concurrent.crediversemobile.view_models.StatisticsViewModel
import systems.concurrent.masapi.MasApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.CompletableFuture

/**
 * A simple [Fragment] subclass.
 * Use the [TeamMemberStatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeamMemberStatsFragment(private val teamMember: TeamMemberModel) :
    ViewBindingFragment<FragmentTeamMemberStatsBinding>(FragmentTeamMemberStatsBinding::inflate) {

    override val thisPage: NavigationManager.Page? = null

    private val _tag = this::class.java.kotlin.simpleName
    private lateinit var statisticsViewModel: StatisticsViewModel

    // 13 days ... going backward from YESTERDAY (inclusive)
    private val memberWeeklySalesSummaryRequests = (13 downTo 1).map {
        MasRepository.SalesSummaryRequest(
            MasRepository.SalesSummaryType.MEMBER,
            MasRepository.SalesSummaryPeriod.DAILY,
            MasRepository.SalesSummaryIntervalAge(it.toByte()),
            teamMember.msisdn
        )
    }

    // TODAY
    private val todaySalesSummaryRequest = MasRepository.SalesSummaryRequest(
        MasRepository.SalesSummaryType.MEMBER,
        MasRepository.SalesSummaryPeriod.DAILY,
        MasRepository.SalesSummaryIntervalAge(0),
        teamMember.msisdn
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val statsFactory = InjectorUtils.provideStatisticsFactory(requireContext())

        statisticsViewModel =
            ViewModelProvider(this, statsFactory)[StatisticsViewModel::class.java]
    }

    private fun togglePageVisibility(show: Boolean, withError: Boolean = false) {
        useBinding { binding ->
            if (show && withError) {
                binding.rootWrapper.visibility = View.GONE
                binding.pageError.visibility = View.VISIBLE
            } else if (show) {
                binding.rootWrapper.visibility = View.VISIBLE
                binding.pageError.visibility = View.GONE
            }

            binding.progressBar.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    private lateinit var weeklySalesChart: ColumnChartView
    private lateinit var salesTargetChart: PieChartView

    private data class SalesData(
        val pastAirtime: List<Float>,
        val pastBundles: List<Float>,
        val presentAirtime: List<Float>,
        val presentBundles: List<Float>
    )

    private val targetLiveData = MutableLiveData(0.0)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickHandlers()

        useBinding { binding ->
            val weeklySalesBarChart = binding.weeklySalesBarChart
            val totalAndBreakdownSalesCard = binding.totalAndBreakdownSalesCard
            val stockBalanceCard = binding.stockBalanceCard

            if (!FeatureToggle.TeamMemberPage.showWeeklySalesBarGraph) {
                weeklySalesBarChart.root.visibility = View.GONE
            }

            stockBalanceCard.balanceVisibilityBlock.visibility = View.GONE
            stockBalanceCard.stockLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.balance))
            totalAndBreakdownSalesCard.salesTodayLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.total_sales_today))
            weeklySalesBarChart.weeklySalesChartLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.weekly_sales))
            binding.salesTargetChartCard.salesTargetLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.sales_target_title))

            salesTargetChart = binding.salesTargetChartCard.salesTargetTachometerChart
            salesTargetChart.isZoomEnabled = false
            salesTargetChart.isScrollEnabled = false
            salesTargetChart.isChartRotationEnabled = false

            weeklySalesChart = weeklySalesBarChart.weeklySalesChart
            weeklySalesChart.isZoomEnabled = false
            weeklySalesChart.isScrollEnabled = false

            val salesTodayFuture = CompletableFuture<CSResult<SalesSummaryModel>>()
            statisticsViewModel.getSingleSaleLiveData(todaySalesSummaryRequest)
                .observe(viewLifecycleOwner) { salesTodayFuture.complete(it) }

            /**
             * We wait for BOTH to complete...
             * because teamMembership decides if some things display on the screen
             */
            CompletableFuture.allOf(salesTodayFuture).thenRun {
                val salesTodayResult = salesTodayFuture.get()

                salesTodayResult
                    .onSuccess {
                        val stockBalance = Formatter.formatCustomCurrency(
                            teamMember.stockBalance.balance.toDoubleOrNull() ?: 0.0,
                            CurrencyFormatParams().useWithoutCurrencyPattern()
                                .withTwoDecimalPlaces()
                        )

                        // Applicable going forward...
                        val currencyFormatParams =
                            CurrencyFormatParams().useWithoutCurrencyPattern()
                                .withNoDecimalPlaces()

                        val combinedSales = (it.value.airtimeSalesValue.toDoubleOrNull() ?: 0.0) +
                                (it.value.bundleSalesValue.toDoubleOrNull() ?: 0.0)
                        val combinedCounts = it.value.airtimeSalesCount + it.value.bundleSalesCount

                        val totalSales =
                            Formatter.formatCustomCurrency(combinedSales, currencyFormatParams)

                        val airtimeSales = Formatter.formatCustomCurrency(
                            it.value.airtimeSalesValue.toDoubleOrNull() ?: 0.0, currencyFormatParams
                        )
                        val bundleSales = Formatter.formatCustomCurrency(
                            it.value.bundleSalesValue.toDoubleOrNull() ?: 0.0, currencyFormatParams
                        )

                        stockBalanceCard.stockValue.text = stockBalance

                        totalAndBreakdownSalesCard.totalSalesToday.text = totalSales
                        totalAndBreakdownSalesCard.airtimeSalesToday.text = airtimeSales
                        totalAndBreakdownSalesCard.bundlesSalesToday.text = bundleSales

                        totalAndBreakdownSalesCard.numberOfTotalSales.text =
                            "(" + getString(
                                R.string.number_of_sales, combinedCounts.toString()
                            ) + ")"
                        totalAndBreakdownSalesCard.numberOfAirtimeSales.text =
                            "(" + getString(
                                R.string.number_of_sales, it.value.airtimeSalesCount.toString()
                            ) + ")"
                        totalAndBreakdownSalesCard.numberOfBundleSales.text =
                            "(" + getString(
                                R.string.number_of_sales, it.value.bundleSalesCount.toString()
                            ) + ")"
                    }

                togglePageVisibility(show = true, withError = salesTodayResult.isFailure)
            }

            val weeklyChartDataFuture = CompletableFuture<Result<SalesData>>()
            getWeeklySalesChartData { weeklyChartDataFuture.complete(it) }

            applyWeeklySalesChartData(weeklyChartDataFuture)
        }
    }

    private fun applyWeeklySalesChartData(
        weeklyChartDataFuture: CompletableFuture<Result<SalesData>>
    ) {
        weeklyChartDataFuture.thenApplyAsync { resultData ->
            NavigationManager.runOnNavigationUIThread { _ ->
                useBinding { binding ->
                    resultData
                        .onSuccess { salesData ->
                            val totalPastSales =
                                salesData.pastAirtime.zip(salesData.pastBundles) { a, b -> a + b }
                            val totalPresentSales =
                                salesData.presentAirtime.zip(salesData.presentBundles) { a, b -> a + b }

                            if (totalPresentSales.isEmpty()) {

                                binding.weeklySalesMessage.visibility = View.VISIBLE
                                binding.weeklySalesMessage.setTextColor(requireContext().getColor(R.color.warn))
                                binding.weeklySalesMessage.text =
                                    getString(R.string.no_weekly_sales)

                                binding.weeklySalesBarChart.root.visibility = View.GONE
                            }

                            val columnData = ChartingUtils.getSideBySideBarChartData(
                                requireContext(),
                                totalPastSales, totalPresentSales,
                                ChartingUtils.getWeeklyLabels(requireContext()),
                                ChartingUtils.Colors(
                                    R.color.medium_gray, R.color.cs_primary, R.color.cs_primary,
                                )
                            )

                            binding.weeklySalesBarChart.weeklySalesChart.setPadding(
                                columnData?.chartYPadding ?: 0, 24, 0, 0
                            )
                            binding.weeklySalesBarChart.weeklySalesChart.columnChartData =
                                columnData?.mainChartData

                            binding.weeklySalesBarChart.weeklySalesChart.visibility = View.VISIBLE
                            binding.salesTargetChartCard.salesTargetContent.visibility =
                                View.VISIBLE

                            val totalPresentWeekSales = totalPresentSales.sumOf { it.toDouble() }

                            binding.salesTargetChartCard.weeklySalesValue.text =
                                Formatter.formatCustomCurrency(
                                    totalPresentWeekSales,
                                    CurrencyFormatParams().useWithoutCurrencyPattern()
                                        .withNoDecimalPlaces()
                                )

                            targetLiveData.observe(viewLifecycleOwner) { target ->
                                val percentAchieved: Float? =
                                    if (target != 0.0) (totalPresentWeekSales * 100 / target).toFloat()
                                    else null

                                val tachometerData = ChartingUtils.getTachometerChartData(
                                    requireContext(), percentAchieved ?: 0f
                                )

                                var achieved =
                                    percentAchieved?.toStringWithPaddedDecimalPlaces(1)
                                if (achieved == null)
                                    binding.salesTargetChartCard.achievedPercent.text =
                                        Formatter.adjustSizeRelative(
                                            getString(R.string.not_applicable), 0.8f
                                        )
                                else {
                                    achieved =
                                        if (achieved.toFloat() > 1000f) "1000%+"
                                        else "$achieved%"
                                    binding.salesTargetChartCard.achievedPercent.text = achieved
                                }
                                salesTargetChart.pieChartData = tachometerData.mainChartData
                            }

                            targetLiveData.value =
                                teamMember.salesTarget?.weeklyAmount?.toDoubleOrNull() ?: 0.0

                            setupSalesTargetEditHandling(binding)
                        }.onFailure {
                            binding.weeklySalesMessage.visibility = View.VISIBLE
                            binding.weeklySalesMessage.setTextColor(requireContext().getColor(R.color.danger))
                            binding.weeklySalesMessage.text = getString(R.string.weekly_sales_error)
                            binding.salesTargetChartCard.root.visibility = View.GONE
                            binding.weeklySalesBarChart.root.visibility = View.GONE
                        }
                    binding.weeklySalesBarChart.salesTargetColumnChartProgressBar.visibility =
                        View.GONE
                    binding.salesTargetChartCard.salesTargetTachometerChartProgressBar.visibility =
                        View.GONE
                }
            }
        }
    }

    private fun setupSalesTargetEditHandling(
        binding: FragmentTeamMemberStatsBinding
    ) {
        var weeklyTarget =
            teamMember.salesTarget?.weeklyAmount?.stripDecimalsOrNull()?.makeZeroEmpty() ?: ""

        // applicable going forward...
        val currencyFormatParams = CurrencyFormatParams().withNoDecimalPlaces()

        if (weeklyTarget.isNotEmpty()) {
            binding.salesTargetChartCard.weeklyTargetValue.text =
                Formatter.formatCustomCurrency(weeklyTarget.toDouble(), currencyFormatParams)
        } else {
            binding.salesTargetChartCard.weeklyTargetValue.text = getString(R.string.c_not_set)
        }

        val inputHint = getString(R.string.sales_target_amount)
        var inputDefaultText = weeklyTarget
        val teamMemberName = "${teamMember.firstName} ${teamMember.surname}"

        binding.salesTargetChartCard.editSalesTargetButton.setOnClickListener {
            val dialog = Dialog(
                NavigationManager.getActivity(), DialogType.CONFIRM, "",
                Formatter.adjustSizeRelative(
                    Formatter.combine(
                        Formatter.fromHtml(
                            getString(
                                R.string.update_weekly_sales_target_for_team_member,
                                teamMemberName
                            )
                        ), "\n"
                    ), 1.2f
                ),
                DialogOptions().withInput(true, inputHint, inputDefaultText)
                    .setSize(DialogSize.LARGE)
                    .setConfirmBtnTextResource(R.string.submit_button_text)
                    .setAutoDismiss(false)
                    .setDialogHint(getString(R.string.target_clear_hint))
            )

            dialog
                .onConfirmGetInput { inputText ->
                    dialog.getDialogRootView()?.let {
                        ActivityUtils.hideKeyboard(NavigationManager.getActivity(), it)
                    }

                    val inputValue = inputText.stripDecimalsOrNull() ?: ""

                    val isZero = inputValue.isNotEmpty() && inputValue.toDouble() == 0.0
                    val tooLarge =
                        inputValue.isNotEmpty() && inputValue.toDouble() > maxWeeklyTarget

                    val toastMessage = when {
                        isZero || tooLarge -> {
                            Formatter.combine(
                                getString(R.string.target_zero_or_too_large),
                                Formatter.formatCustomCurrency(
                                    maxWeeklyTarget.toDouble(),
                                    currencyFormatParams.withNoDecimalPlaces()
                                )
                            )
                        }
                        inputText == weeklyTarget -> getString(R.string.target_no_change)
                        else -> null
                    }

                    if (toastMessage != null) {
                        Toaster.showCustomToast(
                            requireActivity(), toastMessage,
                            Toaster.Options().setType(ToasterType.INFO)
                        )
                        //dialog.dismiss()
                        return@onConfirmGetInput
                    }

                    Log.i(_tag, "Entered target amount (numeric or empty): '$inputValue'")
                    dialog.toggleInputProgress(show = true)
                    setSalesTarget(inputValue) {
                        if (it.isSuccess) {
                            weeklyTarget = inputValue
                            inputDefaultText = inputValue
                            teamMember.salesTarget?.weeklyAmount = weeklyTarget
                            targetLiveData.value = weeklyTarget.toDoubleOrNull() ?: 0.0
                        }
                        dialog.toggleInputProgress(show = false)
                        dialog.dismiss()
                    }
                }
                .onDismiss { dialog.dismiss() }
                .show()
        }
    }

    private fun clickHandlers() {
        useBinding { binding ->
            binding.closeButton.setOnClickListener {
                NavigationManager.setPage(
                    NavigationManager.Page.CHOOSE_TEAM_MEMBER, addToHistory = true
                )
            }
            binding.detailedStatsButton.setOnClickListener {
                NavigationManager.setPage(
                    NavigationManager.Page.STATS.setFragment(
                        StatsFragment(MasRepository.SalesSummaryType.MEMBER, teamMember)
                    ), addToHistory = true
                )
            }
        }
    }

    private fun setSalesTarget(amount: String, callback: (CSResult<SalesTargetModel>) -> Unit) {
        statisticsViewModel.setSalesTarget(
            SalesTargetModel(teamMember.msisdn, MasApi.Period.WEEK, amount)
        ) { result ->
            NavigationManager.runOnNavigationUIThread {
                result.onSuccess { _ ->
                    useBinding { binding ->
                        if (amount.isNotEmpty()) {
                            binding.salesTargetChartCard.weeklyTargetValue.text =
                                Formatter.formatCustomCurrency(
                                    amount.toDouble(),
                                    CurrencyFormatParams().withNoDecimalPlaces()
                                )
                        } else {
                            binding.salesTargetChartCard.weeklyTargetValue.text =
                                getString(R.string.c_not_set)
                        }
                    }

                    val teamMemberName = "${teamMember.firstName} ${teamMember.surname}"

                    val message = when (amount.isEmpty()) {
                        true -> {
                            Formatter.fromHtml(
                                getString(
                                    R.string.sales_target_removed_for_team_member, teamMemberName
                                )
                            )
                        }
                        false -> getString(R.string.sales_target_updated_successfully)
                    }

                    val toasterType = when (amount.isEmpty()) {
                        true -> ToasterType.WARN
                        false -> ToasterType.SUCCESS
                    }

                    Toaster.showCustomToast(
                        requireActivity(), message,
                        Toaster.Options().setType(toasterType)
                    )
                }.onFailure {
                    val finalString = it.getStringFromResourceOrDefault(requireContext())
                    Dialog(requireActivity(), DialogType.ERROR, "", finalString).show()
                }
                callback(result)
            }
        }
    }

    private fun getWeeklySalesChartData(callback: (Result<SalesData>) -> Unit) {
        statisticsViewModel.getSalesLiveData(memberWeeklySalesSummaryRequests + todaySalesSummaryRequest)
            .observe(viewLifecycleOwner) { weeklySalesRequest ->
                weeklySalesRequest
                    .onFailure {
                        Log.e(_tag, "Problem getting hourly data: ${it.message}")
                        callback(Result.failure(it))
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

                        callback(
                            Result.success(
                                SalesData(
                                    lastWeekAirtimeSales, lastWeekBundleSales,
                                    thisWeekAirtimeSales, thisWeekBundleSales
                                )
                            )
                        )
                    }
            }
    }

    companion object {
        private const val maxWeeklyTarget = 9999999

        /**
         * Use this factory method to create a new instance of this fragment
         * @return A new instance of fragment Home.
         */
        @JvmStatic
        fun newInstance(teamMember: TeamMemberModel) = TeamMemberStatsFragment(teamMember)
    }
}
