package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// The same repository that's needed for QuotesViewModel
// is also passed to the factory
class FromHistoryViewModelFactory() :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FromHistoryViewModel() as T
    }
}
