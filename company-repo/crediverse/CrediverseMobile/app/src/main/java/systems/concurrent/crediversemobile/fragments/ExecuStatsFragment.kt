package systems.concurrent.crediversemobile.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.listener.ViewportChangeListener
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.ColumnChartView
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentExecuStatsBinding
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.services.InjectorUtils
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.StatisticsViewModel
import java.util.concurrent.CompletableFuture

private const val NOT_SET = "-"

/**
 * A simple [Fragment] subclass.
 * Use the [ExecuStatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExecuStatsFragment :
    ViewBindingFragment<FragmentExecuStatsBinding>(FragmentExecuStatsBinding::inflate) {

    override val thisPage by lazy { NavigationManager.Page.EXECU_STATS }

    private val _tag = this::class.java.kotlin.simpleName
    private lateinit var statisticsViewModel: StatisticsViewModel

    private lateinit var masRepository: MasRepository

    private val execuStatsSalesSummaryRequests = listOf(
        MasRepository.SalesSummaryRequest(
            MasRepository.SalesSummaryType.EXEC,
            MasRepository.SalesSummaryPeriod.DAILY,
            MasRepository.SalesSummaryIntervalAge(0)
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = InjectorUtils.provideStatisticsFactory(requireContext())
        statisticsViewModel =
            ViewModelProvider(this, factory)[StatisticsViewModel::class.java]

        masRepository = MasRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        useBinding { binding ->
            chart = binding.chart
            previewChart = binding.chartPreview

            binding.globalSalesHourlyLabel.text =
                Formatter.formatWithSmallCurrencySuffix(getString(R.string.global_sales_hourly))
        }

        initializeUi()

        return view
    }

    private fun togglePageVisibility(show: Boolean) {
        useBinding { binding ->
            binding.rootWrapper.visibility = if (show) View.VISIBLE else View.GONE
            binding.progressBar.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    private lateinit var chart: ColumnChartView
    private lateinit var previewChart: ColumnChartView

    private var data: ColumnChartData? = null
    private var previewData: ColumnChartData? = null

    var todaySalesData: MutableList<Float> = mutableListOf()
    var yesterdaySalesData: List<Float> = listOf()

    private fun previewX(animate: Boolean) {
        val tempViewport = Viewport(chart.maximumViewport)
        val dx = tempViewport.width() / 4
        tempViewport.inset(dx, 0f)
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport)
        } else {
            previewChart.currentViewport = tempViewport
        }
        previewChart.zoomType = ZoomType.HORIZONTAL
    }

    /**
     * Viewport listener for preview chart(lower one). in [.onViewportChanged] method change
     * viewport of upper chart.
     */
    inner class ViewportListener : ViewportChangeListener {
        override fun onViewportChanged(newViewport: Viewport) {
            chart.currentViewport = newViewport
        }
    }

    private var execuStatsGlobalStatsFuture = CompletableFuture<CSResult<Boolean>>()
    private var execuStatsHourlyFuture = CompletableFuture<CSResult<Boolean>>()

    private var combinedFuture =
        CompletableFuture.allOf(execuStatsGlobalStatsFuture, execuStatsHourlyFuture)

    private fun initializeUi() {
        Log.i(_tag, "Initialising UI")

        execuStatsGlobalStatsFuture = CompletableFuture()
        execuStatsHourlyFuture = CompletableFuture()

        combinedFuture.cancel(true)
        combinedFuture =
            CompletableFuture.allOf(execuStatsGlobalStatsFuture, execuStatsHourlyFuture)

        statisticsViewModel.getHourlySalesSummary(
            listOf(
                MasRepository.SalesSummaryIntervalAge(0), // today
                MasRepository.SalesSummaryIntervalAge(1) // yesterday
            )
        ) { hourlySalesResult ->
            hourlySalesResult
                .onFailure {
                    Log.e(_tag, "Problem getting hourly data: ${it.message}")
                    execuStatsHourlyFuture.complete(it.toCSResultFailure())
                }.onSuccess { hourlySales ->
                    val todaySalesPerHour = hourlySales[0].value.map {
                        it.airtimeSalesValue.toFloatOrNull() ?: 0f
                    }
                    val yesterdaySalesPerHour = hourlySales[1].value.map {
                        it.airtimeSalesValue.toFloatOrNull() ?: 0f
                    }

                    val missingDataForYesterday = yesterdaySalesPerHour.size != 24
                    val todayDataInvalid = todaySalesPerHour.size !in 1..24

                    if (todayDataInvalid || missingDataForYesterday) {
                        execuStatsHourlyFuture.complete(CSResult.Failure(CSException("failed getting stats")))
                        return@onSuccess
                    }

                    todaySalesData = todaySalesPerHour.toMutableList()
                    yesterdaySalesData = yesterdaySalesPerHour

                    val todayIsEmpty = todaySalesData.firstOrNull { it.compareTo(0f) != 0 } == null
                    val yesterdayIsEmpty =
                        yesterdaySalesData.firstOrNull { it.compareTo(0f) != 0 } == null
                    if (todayIsEmpty && yesterdayIsEmpty) {
                        execuStatsHourlyFuture.complete(
                            CSResult.Failure(CSException(MasRepository.ErrorMessages.NO_HOURLY_SALES_DATA))
                        )
                        return@onSuccess
                    }

                    if (bindingOrNull == null) {
                        execuStatsHourlyFuture.complete(
                            CSResult.Failure(CSException("failed to complete hourly stats retrieval - no binding"))
                        )
                        return@onSuccess
                    }
                    val columnData = ChartingUtils.getSideBySideBarChartDataWithPreview(
                        requireContext(), yesterdaySalesData, todaySalesData
                    )

                    if (columnData == null) {
                        execuStatsHourlyFuture.complete(
                            CSResult.Failure(CSException("problem getting chart data"))
                        )
                        return@onSuccess
                    }

                    data = columnData.mainChartData
                    previewData = columnData.previewChartData

                    chart.columnChartData = data
                    // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
                    // zoom/scroll is unnecessary.
                    chart.isZoomEnabled = false
                    chart.isScrollEnabled = false

                    previewChart.columnChartData = previewData
                    previewChart.setViewportChangeListener(ViewportListener())

                    previewX(false)

                    Log.e(_tag, "finishing")
                    execuStatsHourlyFuture.complete(CSResult.Success(true))
                }
        }

        statisticsViewModel.getSalesLiveData(execuStatsSalesSummaryRequests)
            .observe(viewLifecycleOwner) { balanceResult ->
                statisticsViewModel.viewModelScope.launch {
                    var balance: Double? = null

                    balanceResult.onSuccess {
                        // Global Stats are stored in the `airtimeSalesValue'
                        balance = it[0].value.airtimeSalesValue.toDoubleOrNull()
                    }.onFailure {
                        Log.w(_tag, "grossRetailViewModel Observer error: ${it.message.toString()}")
                    }

                    val finalBalance =
                        if (balance == null) getString(R.string.error_balance)
                        else Formatter.formatCustomCurrency(balance!!)

                    bindingOrNull?.grossRetailTotal?.text = finalBalance

                    execuStatsGlobalStatsFuture.complete(CSResult.Success(true))
                }
            }

        combinedFuture.thenApply {
            Log.e(_tag, "showing page?")
            statisticsViewModel.viewModelScope.launch {
                useBindingOrNull { binding ->
                    val execStatsGlobalStatsSucceeded = execuStatsGlobalStatsFuture.get().isSuccess
                    val execStatsHourlyStatsSucceeded = execuStatsHourlyFuture.get().isSuccess

                    if (!execStatsGlobalStatsSucceeded || !execStatsHourlyStatsSucceeded) {
                        binding?.hourlyCharts?.visibility = View.GONE
                        binding?.hourlyChartError?.visibility = View.VISIBLE
                        binding?.hourlyChartError?.text = getString(R.string.hourly_stats_error)

                        execuStatsHourlyFuture.get().onFailure {
                            if (it.isAnyOf(MasRepository.ErrorMessages.NO_HOURLY_SALES_DATA)) {
                                binding?.hourlyChartError?.text =
                                    getString(R.string.no_hourly_sales_data)
                            }
                        }
                    }
                    togglePageVisibility(show = true)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // make progress bar visible until data is loaded
        togglePageVisibility(show = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        combinedFuture.cancel(true)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment
         * @return A new instance of fragment Home.
         */
        @JvmStatic
        fun newInstance() = ExecuStatsFragment()
    }
}
