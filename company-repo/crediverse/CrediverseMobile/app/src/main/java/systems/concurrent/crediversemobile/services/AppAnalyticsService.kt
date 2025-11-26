package systems.concurrent.crediversemobile.services

import android.util.Log
import com.google.gson.Gson
import systems.concurrent.crediversemobile.utils.AppFlag
import systems.concurrent.crediversemobile.utils.CSResult
import systems.concurrent.crediversemobile.utils.CustomUtils
import systems.concurrent.masapi.MasApi

class AppAnalyticsService {
    object Event {
        const val UPGRADE_REQUIRED = "upgradeRequired"
        const val FORCED_LOGOUT = "forcedLogout"
    }

    enum class AnalyticsEventType {
        CRASH, EXCEPTION, ACTION;

        fun toMasApiType(): MasApi.AnalyticsEventType {
            return MasApi.AnalyticsEventType.valueOf(this.toString())
        }
    }

    data class AnalyticsEvent(val type: AnalyticsEventType, val entry: AnalyticsEntry) {
        private var ts = CustomUtils.nowEpoch()

        fun toMasApiAnalyticsEvent(): MasApi.AnalyticsEvent? {
            val type = type.toMasApiType()

            return MasApi.AnalyticsEvent.newBuilder()
                .setType(type).setTime(ts).setContent(entry.toString())
                .build()
        }
    }

    enum class Result { Success, Failure }
    enum class What {
        Login, Logout, SubmitOTP,
        GetBalances, GetBundleList, GetTXHistory,
        Nav, Menu,
        AirtimeSale, Transfer, BundleSale,
    }

    data class AnalyticsEntry(val what: What? = null) {
        private var endpoint: String? = null
        private var result: Result? = null
        private var data: Map<String, *>? = null

        override fun toString(): String {
            return Gson().toJson(this)
        }

        fun withEndpoint(endpoint: String): AnalyticsEntry {
            this.endpoint = endpoint
            return this
        }

        fun isSuccess() = result == Result.Success
        fun isFailure() = result == Result.Failure

        fun asSuccess() = withResult(Result.Success)
        fun asFailure() = withResult(Result.Failure)
        private fun withResult(result: Result): AnalyticsEntry {
            this.result = result
            return this
        }

        fun withReason(reason: String): AnalyticsEntry {
            this.data = mapOf("reason" to reason)
            return this
        }

        fun withData(dataList: Map<String, *>?): AnalyticsEntry {
            this.data = dataList
            if (this.data?.isEmpty() == true) this.data = null
            return this
        }
    }

    companion object {
        private var writeEventsCallback:
                ((List<AnalyticsEvent>, (CSResult<Int>) -> Unit) -> Unit)? = null

        fun setWriteCallback(callback: (List<AnalyticsEvent>, onDone: (CSResult<Int>) -> Unit) -> Unit) {
            writeEventsCallback = callback
        }

        fun submitAnalyticsEvents(onReady: (() -> Unit)? = null) {
            if (writeEventsCallback == null || events.isEmpty()) {
                onReady?.invoke()
                return
            }

            val sentEvents = events.toList()
            events.clear()

            /**
             * FIXME -- right now we clear the events even if it FAILED...
             *          This would mean the 'submit' to MAS action failed ... so we LOST those events
             */
            writeEventsCallback!!.invoke(sentEvents) {
                val message = "Submitted all events!! ${Gson().toJson(sentEvents.toString())}"
                Log.e(_tag, message)
                onReady?.invoke()
            }
        }

        fun addNavigationEvent(endpoint: String) {
            addEvent(
                AnalyticsEventType.ACTION, success = null, navEntry.withEndpoint(endpoint)
            )
        }

        fun addSuccessfulActionEvent(
            analyticsEntry: AnalyticsEntry, successData: Map<String, *>? = null
        ) {
            addEvent(AnalyticsEventType.ACTION, success = true, analyticsEntry, successData)
        }

        fun addFailedActionEvent(
            analyticsEntry: AnalyticsEntry, failureData: Map<String, *>? = null
        ) {
            addEvent(
                AnalyticsEventType.ACTION, success = false, analyticsEntry, null, failureData
            )
        }

        fun addExceptionEvent(throwable: Throwable, analyticsEntry: AnalyticsEntry? = null) {
            addEvent(
                AnalyticsEventType.EXCEPTION, null,
                (analyticsEntry ?: AnalyticsEntry()).withReason(throwable.stackTraceToString())
            )
        }

        fun addCrashEvent(it: String) {
            addEvent(
                AnalyticsEventType.CRASH, null,
                AnalyticsEntry().withReason(it)
            )
        }

        fun addLogoutEvent(forcedLogoutReason: String? = null) {
            var entry = AnalyticsEntry(What.Logout)
            entry =
                if (forcedLogoutReason != null) entry.withReason(forcedLogoutReason)
                else entry.withReason("userLogout")

            addAnalyticsEvent(AnalyticsEventType.ACTION, entry)
        }

        private fun addEvent(
            type: AnalyticsEventType, success: Boolean?, analyticsEntry: AnalyticsEntry,
            successData: Map<String, *>? = null, failureData: Map<String, *>? = null
        ) {
            val entry =
                if (success == null) analyticsEntry
                else if (success) analyticsEntry.asSuccess().withData(successData)
                else analyticsEntry.asFailure().withData(failureData)

            addAnalyticsEvent(type, entry)
        }

        private fun addAnalyticsEvent(eventType: AnalyticsEventType, entry: AnalyticsEntry) {
            val event = AnalyticsEvent(eventType, entry)
            Log.e(_tag, "ANALYTICS EVENT : $event")
            events.add(event)

            val isLoginActivityEvent =
                listOf(What.Login, What.Logout, What.SubmitOTP).contains(entry.what)

            if (isFlushInterval && !isLoginActivityEvent) {
                submitAnalyticsEvents()
            }
        }

        private val isFlushInterval get() = events.size > AppFlag.Analytics.eventFlushCount

        private val _tag = this::class.java.kotlin.simpleName
        private val events: MutableList<AnalyticsEvent> = mutableListOf()

        val submitFeedbackEntry get() = AnalyticsEntry(What.Menu).withEndpoint("submitFeedback")

        val viewProfileEntry get() = AnalyticsEntry(What.Menu).withEndpoint("ViewProfile")
        val viewMyLocationEntry get() = AnalyticsEntry(What.Menu).withEndpoint("MyLocation")
        val editProfileEntry get() = AnalyticsEntry(What.Menu).withEndpoint("EditProfile")
        val changePinEntry get() = AnalyticsEntry(What.Menu).withEndpoint("ChangePIN")

        val transferEntry get() = AnalyticsEntry(What.Transfer)
        val bundleSaleEntry get() = AnalyticsEntry(What.BundleSale)
        val airtimeSaleEntry get() = AnalyticsEntry(What.AirtimeSale)

        val getHistoryPageEntry get() = AnalyticsEntry(What.GetTXHistory)
        val getBundleListEntry get() = AnalyticsEntry(What.GetBundleList)
        val getBalancesEntry get() = AnalyticsEntry(What.GetBalances)

        val loginEntry get() = AnalyticsEntry(What.Login)
        val submitOtpEntry get() = AnalyticsEntry(What.SubmitOTP)
        private val navEntry get() = AnalyticsEntry(What.Nav)
    }
}
