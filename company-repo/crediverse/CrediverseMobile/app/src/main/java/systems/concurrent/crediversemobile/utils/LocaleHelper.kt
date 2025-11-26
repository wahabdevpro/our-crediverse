package systems.concurrent.crediversemobile.utils

import android.app.Activity
import android.content.Context
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.LoginActivity
import systems.concurrent.crediversemobile.services.CacheService

class LocaleHelper {
    enum class Language(private val _languageResource: Int, private val _languageCode: String) {
        EN(R.string.english, "en"),
        FR(R.string.french, "fr");

        val resource = _languageResource
        val localeCode = _languageCode

        companion object {
            fun toList(): List<Int> {
                val numberedList = mutableListOf<Int>()
                values().forEach { numberedList.add(it._languageResource) }
                return numberedList
            }

            fun currentLanguageIndex(context: Context): Int {
                return values().map { it._languageCode }
                    .indexOf(getCurrentLanguage(context)._languageCode)
            }

            fun valueFromIndexOrDefault(position: Int): Language {
                return values().getOrNull(position) ?: defaultLanguage
            }

            fun valueOfOrNull(localeCode: String): Language? {
                return values().firstOrNull { it.localeCode == localeCode.lowercase() }
            }

            fun valueOfOrDefault(localeCode: String): Language {
                return values().firstOrNull { it.localeCode == localeCode.lowercase() }
                    ?: defaultLanguage
            }
        }
    }

    companion object {
        private val _tag = Companion::class.java.kotlin.simpleName

        private val defaultLanguage by lazy {
            Language.valueOfOrNull(AppFlag.LoginPage.defaultAppLanguage) ?: Language.EN
        }

        private var language = defaultLanguage // starting value

        fun getCurrentLanguage(context: Context): Language {
            val storedLanguage = CacheService(context).getOrNull<Language>()
            return storedLanguage ?: language
        }

        fun setLanguage(
            activity: Activity, language: Language, saveToCache: Boolean = false,
            onChange: ((wasUpdated: Boolean) -> Unit)? = null
        ) {
            if (this.language != language) {
                this.language = language
                onChange?.invoke(true)
            } else {
                onChange?.invoke(false)
            }

            if (activity is LoginActivity) {
                AppCompatActivityWithIdleManager.setLoginPageLanguage(language)
            }

            if (activity !is LoginActivity && saveToCache) {
                val cacheService = CacheService(activity.applicationContext)
                cacheService.save(language, CacheService.NEVER_EXPIRES)
            }
        }
    }
}