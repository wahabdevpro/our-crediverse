package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import systems.concurrent.crediversemobile.models.SmartshopBundlesListGet
import systems.concurrent.crediversemobile.repositories.BundleRepository
import systems.concurrent.crediversemobile.utils.CSResult
import systems.concurrent.crediversemobile.utils.onFailure
import systems.concurrent.crediversemobile.utils.onSuccess

class BundleViewModel(private val bundleRepository: BundleRepository) : ViewModel() {
    companion object {
        private val bundles: MutableLiveData<CSResult<SmartshopBundlesListGet>> by lazy {
            MutableLiveData<CSResult<SmartshopBundlesListGet>>()
        }
    }

    fun updateBundlesListLiveData(msisdn: String, onCompleteCallback: (Boolean) -> Unit) {
        bundleRepository.getBundles(msisdn) { resultBundleList ->
            resultBundleList.onSuccess {
                bundles.postValue(CSResult.Success(it))
                onCompleteCallback(true)
            }.onFailure {
                bundles.postValue(CSResult.Failure(it))
                onCompleteCallback(false)
            }
        }
    }

    fun getBundlesLiveData(): LiveData<CSResult<SmartshopBundlesListGet>> {
        return bundles
    }
}