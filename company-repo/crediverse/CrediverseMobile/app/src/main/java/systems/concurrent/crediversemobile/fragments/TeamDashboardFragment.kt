package systems.concurrent.crediversemobile.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.StatisticsViewModel
import systems.concurrent.crediversemobile.databinding.FragmentTeamDashboardBinding
import systems.concurrent.crediversemobile.models.TeamModel
import systems.concurrent.crediversemobile.repositories.MasRepository

/**
 * A simple [Fragment] subclass.
 * Use the [TeamDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeamDashboardFragment :
    ViewBindingFragment<FragmentTeamDashboardBinding>(FragmentTeamDashboardBinding::inflate) {

    override val thisPage by lazy { NavigationManager.Page.TEAM_DASHBOARD }

    private lateinit var statisticsViewModel: StatisticsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statsFactory = InjectorUtils.provideStatisticsFactory(requireContext())
        statisticsViewModel =
            ViewModelProvider(this, statsFactory)[StatisticsViewModel::class.java]

        getTeamWithTargetsAchieved()
    }

    private fun getTeamWithTargetsAchieved() {
        statisticsViewModel.getTeam { result ->
            statisticsViewModel.viewModelScope.launch {
                useBinding { binding ->
                    result
                        .onSuccess { team ->
                            /**
                             * by default 'stockInfo' is not visible
                             *   most places that reuse the 'stockTotalCard'
                             *   do not need it visible when first opening a page
                             */
                            binding.stockTotalCard.stockInfo.visibility = View.VISIBLE

                            val totalTeamStock = team.team.sumOf {
                                it.stockBalance.balance.toDoubleOrNull() ?: 0.0
                            }
                            binding.stockTotalCard.stockTitle.text =
                                getString(R.string.stock_title, getString(R.string.team))
                            binding.stockTotalCard.currentSummaryStock.text =
                                Formatter.formatCustomCurrency(totalTeamStock)

                            val totalTargets = team.team.sumOf { it.salesTarget?.weeklyAmount?.toDoubleOrNull() ?: 0.0 }
                            if (totalTargets != 0.0) {
                                binding.salesTargetCard.totalSalesTargetTeamValue.text =
                                    Formatter.formatCustomCurrency(totalTargets)
                            }

                            val membersHavingWeeklyTargets = team.team.count {
                                it.salesTarget?.weeklyAmount != null
                            }
                            binding.salesTargetCard.totalMembersWithSalesTarget.text = getString(
                                R.string.total_members_with_sales_target,
                                membersHavingWeeklyTargets.toString(),
                                team.count.toString()
                            )

                            binding.teamInfoCard.teamMemberCount.text = team.count.toString()

                            getTargetsAchievedPercent(team)
                        }
                        .onFailure {
                            Log.e(_tag, "Team Dashboard load error: " + it.message.toString())
                            showError()
                        }
                }
            }
        }
    }

    private fun getTargetsAchievedPercent(team: TeamModel) {
        val requestList = team.team.map {
            val ssRequest = MasRepository.SalesSummaryRequest(
                MasRepository.SalesSummaryType.MEMBER,
                MasRepository.SalesSummaryPeriod.WEEKLY,
                MasRepository.SalesSummaryIntervalAge(0),
                it.msisdn
            )
            return@map if (it.salesTarget?.weeklyAmount != null) ssRequest else null
        }.filterNotNull()

        val totalTargets = team.team.sumOf { it.salesTarget?.weeklyAmount?.toDoubleOrNull() ?: 0.0 }

        statisticsViewModel.getSalesSummaryOnce(requestList) { result ->
            statisticsViewModel.viewModelScope.launch {
                result
                    .onFailure { showError() }
                    .onSuccess { summaryResult ->
                        useBinding { binding ->
                            val totalAirtimeSalesForTheWeek = summaryResult.sumOf {
                                it.value.airtimeSalesValue.toDoubleOrNull() ?: 0.0
                            }
                            val totalBundleSalesForTheWeek = summaryResult.sumOf {
                                it.value.airtimeSalesValue.toDoubleOrNull() ?: 0.0
                            }

                            val totalTeamSalesForThisWeek =
                                totalAirtimeSalesForTheWeek.plus(totalBundleSalesForTheWeek)

                            val teamWeeklySalesTargetAchievedPercentage =
                                if (totalTargets == 0.0) 0.0
                                else {
                                    binding.salesTargetCard.teamAchievedSalesTarget.text =
                                        Formatter.formatCustomCurrency(totalTeamSalesForThisWeek)
                                    totalTeamSalesForThisWeek.div(totalTargets).times(100)
                                }

                            binding.salesTargetCard.teamAchievedSalesTargetPercentage.text =
                                Formatter.combine(
                                    String.format("%.2f", teamWeeklySalesTargetAchievedPercentage),
                                    " (%)"
                                )

                            showPage()
                        }
                    }
            }
        }
    }


    private fun showPage() {
        useBinding { binding ->
            binding.progressBar.visibility = View.GONE
            binding.teamInfoWrapper.visibility = View.VISIBLE
        }
    }

    private fun showError() {
        useBinding { binding ->
            binding.progressBar.visibility = View.GONE
            binding.dashboardLoadError.visibility = View.VISIBLE
        }
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of fragment StatsFragment.
         */
        @JvmStatic
        fun newInstance() = TeamDashboardFragment().apply { arguments = Bundle() }
    }
}
