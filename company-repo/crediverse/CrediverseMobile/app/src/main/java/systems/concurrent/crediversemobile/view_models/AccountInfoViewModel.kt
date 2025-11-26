package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.models.AccountInfoResponseModel
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.utils.CSResult
import systems.concurrent.crediversemobile.utils.isSuccess

class AccountInfoViewModel(private val repository: MasRepository) : ViewModel() {

    companion object {
        private var useCachedValues = false

        private val accountInfoLiveData: MutableLiveData<CSResult<AccountInfoResponseModel>> by lazy {
            MutableLiveData<CSResult<AccountInfoResponseModel>>()
        }

        fun resetCache() {
            useCachedValues = false
        }
    }

    fun getAccountInfo() = accountInfoLiveData.value

    fun updateAccountInfo(
        accountInfo: AccountInfoResponseModel,
        onReady: ((CSResult<AccountInfoResponseModel>) -> Unit)? = null
    ) {
        repository.updateAccountInfo(accountInfo) { accountInfoResult ->
            viewModelScope.launch {
                accountInfoLiveData.value = accountInfoResult
                useCachedValues = accountInfoResult.isSuccess
                onReady?.invoke(accountInfoResult)
            }
        }
    }

    fun getAccountInfoLiveData(
        onReady: ((CSResult<AccountInfoResponseModel>) -> Unit)? = null
    ): LiveData<CSResult<AccountInfoResponseModel>> {
        if (!useCachedValues) {
            repository.getAccountInfo {
                viewModelScope.launch {
                    accountInfoLiveData.value = it
                    useCachedValues = it.isSuccess
                    onReady?.invoke(it)
                }
            }
        }
        return accountInfoLiveData
    }
}