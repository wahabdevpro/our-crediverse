package systems.concurrent.crediversemobile.view_models

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.models.*
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.utils.*
import kotlin.onFailure

class StatisticsViewModel(private val repository: MasRepository) : ViewModel() {

    enum class LoadState { NEW, BUSY, DONE }

    companion object {
        private var isTeamLead: Result<Boolean>? = null
        private var teamMembership: CSResult<TeamMembership>? = null

        private var dailyFragmentLoading = LoadState.NEW
        private var weeklyFragmentLoading = LoadState.NEW
        private var monthlyFragmentLoading = LoadState.NEW

        fun resetLoadingStates() {
            dailyFragmentLoading = LoadState.NEW
            weeklyFragmentLoading = LoadState.NEW
            monthlyFragmentLoading = LoadState.NEW
        }
    }

    fun getLoadingState(period: MasRepository.SalesSummaryPeriod): LoadState {
        return when (period) {
            MasRepository.SalesSummaryPeriod.DAILY -> dailyFragmentLoading
            MasRepository.SalesSummaryPeriod.WEEKLY -> weeklyFragmentLoading
            MasRepository.SalesSummaryPeriod.MONTHLY -> monthlyFragmentLoading
        }
    }

    private val dailyFragmentStats: MutableLiveData<CSResult<List<SalesSummaryModel>>> by lazy {
        MutableLiveData<CSResult<List<SalesSummaryModel>>>()
    }

    private val weeklyFragmentStats: MutableLiveData<CSResult<List<SalesSummaryModel>>> by lazy {
        MutableLiveData<CSResult<List<SalesSummaryModel>>>()
    }

    private val monthlyFragmentStats: MutableLiveData<CSResult<List<SalesSummaryModel>>> by lazy {
        MutableLiveData<CSResult<List<SalesSummaryModel>>>()
    }

    private val saleLiveData: MutableLiveData<CSResult<SalesSummaryModel>> by lazy {
        MutableLiveData<CSResult<SalesSummaryModel>>()
    }

    private val salesLiveData: MutableLiveData<CSResult<List<SalesSummaryModel>>> by lazy {
        MutableLiveData<CSResult<List<SalesSummaryModel>>>()
    }

    fun getTeam(callback: (CSResult<TeamModel>) -> Unit) {
        repository.getTeam {
            viewModelScope.launch {
                callback(it)
            }
        }
    }

    fun updateFragmentLiveData(
        period: MasRepository.SalesSummaryPeriod,
        requests: List<MasRepository.SalesSummaryRequest>,
        callback: (() -> Unit)
    ) {
        when (period) {
            MasRepository.SalesSummaryPeriod.DAILY -> dailyFragmentLoading = LoadState.BUSY
            MasRepository.SalesSummaryPeriod.WEEKLY -> weeklyFragmentLoading = LoadState.BUSY
            MasRepository.SalesSummaryPeriod.MONTHLY -> monthlyFragmentLoading = LoadState.BUSY
        }

        repository.getSalesSummary(requests) {
            NavigationManager.runOnNavigationUIThread { _ ->
                when (period) {
                    MasRepository.SalesSummaryPeriod.DAILY -> {
                        dailyFragmentStats.value = it
                        dailyFragmentLoading = if (it.isFailure) LoadState.NEW else LoadState.DONE
                    }
                    MasRepository.SalesSummaryPeriod.WEEKLY -> {
                        weeklyFragmentStats.value = it
                        weeklyFragmentLoading = if (it.isFailure) LoadState.NEW else LoadState.DONE
                    }
                    MasRepository.SalesSummaryPeriod.MONTHLY -> {
                        monthlyFragmentStats.value = it
                        monthlyFragmentLoading = if (it.isFailure) LoadState.NEW else LoadState.DONE
                    }
                }
                callback()
            }
        }
    }

    fun getFragmentLiveData(period: MasRepository.SalesSummaryPeriod): LiveData<CSResult<List<SalesSummaryModel>>> {
        return when (period) {
            MasRepository.SalesSummaryPeriod.DAILY -> dailyFragmentStats
            MasRepository.SalesSummaryPeriod.WEEKLY -> weeklyFragmentStats
            MasRepository.SalesSummaryPeriod.MONTHLY -> monthlyFragmentStats
        }
    }

    fun isTeamLead(
        activity: Activity = NavigationManager.getActivity(),
        onReady: ((CSResult<Boolean>) -> Unit)? = null
    ) {
        repository.isTeamLead(activity) {
            viewModelScope.launch {
                onReady?.invoke(it)
            }
        }
    }

    fun setSalesTarget(
        salesTargetModel: SalesTargetModel, callback: (CSResult<SalesTargetModel>) -> Unit
    ) {
        repository.setSalesTarget(salesTargetModel) {
            viewModelScope.launch {
                getTeamMembership()
                callback(it)
            }
        }
    }

    fun getTeamMembership(onReady: ((CSResult<TeamMembership>) -> Unit)? = null) {
        repository.getTeamMembership { result ->
            viewModelScope.launch {
                teamMembership = result
                onReady?.invoke(result)
            }
        }
    }

    fun getSingleSaleLiveData(salesSummaryRequest: MasRepository.SalesSummaryRequest): LiveData<CSResult<SalesSummaryModel>> {
        repository.getSalesSummary(listOf(salesSummaryRequest)) { result ->
            viewModelScope.launch {
                result
                    .onSuccess { saleLiveData.value = CSResult.Success(it[0]) }
                    .onFailure { saleLiveData.value = CSResult.Failure(it) }
            }
        }
        return saleLiveData
    }

    fun getHourlySalesSummary(
        intervals: List<MasRepository.SalesSummaryIntervalAge>,
        callback: (CSResult<List<HourlySalesSummaryModel>>) -> Unit
    ) {
        repository.getHourlySalesSummary(intervals) {
            viewModelScope.launch {
                callback(it)
            }
        }
    }

    fun getSalesSummaryOnce(
        salesSummaryRequests: List<MasRepository.SalesSummaryRequest>,
        callback: (CSResult<List<SalesSummaryModel>>) -> Unit
    ) {
        repository.getSalesSummary(salesSummaryRequests) { result ->
            viewModelScope.launch {
                callback(result)
            }
        }
    }

    fun getSalesLiveData(salesSummaryRequests: List<MasRepository.SalesSummaryRequest>): LiveData<CSResult<List<SalesSummaryModel>>> {
        repository.getSalesSummary(salesSummaryRequests) { result ->
            viewModelScope.launch {
                salesLiveData.value = result
            }
        }
        return salesLiveData
    }
}