package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.models.TransactionModel
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.utils.CSResult

class TransactionHistoryViewModel(private val repository: MasRepository) : ViewModel() {
    companion object {
        private var useCachedValue = false

        fun resetCache() {
            useCachedValue = false
        }
    }

    fun getNextPage(
        transactionsPerPage: Int, pageNumber: Int,
        callback: (CSResult<List<TransactionModel>>) -> Unit
    ) {
        repository.getTransactionHistory(transactionsPerPage, pageNumber) { txListResult ->
            viewModelScope.launch {
                callback(txListResult)
            }
        }
    }
}