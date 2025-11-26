package systems.concurrent.crediversemobile.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import systems.concurrent.crediversemobile.R

class LogoutManager {
    enum class ID { NO_LOGIN, LOGGED_IN, FORCED_OUT }

    data class State(private val id: ID, private var _reasonResource: Int? = null) {
        fun isForcedLogoutUpgradeRequired(): Boolean {
            return this.id == ID.FORCED_OUT &&
                    _reasonResource == R.string.logout_upgrade_required
        }

        fun isForcedLogout() = this.id == ID.FORCED_OUT
        fun isLoggedOut() = this.id != ID.LOGGED_IN

        val reason get() = _reasonResource
    }

    companion object {
        private val logoutState by lazy { MutableLiveData(State(ID.NO_LOGIN)) }

        fun setLoggedIn() {
            logoutState.value = State(ID.LOGGED_IN)
        }

        /**
         * WARNING:
         *
         * When "observing" the state, you should only act on states that are
         *  "OUT" or "NO_LOGIN" (i.e. don't act on the observed "IN" state)
         */
        fun resetToNew() {
            logoutState.value = State(ID.NO_LOGIN)
        }

        fun forceLogout(reasonResource: Int? = R.string.forced_logout) {
            logoutState.value = State(ID.FORCED_OUT, reasonResource)
        }

        /** ************** */

        fun getLogoutStateLiveData(): LiveData<State> = logoutState
    }
}
