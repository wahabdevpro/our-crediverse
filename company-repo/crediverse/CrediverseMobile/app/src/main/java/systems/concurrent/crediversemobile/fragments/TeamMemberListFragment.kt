package systems.concurrent.crediversemobile.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentTeamMemberListBinding
import systems.concurrent.crediversemobile.recyclerViewAdapters.TeamMemberAdapter
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.StatisticsViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [TeamMemberListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeamMemberListFragment :
    ViewBindingFragment<FragmentTeamMemberListBinding>(FragmentTeamMemberListBinding::inflate) {

    override val thisPage by lazy { NavigationManager.Page.CHOOSE_TEAM_MEMBER }

    private lateinit var statisticsViewModel: StatisticsViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentTeamMemberListBinding.inflate(inflater, container, false)

        val statisticsFragmentFactory =
            InjectorUtils.provideStatisticsFactory(requireContext())
        statisticsViewModel = ViewModelProvider(
            this, statisticsFragmentFactory
        )[StatisticsViewModel::class.java]

        binding.currencySymbolLabel.text = "(${AppFlag.Currency.symbol})"

        getTeamInfo(binding)

        return binding.root
    }

    private fun getTeamInfo(binding: FragmentTeamMemberListBinding) {
        statisticsViewModel.viewModelScope.launch {
            statisticsViewModel.getTeam { teamResult ->
                teamResult.onSuccess { result ->
                    val layoutManager = LinearLayoutManager(requireContext())
                    binding.teamMembersRecycler.layoutManager = layoutManager

                    val adapter =
                        TeamMemberAdapter(requireContext(), result.team) { chosenTeamMember ->
                            Log.e(_tag, "chose team member: ${chosenTeamMember.firstName}")
                            val customTitle = Formatter.combine(
                                Formatter.adjustSizeRelative(
                                    getString(R.string.team_member_stats) + "\n", 0.7f
                                ),
                                "${chosenTeamMember.firstName} ${chosenTeamMember.surname} - ${chosenTeamMember.msisdn}"
                            )

                            NavigationManager.setPage(
                                NavigationManager.Page(
                                    NavigationManager.PageID.TEAM_MEMBER,
                                    R.id.third_nav, Icon.ID_CARD,
                                    TeamMemberStatsFragment(chosenTeamMember),
                                    R.string.team_member, customTitle
                                )
                            )
                        }

                    binding.teamMembersRecycler.adapter = adapter
                }.onFailure {
                    val finalString = it.getStringFromResourceOrDefault(requireContext())

                    Dialog(
                        NavigationManager.getActivity(), DialogType.ERROR, "", finalString
                    ).show()
                }

                activity?.runOnUiThread {
                    binding.teamMemberList.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of fragment ChooseTeamMemberStatsFragment.
         */
        @JvmStatic
        fun newInstance() = TeamMemberListFragment().apply { arguments = Bundle() }
    }
}
