package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import systems.concurrent.crediversemobile.repositories.BundleRepository
import systems.concurrent.crediversemobile.repositories.MasRepository

// The same repository that's needed for QuotesViewModel
// is also passed to the factory
class BundleViewModelFactory(private val bundleRepository: BundleRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BundleViewModel(bundleRepository) as T
    }
}
