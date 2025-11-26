package systems.concurrent.crediversemobile.utils

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.fragments.*
import systems.concurrent.crediversemobile.recyclerViewAdapters.NavigationOverlayAdapter
import systems.concurrent.crediversemobile.repositories.MasRepository

class NavigationManager {

    enum class PageID(private val canDirectlyNavigateHere: Boolean = true) {
        HOME,
        AIRTIME, TRANSFER, BUNDLE, MM_DEPOSIT, MM_WITHDRAW, HISTORY,
        TEAM_DASHBOARD, EXECU_STATS, CHOOSE_TEAM_MEMBER, TEAM_MEMBER_MAP,
        TEAM_MEMBER(canDirectlyNavigateHere = false),
        STATS,
        BUY_WITH_MOBILE_MONEY;

        /**
         * TODO: Not used yet ...
         *       Soon, we will allow structuring a CUSTOM nav nesting with overlays by means of a JSON tree in feature toggles
         *       This custom nav tree will allow values from this PageID Enum ...
         *       But some items should not allow direct navigation from the bottom-navbar ... hence this method
         */
        val hasDirectNav get() = canDirectlyNavigateHere
    }

    data class Page(
        private val featureToggleIdentifier: PageID,
        private var _fragmentId: Int, val icon: Icon, private var _fragment: Fragment?,
        private var _navNameResource: Int, private var _actionBarTitle: CharSequence,
        private var _customNavigationAction: (() -> Unit)? = null
    ) {
        companion object {
            /** Home */
            val HOME by lazy {
                Page(
                    PageID.HOME,
                    R.id.first_nav, Icon.HOUSE, Home(), R.string.home,
                    getActivity().getString(R.string.my_dashboard)
                )
            }

            /** Transact */
            val AIRTIME by lazy {
                Page(
                    PageID.AIRTIME,
                    R.id.second_nav, Icon.RETRO_MOBILE, AirtimeFragment(), R.string.airtime,
                    getActivity().getString(R.string.sell_airtime)
                )
            }
            val TRANSFER by lazy {
                Page(
                    PageID.TRANSFER,
                    R.id.second_nav, Icon.ARROW_RIGHT_ARROW_LEFT, TransferFragment(),
                    R.string.transfer, getActivity().getString(R.string.transfer_credit)
                )
            }
            val BUNDLE by lazy {
                Page(
                    PageID.BUNDLE,
                    R.id.second_nav, Icon.GIFT, BundleLandingFragment(),
                    R.string.bundle, getActivity().getString(R.string.sell_bundle)
                )
            }
            val MM_DEPOSIT by lazy {
                Page(
                    PageID.MM_DEPOSIT,
                    R.id.second_nav, Icon.ARROW_RIGHT_TO_BRACKET,
                    MobileMoneyCashInFragment(), R.string.mm_cash_in_btn,
                    getActivity().getString(R.string.mm_cash_in)
                )
            }
            val MM_WITHDRAW by lazy {
                Page(
                    PageID.MM_WITHDRAW,
                    R.id.second_nav, Icon.ARROW_UP_FROM_BRACKET,
                    MobileMoneyCashOutFragment(), R.string.mm_cash_out_btn,
                    getActivity().getString(R.string.mm_cash_out)
                )
            }
            val HISTORY by lazy {
                Page(
                    PageID.HISTORY,
                    R.id.second_nav, Icon.CLICK_ROTATE_LEFT, HistoryFragment(), R.string.history,
                    getActivity().getString(R.string.history_title)
                )
            }

            /** Team */
            val TEAM_DASHBOARD by lazy {
                Page(
                    PageID.TEAM_DASHBOARD,
                    R.id.third_nav, Icon.USERS, TeamDashboardFragment(), R.string.my_team,
                    getActivity().getString(R.string.team_dashboard)
                )
            }
            val EXECU_STATS by lazy {
                Page(
                    PageID.EXECU_STATS,
                    R.id.fourth_nav, Icon.CHART_PIE, ExecuStatsFragment(), R.string.execu_stats,
                    getActivity().getString(R.string.execu_stats)
                )
            }
            val CHOOSE_TEAM_MEMBER by lazy {
                Page(
                    PageID.CHOOSE_TEAM_MEMBER, R.id.third_nav, Icon.ID_CARD,
                    TeamMemberListFragment(), R.string.team_members,
                    getActivity().getString(R.string.team_members)
                )
            }
            val TEAM_MEMBER_MAP by lazy {
                Page(
                    PageID.TEAM_MEMBER_MAP, R.id.third_nav, Icon.MAP_LOCATION_DOT,
                    null, R.string.map_location,
                    getActivity().getString(R.string.map_location)
                )
            }

            /** Stats */
            val STATS by lazy {
                Page(
                    PageID.STATS,
                    R.id.third_nav, Icon.CHART_COLUMN,
                    StatsFragment(MasRepository.SalesSummaryType.SELF), R.string.stats,
                    getActivity().getString(R.string.my_stats)
                )
            }

            /** Buy Mobile Money */
            val BUY_MOBILE_MONEY by lazy {
                Page(
                    PageID.BUY_WITH_MOBILE_MONEY, 0, Icon.WALLET, null, 0,
                    getActivity().getString(R.string.buy_credit),
                )
            }
        }

        private var _customNavigationActionHighlightsIcon = false

        val pageId get() = featureToggleIdentifier

        fun setCustomNavigationAction(
            shouldHighlightIcon: Boolean = false,
            action: () -> Unit
        ): Page {
            _customNavigationAction = action
            _customNavigationActionHighlightsIcon = shouldHighlightIcon
            return this
        }

        fun doNavigationAction(): Boolean {
            return if (_customNavigationAction == null) {
                setPage(this, addToHistory = true)
                EventUtil.EVENT_CONSUMED
            } else {
                _customNavigationAction!!.invoke()
                return if (_customNavigationActionHighlightsIcon) EventUtil.EVENT_CONSUMED
                else EventUtil.EVENT_NOT_CONSUMED
            }
        }

        fun updateFragmentId(newFragmentId: Int): Page {
            return if (newFragmentId in listOf(
                    R.id.first_nav, R.id.second_nav, R.id.third_nav,
                    R.id.fourth_nav, R.id.fifth_nav
                )
            ) {
                Page(
                    this.featureToggleIdentifier,
                    newFragmentId, this.icon, this._fragment,
                    this._navNameResource, this._actionBarTitle
                )
            } else this

        }

        fun setActionBarTitle(newTitle: CharSequence? = null): Page {
            return Page(
                this.featureToggleIdentifier,
                this._fragmentId, this.icon, this._fragment,
                this._navNameResource, newTitle ?: this._actionBarTitle
            )
        }

        fun setNavResource(newNavResource: Int): Page {
            return Page(
                this.featureToggleIdentifier,
                this._fragmentId, this.icon, this._fragment,
                newNavResource, this._actionBarTitle
            )
        }

        fun setFragment(fragment: Fragment): Page {
            return Page(
                this.featureToggleIdentifier,
                this._fragmentId, this.icon, fragment,
                this._navNameResource, this._actionBarTitle
            )
        }

        override fun toString() = pageId.toString()

        val fragmentId get() = this._fragmentId
        val fragment get() = this._fragment
        val actionBarTitle get() = this._actionBarTitle
        val navNameResource get() = this._navNameResource

    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        val navigationPages: MutableMap<Int, NavigationOverlayAdapter.OverlayData>
            get() {
                /**
                 * This is where all 'bottom navigation' and any extended navigation is decided by feature toggle
                 */

                var finalNavList = mutableMapOf(
                    R.id.first_nav to NavigationOverlayAdapter.OverlayData(
                        R.string.home, Icon.HOUSE, listOf(Page.HOME)
                    ),
                )

                var transactPages = listOf(
                    Page.AIRTIME,
                    Page.BUNDLE,
                    Page.TRANSFER,
                    Page.MM_DEPOSIT,
                    Page.MM_WITHDRAW
                )

                transactPages = transactPages.filter {
                    when (it.pageId) {
                        PageID.BUNDLE -> FeatureToggle.Nav.hasBundlePage
                        PageID.TRANSFER -> FeatureToggle.Nav.hasTransferPage
                        PageID.MM_DEPOSIT -> FeatureToggle.Nav.hasMobileMoneyDeposit
                        PageID.MM_WITHDRAW -> FeatureToggle.Nav.hasMobileMoneyWithdraw
                        else -> true
                    }
                }

                finalNavList[R.id.second_nav] =
                        /**
                         * If more than 1 page ... place History in the 'special' position
                         */
                    if (transactPages.size > 1)
                        NavigationOverlayAdapter.OverlayData(
                            R.string.transact,
                            Icon.ARROW_RIGHT_ARROW_LEFT,
                            transactPages,
                            Page.HISTORY,
                        )
                    /**
                     * If only 1 page ... place History alongside other pages
                     */
                    else
                        NavigationOverlayAdapter.OverlayData(
                            R.string.transact, Icon.ARROW_RIGHT_ARROW_LEFT,
                            transactPages + listOf(Page.HISTORY)
                        )

                val teamPage = Page.STATS
                    .setFragment(StatsFragment(MasRepository.SalesSummaryType.TEAM))
                    .setNavResource(R.string.team_stats)
                    .setActionBarTitle(getActivity().getString(R.string.team_stats))

                if (FeatureToggle.Nav.hasTeamPages && isTeamLead) {
                    val teamNavList = mutableListOf(
                        Page.TEAM_DASHBOARD.setNavResource(R.string.team_dashboard),
                        teamPage,
                        Page.CHOOSE_TEAM_MEMBER,
                    )
                    if (FeatureToggle.Nav.Team.membersLocationMap) {
                        teamNavList.add(Page.TEAM_MEMBER_MAP)
                    }

                    finalNavList[R.id.third_nav] = NavigationOverlayAdapter.OverlayData(
                        R.string.teams, Icon.USERS, teamNavList
                    )
                }

                if (FeatureToggle.Nav.hasStatsPage || FeatureToggle.Nav.hasExecuStatsPage) {
                    val statsPages = mutableListOf<Page>()

                    if (FeatureToggle.Nav.hasExecuStatsPage) {
                        statsPages.add(Page.EXECU_STATS)
                    }

                    if (FeatureToggle.Nav.hasStatsPage) {
                        statsPages.add(
                            Page.STATS.setFragment(StatsFragment(MasRepository.SalesSummaryType.SELF))
                                .updateFragmentId(R.id.fourth_nav)
                                .setNavResource(R.string.my_stats),
                        )
                    }

                    finalNavList[R.id.fourth_nav] = NavigationOverlayAdapter.OverlayData(
                        R.string.stats, Icon.CHART_COLUMN, statsPages
                    )
                }

                if (FeatureToggle.Nav.canBuyWithMobileMoney) {
                    finalNavList[R.id.fifth_nav] = NavigationOverlayAdapter.OverlayData(
                        R.string.buy_credit, Icon.WALLET,
                        listOf(Page.BUY_MOBILE_MONEY),
                        specialPage = null, heading = null,
                    )
                }

                finalNavList = flattenNavigationIfPossible(finalNavList)

                return finalNavList
            }

        private fun flattenNavigationIfPossible(finalNavList: MutableMap<Int, NavigationOverlayAdapter.OverlayData>): MutableMap<Int, NavigationOverlayAdapter.OverlayData> {
            /**
             * There is a hardcoded MAX of 5 navigation endpoints on the BottomNavigationBar
             * if there are more than 5 ... Exit, as we cannot flatten the structure
             */
            val numberOfNavEndpoints = finalNavList.map {
                val specialPageCount = if (it.value.specialPage != null) 1 else 0
                it.value.pages.size + specialPageCount
            }.reduce { acc, i -> acc + i }

            if (numberOfNavEndpoints > 5) return finalNavList

            val navPositions = listOf(
                R.id.first_nav, R.id.second_nav, R.id.third_nav,
                R.id.fourth_nav, R.id.fifth_nav
            )

            var counter = 0
            val flatNav = mutableMapOf<Int, NavigationOverlayAdapter.OverlayData>()
            finalNavList.forEach {
                val pages = it.value.pages.toMutableList()
                if (it.value.specialPage != null) pages.add(it.value.specialPage!!)
                if (pages.isEmpty()) return@forEach
                pages.forEach { page ->
                    Log.e(
                        _tag,
                        "NAV POSITION $counter - ${getActivity().getString(page.navNameResource)}"
                    )
                    flatNav[navPositions[counter]] = NavigationOverlayAdapter.OverlayData(
                        page.navNameResource, page.icon, listOf(page)
                    )
                    counter += 1
                }
            }

            return flatNav
        }

        private val history = CustomUtils.History<Page>()

        private val currentPageTitle: MutableLiveData<CharSequence> by lazy {
            MutableLiveData<CharSequence>()
        }

        private val currentPage: MutableLiveData<Page> by lazy {
            MutableLiveData<Page>()
        }

        private var navigationActivity: NavigationActivity? = null
        private var _isTeamLead: Boolean = false
        private val isTeamLead get() = _isTeamLead

        fun setIsTeamLead(isTeamLead: Boolean) {
            _isTeamLead = isTeamLead
        }

        fun setNavigationActivity(na: NavigationActivity) {
            navigationActivity = na
        }

        fun runOnNavigationUIThread(callback: (activity: NavigationActivity) -> Unit) {
            val activity = getActivity()
            activity.runOnUiThread { callback(activity) }
        }

        fun getActivity(): NavigationActivity {
            if (navigationActivity == null) {
                throw Exception("Failed to setup the Navigation Manager ... needs a NavigationActivity?")
            }
            return navigationActivity as NavigationActivity
        }

        fun getPageTitle(): LiveData<CharSequence> {
            currentPageTitle.value =
                currentPageTitle.value ?: getActivity().getString(R.string.home)

            return currentPageTitle
        }

        fun getPage(): LiveData<Page> {
            currentPage.value = currentPage.value ?: Page.HOME

            return currentPage
        }

        fun clearHistory() {
            currentPage.value = null
            history.reset()
        }

        fun hasHistory(): Boolean {
            return history.size > 0
        }

        fun gotoPreviousPage(): Page? {
            if (!hasHistory()) return null

            val previousPage = history.pop()
            if (previousPage != null) setCurrentPage(previousPage)

            return previousPage
        }

        fun setPageTitle(newTitle: CharSequence) {
            currentPageTitle.value = newTitle
        }

        private fun setCurrentPage(page: Page) {
            currentPage.value = page
        }

        fun resumePage(page: Page) {
            if (currentPage.value?.pageId != page.pageId)
                setCurrentPage(page)
        }

        fun setPage(page: Page, addToHistory: Boolean = true) {
            val currentPageSameAsRequestedPage = currentPage.value == page
            if (currentPageSameAsRequestedPage) return

            if (addToHistory && currentPage.value != null) {
                history.push(currentPage.value!!)
            }

            setCurrentPage(page)
        }
    }
}
