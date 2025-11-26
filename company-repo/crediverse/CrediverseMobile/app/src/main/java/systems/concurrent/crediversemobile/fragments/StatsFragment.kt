package systems.concurrent.crediversemobile.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentStatsLandingBinding
import systems.concurrent.crediversemobile.models.TeamMemberModel
import systems.concurrent.crediversemobile.recyclerViewAdapters.PagerAdapter
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.repositories.MasRepository.SalesSummaryPeriod
import systems.concurrent.crediversemobile.repositories.MasRepository.SalesSummaryType
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.StatisticsViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [StatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatsFragment(
    private val salesSummaryType: SalesSummaryType,
    private var teamMember: TeamMemberModel? = null
) : ViewBindingFragment<FragmentStatsLandingBinding>(FragmentStatsLandingBinding::inflate) {

    override var thisPage: NavigationManager.Page? = null

    private lateinit var viewSlider: ViewPager2
    private lateinit var layoutTabs: TabLayout

    private lateinit var dailyFragment: StatsPreviousAndCurrentFragment
    private lateinit var weeklyFragment: StatsPreviousAndCurrentFragment
    private lateinit var monthlyFragment: StatsPreviousAndCurrentFragment

    private lateinit var dailySalesRequest: List<MasRepository.SalesSummaryRequest>
    private lateinit var weeklySalesRequest: List<MasRepository.SalesSummaryRequest>
    private lateinit var monthlySalesRequest: List<MasRepository.SalesSummaryRequest>

    private lateinit var statisticsViewModel: StatisticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentStatsLandingBinding.inflate(inflater, container, false)

        val statisticsFragmentFactory =
            InjectorUtils.provideStatisticsFactory(requireContext())
        statisticsViewModel = ViewModelProvider(
            this, statisticsFragmentFactory
        )[StatisticsViewModel::class.java]

        viewSlider = binding.statsViewPager
        layoutTabs = binding.statsTabLayout

        dailyFragment = StatsPreviousAndCurrentFragment.newInstance()
        weeklyFragment = StatsPreviousAndCurrentFragment.newInstance()
        monthlyFragment = StatsPreviousAndCurrentFragment.newInstance()
        getFragmentSalesSummaryRequests()

        loadAdapterPages()

        updatePageTitle(withToast = false)

        return binding.root
    }

    private fun getFragmentSalesSummaryRequests() {
        val requests = SalesSummaryPeriod.values().map { salesSummaryPeriod ->
            listOf(
                MasRepository.SalesSummaryRequest(
                    salesSummaryType, salesSummaryPeriod,
                    MasRepository.SalesSummaryIntervalAge(0), teamMember?.msisdn
                ),
                MasRepository.SalesSummaryRequest(
                    salesSummaryType, salesSummaryPeriod,
                    MasRepository.SalesSummaryIntervalAge(1), teamMember?.msisdn
                )
            )
        }

        requests.forEach {
            when (it[0].salesSummaryPeriod) {
                SalesSummaryPeriod.DAILY -> dailySalesRequest = it
                SalesSummaryPeriod.WEEKLY -> weeklySalesRequest = it
                SalesSummaryPeriod.MONTHLY -> monthlySalesRequest = it
            }

            when (it[0].salesSummaryPeriod) {
                SalesSummaryPeriod.DAILY -> dailyFragment.updateSalesSummaryRequests(it)
                SalesSummaryPeriod.WEEKLY -> weeklyFragment.updateSalesSummaryRequests(it)
                SalesSummaryPeriod.MONTHLY -> monthlyFragment.updateSalesSummaryRequests(it)
            }
        }
    }

    private fun loadAdapterPages() {
        Log.e(_tag, "Starting on page: $salesSummaryType")
        viewSlider.isSaveEnabled = false

        viewSlider.adapter = PagerAdapter(this)

        val adapter = viewSlider.adapter as PagerAdapter<*>
        Log.e(_tag, "change adapterPages - $salesSummaryType")

        adapter.addFragment(dailyFragment, getString(R.string.stats_daily_tab_title))
        adapter.addFragment(weeklyFragment, getString(R.string.stats_weekly_tab_title))
        adapter.addFragment(monthlyFragment, getString(R.string.stats_monthly_tab_title))

        viewSlider.currentItem = 0
        //viewSlider.getChildAt(viewSlider.currentItem)

        TabLayoutMediator(layoutTabs, viewSlider) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }

    private fun updatePageTitle(withToast: Boolean = true) {
        // Handle a missing context
        try {
            this.requireContext()
        } catch (e: Exception) {
            Log.e(_tag, "Missing context, will not update page title")
            return
        }

        var message: CharSequence = Formatter.normal(
            when (salesSummaryType) {
                SalesSummaryType.MEMBER -> getString(R.string.team_member_stats)
                SalesSummaryType.TEAM -> getString(R.string.team_stats)
                else -> getString(R.string.my_stats)
            }
        )

        if (withToast) {
            Toaster.showCustomToast(
                NavigationManager.getActivity(), message,
                Toaster.Options().setTime(Snackbar.LENGTH_SHORT).setTopMargin(32F)
            )
        }

        if (salesSummaryType == SalesSummaryType.MEMBER) {
            message = Formatter.combine(
                Formatter.adjustSizeRelative(getString(R.string.team_member_stats) + "\n", 0.7f),
                "${teamMember?.firstName} ${teamMember?.surname} - ${teamMember?.msisdn}"
            )
        }
        NavigationManager.setPageTitle(message)
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        private var busyChangingLayout = false

        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of fragment StatsFragment.
         */
        @JvmStatic
        fun newInstance(salesSummaryType: SalesSummaryType) =
            StatsFragment(salesSummaryType).apply { arguments = Bundle() }
    }
}
