package systems.concurrent.crediversemobile.repositories

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.models.BundleModel
import systems.concurrent.crediversemobile.models.SmartshopBundlesListGet
import systems.concurrent.crediversemobile.models.SsApiErrorModel
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.bundleSaleEntry
import systems.concurrent.crediversemobile.utils.*

class BundleRepository {
    private val _tag = this::class.java.kotlin.simpleName

    private val sandboxRepository = SandboxRepository()

    enum class ErrorMessages(private val _resource: Int) {
        /* GENERIC ERRORS */
        INVALID_AMOUNT(R.string.error_invalid_amount),
        INSUFFICIENT_FUNDS(R.string.error_insufficient_funds),
        PROVISION_REJECTION(R.string.bundle_provision_failed),
        LISTING_FAILED(R.string.bundle_listing_failed),
        PIN_INVALID(R.string.bundle_pin_invalid),

        /* General/Auth Errors */
        TIMEOUT(R.string.connection_timeout),
        UNAVAILABLE(R.string.unable_to_connect),
        FORBIDDEN(R.string.auth_forbidden),
        PERMISSION_DENIED(R.string.auth_permission_denied),
        UNAUTHORIZED(R.string.auth_forbidden),
        REQUEST_ERROR(R.string.request_error),
        INTERNAL_SERVER_ERROR(R.string.internal_server_error);

        val resource get() = _resource

        companion object {
            fun getResourceOrDefault(
                name: String, defaultResource: Int = INTERNAL_SERVER_ERROR.resource
            ): Int {
                return valueOfOrNull(name)?.resource ?: defaultResource
            }

            private fun valueOfOrNull(name: String): ErrorMessages? {
                return values().firstOrNull { it.name == name }
            }
        }
    }

    private fun <T> handleSpecialResultOrDefault(e: Throwable): CSResult<T> {
        Log.e(_tag, "Problem in request.  Error: ${e.message}")

        var finalReason = ErrorMessages.INTERNAL_SERVER_ERROR
        var ssApiError: SsApiErrorModel? = null
        try {
            ssApiError = gson.fromJson(e.message, SsApiErrorModel::class.java)
        } catch (exception: Exception) {
            Log.w(_tag, "Unable to parse error from JSON... will use default")
            if (e.message?.contains("timeout", ignoreCase = true) == true) {
                finalReason = ErrorMessages.TIMEOUT
            } else if (e.message?.contains("failed to connect", ignoreCase = true) == true) {
                finalReason = ErrorMessages.UNAVAILABLE
            }
        }

        var ssApiErrorCode = 9999

        if (ssApiError?.error?.message?.isNotEmpty() == true && ssApiError.error?.code != null) {
            ssApiErrorCode = ssApiError.error!!.code
            val message = ssApiError.error!!.message
            Log.w(_tag, "ssApiError.message: $message")

            finalReason = when (ssApiErrorCode) {
                110 -> ErrorMessages.INTERNAL_SERVER_ERROR
                in 111..116, 118 -> ErrorMessages.REQUEST_ERROR
                120, 141 -> {
                    NavigationManager.runOnNavigationUIThread {
                        LogoutManager.forceLogout()
                    }
                    ErrorMessages.UNAUTHORIZED
                }
                130 -> ErrorMessages.INSUFFICIENT_FUNDS
                140 -> ErrorMessages.PIN_INVALID
                800 -> ErrorMessages.PROVISION_REJECTION
                801 -> ErrorMessages.LISTING_FAILED
                900 -> ErrorMessages.UNAVAILABLE
                else -> ErrorMessages.INTERNAL_SERVER_ERROR
            }

            if (e.message?.contains("timeout", ignoreCase = true) == true) {
                finalReason = ErrorMessages.TIMEOUT
            }

            Log.w(_tag, "handleSpecialResultOrReason(...) Error: $finalReason")
        }

        return CSResult.Failure(CSException(finalReason, ssApiErrorCode))
    }

    fun getBundles(msisdn: String, callback: (CSResult<SmartshopBundlesListGet>) -> Unit) {
        val addEvent = { result: CSResult<SmartshopBundlesListGet> ->
            when {
                result.isSuccess -> AppAnalyticsService.addSuccessfulActionEvent(AppAnalyticsService.getBundleListEntry)
                result.isFailure -> AppAnalyticsService.addFailedActionEvent(AppAnalyticsService.getBundleListEntry)
            }
        }

        if (SandboxRepository.isSandboxEnabled) {
            val result = sandboxRepository.getBundles(msisdn)
            addEvent(result)
            callback(result)
            return
        }

        try {
            bundleService.getBundles(msisdn, MasService.getLoginToken() ?: "") { result ->
                addEvent(result.toCSResult())
                result.onFailure {
                    callback(handleSpecialResultOrDefault(it))
                }.onSuccess { bundles ->
                    callback(CSResult.Success(bundles))
                }
            }
        } catch (e: Exception) {
            AppAnalyticsService.addExceptionEvent(e, AppAnalyticsService.getBundleListEntry)
            Log.e(_tag, "getBundles(...) Error: ${e.message}")
            callback(handleSpecialResultOrDefault(e))
        }
    }

    fun sellBundle(
        bundleSaleData: BundleService.BundleSaleData, chargeAmount: Double,
        callback: (CSResult<BundleModel>) -> Unit
    ) {
        val coordinates = LocationService.getSsApiLocation()
        val failData = mapOf(
            "name" to bundleSaleData.bundleName,
            "beneficiary" to bundleSaleData.beneficiary,
            "amount" to chargeAmount.toString(),
        )

        val addEvent = { result: CSResult<BundleModel> ->
            when {
                result.isSuccess -> AppAnalyticsService.addSuccessfulActionEvent(bundleSaleEntry)
                result.isFailure -> AppAnalyticsService.addFailedActionEvent(
                    bundleSaleEntry, failData
                )
            }
        }

        if (SandboxRepository.isSandboxEnabled) {
            SandboxRepository.delayedCallback {
                val result = sandboxRepository.sellBundle(bundleSaleData, chargeAmount)
                addEvent(result)
                result.onSuccess {
                    callback(CSResult.Success(it))
                }.onFailure {
                    callback(handleSpecialResultOrDefault(it))
                }
            }
            return
        }

        try {
            bundleService.sellBundle(
                bundleSaleData, MasService.getLoginToken() ?: "", coordinates
            ) { result ->
                addEvent(result.toCSResult())
                result.onFailure {
                    callback(handleSpecialResultOrDefault(it))
                }.onSuccess { bundle ->
                    callback(CSResult.Success(bundle))
                }
            }
        } catch (e: Exception) {
            Log.e(_tag, "sellBundle(...) Error: ${e.message}")
            AppAnalyticsService.addExceptionEvent(e, bundleSaleEntry)
            callback(handleSpecialResultOrDefault(e))
        }
    }

    companion object {
        private val gson =
            GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        private val bundleService: BundleService = BundleService()
    }

}
