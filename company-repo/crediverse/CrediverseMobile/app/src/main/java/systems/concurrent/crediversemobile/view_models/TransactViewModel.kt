package systems.concurrent.crediversemobile.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.models.SellAirtimeResponse
import systems.concurrent.crediversemobile.repositories.BundleRepository
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.utils.CSResult

class TransactViewModel : ViewModel {
    private var masRepository: MasRepository? = null
    private var bundleRepository: BundleRepository? = null

    constructor(repository: BundleRepository) : super() {
        bundleRepository = repository
    }

    constructor(repository: MasRepository) : super() {
        masRepository = repository
    }

    fun sellAirtime(
        amount: Double, msisdn: String,
        ready: ((CSResult<SellAirtimeResponse>) -> Unit)? = null
    ) {
        if (masRepository == null) Log.e(_tag, "Missing MAS Repository in TransactViewModel.sellAirtime(...) method")
        masRepository?.sellAirtime(amount, msisdn) { result ->
            viewModelScope.launch { ready?.invoke(result) }
        }
    }

    fun transferCredit(
        amount: Double, msisdn: String,
        ready: ((CSResult<SellAirtimeResponse>) -> Unit)? = null
    ) {
        if (masRepository == null) Log.e(_tag, "Missing MAS Repository in TransactViewModel.transferCredit(...) method")
        masRepository?.transferCredit(amount, msisdn) { result ->
            viewModelScope.launch { ready?.invoke(result) }
        }
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName
    }
}