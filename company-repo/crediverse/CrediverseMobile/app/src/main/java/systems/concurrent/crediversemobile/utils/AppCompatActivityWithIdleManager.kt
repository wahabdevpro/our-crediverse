package systems.concurrent.crediversemobile.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AppCompatActivity
import org.intellij.lang.annotations.Language
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.services.LogoutManager
import systems.concurrent.crediversemobile.services.MasService
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.nowEpoch
import java.util.*

open class AppCompatActivityWithIdleManager : AppCompatActivity() {
    private val _tag = this::class.java.kotlin.simpleName

    companion object {
        private var lastActivity = nowEpoch()
        private var lastRenewal = nowEpoch()

        private var _loginPageLanguage: LocaleHelper.Language? = null

        fun setLoginPageLanguage(language: LocaleHelper.Language) {
            _loginPageLanguage = language
        }

        fun getLoginPageLanguageOnce(): LocaleHelper.Language? {
            val returnedLanguage = _loginPageLanguage
            _loginPageLanguage = null
            return returnedLanguage
        }

        private const val RENEW_EVERY_SECONDS = 60

        fun updateLastTokenRenewalTime() {
            lastRenewal = nowEpoch()
        }
    }

    private fun updateLanguageInContext(context: Context, wrapper: ContextThemeWrapper): Context? {
        val language = getLoginPageLanguageOnce() ?: LocaleHelper.getCurrentLanguage(context)
        val dLocale = Locale(language.localeCode)

        Locale.setDefault(dLocale)
        val configuration = Configuration()
        configuration.setLocale(dLocale)
        wrapper.applyOverrideConfiguration(configuration)

        return context.createConfigurationContext(configuration)
    }

    override fun attachBaseContext(newBaseContext: Context) {
        val updatedContext = updateLanguageInContext(newBaseContext, this)
        super.attachBaseContext(updatedContext)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        val now = nowEpoch()

        lastActivity = now

        /**
         * ONLY monitor idle state if we are NOT on the login page
         */
        if (_tag?.contains("Login") == true) return

        val loginTokenClaim = MasService.getLoginTokenClaim() ?: return

        val tokenExpired = (loginTokenClaim.exp - now) <= 0

        if (tokenExpired) LogoutManager.forceLogout()

        val secondsSinceLastRenewal = now - lastRenewal

        val readyToRenew = secondsSinceLastRenewal > RENEW_EVERY_SECONDS
        if (!readyToRenew) {
            val logMessage = "Not ready to renew the token... " +
                    "Seconds since last renewal? $secondsSinceLastRenewal"
            Log.i(_tag, logMessage)
            return
        }

        Log.i(_tag, "Seconds since last renewal: $secondsSinceLastRenewal")
        Log.i(_tag, "Begin renewal")

        NavigationActivity.renewJwtToken {
            it.onSuccess { updateLastTokenRenewalTime() }
        }
    }

}
