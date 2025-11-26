package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.models.MobileMoneyDepositResponse
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.utils.CSResult

class MobileMoneyViewModel(private val repository: MasRepository) : ViewModel() {

    fun mobileMoneyLogin(
        username: String, password: String,
        onReady: ((CSResult<Unit>) -> Unit)? = null
    ) {
        repository.mobileMoneyLogin(username, password) { result ->
            viewModelScope.launch {
                onReady?.invoke(result)
            }
        }
    }

    fun mobileMoneyDeposit(
        amount: Double, msisdn: String,
        onReady: ((CSResult<MobileMoneyDepositResponse>) -> Unit)? = null
    ) {
        repository.mobileMoneyDeposit(amount, msisdn) { result ->
            viewModelScope.launch {
                onReady?.invoke(result)
            }
        }
    }

    fun mobileMoneyWithdraw(
        amount: Double, fromMsisdn: String,
        onReady: ((CSResult<Unit>) -> Unit)? = null
    ) {
        repository.mobileMoneyWithdrawal(amount, fromMsisdn) { result ->
            viewModelScope.launch {
                onReady?.invoke(result)
            }
        }
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName
    }
}