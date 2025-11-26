package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.models.BalancesResponseModel
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.utils.*

class AccountBalancesViewModel(private val repository: MasRepository) : ViewModel() {
    companion object {
        private var useCachedValue = false

        private val allBalancesLiveData: MutableLiveData<CSResult<BalancesResponseModel>> by lazy {
            MutableLiveData<CSResult<BalancesResponseModel>>()
        }

        fun resetCache() {
            useCachedValue = false
        }
    }

    fun updateBalancesLiveData(ready: ((CSResult<BalancesResponseModel>) -> Unit)? = null) {
        repository.getBalances { result ->
            viewModelScope.launch {
                allBalancesLiveData.postValue(result)
                useCachedValue = result.isSuccess
                allBalancesLiveData.value?.let { value -> ready?.invoke(value) }
            }
        }
    }

    val balances get(): CSResult<BalancesResponseModel>? = allBalancesLiveData.value

    fun getBalancesLiveDataOnce(ready: (CSResult<BalancesResponseModel>) -> Unit) {
        updateBalancesLiveData {
            ready(it)
        }
    }

    fun getBalancesLiveData(): LiveData<CSResult<BalancesResponseModel>> {
        if (!useCachedValue) {
            repository.getBalances {
                NavigationManager.runOnNavigationUIThread { _ ->
                    allBalancesLiveData.postValue(it)
                    useCachedValue = it.isSuccess
                }
            }
        }
        return allBalancesLiveData
    }
}