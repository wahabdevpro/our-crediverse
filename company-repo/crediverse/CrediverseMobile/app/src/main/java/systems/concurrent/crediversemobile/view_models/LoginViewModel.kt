package systems.concurrent.crediversemobile.view_models

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.models.BalancesResponseModel
import systems.concurrent.crediversemobile.models.VersionStatus
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.utils.*

class LoginViewModel(private val repository: MasRepository) : ViewModel() {
    fun getVersionStatus(activity: Activity, callback: (CSResult<VersionStatus>) -> Unit) {
        repository.getVersionStatus(activity) {
            viewModelScope.launch { callback(it) }
        }
    }
}