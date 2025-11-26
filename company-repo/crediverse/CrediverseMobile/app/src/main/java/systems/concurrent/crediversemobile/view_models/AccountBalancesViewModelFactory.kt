package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import systems.concurrent.crediversemobile.repositories.MasRepository

// The same repository that's needed for QuotesViewModel
// is also passed to the factory
class AccountBalancesViewModelFactory(private val repository: MasRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AccountBalancesViewModel(repository) as T
    }
}
