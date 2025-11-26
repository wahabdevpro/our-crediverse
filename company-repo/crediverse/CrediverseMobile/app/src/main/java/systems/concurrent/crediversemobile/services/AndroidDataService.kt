package systems.concurrent.crediversemobile.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.google.gson.Gson
import systems.concurrent.crediversemobile.models.BalancesResponseModel
import systems.concurrent.crediversemobile.models.AccountInfoResponseModel
import systems.concurrent.crediversemobile.models.LoginResponseModel
import systems.concurrent.crediversemobile.models.TransactionModel
import systems.concurrent.crediversemobile.utils.LocaleHelper

class AndroidDataService(private val context: Context?) {
    private val _tag = this::class.java.kotlin.simpleName

    private val cacheStorageString = "CACHE_STORE"

    private enum class CacheIndex { BALANCE_VISIBILITY, APP_LANGUAGE }

    private fun <T> save(index: CacheIndex, data: T) {
        Log.i(_tag, "Saving cache for ${index.name}")
        val prefs = context?.getSharedPreferences(cacheStorageString, MODE_PRIVATE)

        val prefsEditor = prefs?.edit()
        val json = Gson().toJson(data)
        prefsEditor?.putString(index.name, json)
        prefsEditor?.apply()
        Log.i(_tag, "Saving completed")
    }

    private inline fun <reified T> get(index: CacheIndex): T {
        Log.i(_tag, "Retrieving cache for ${index.name}")
        val prefs = context?.getSharedPreferences(cacheStorageString, MODE_PRIVATE)
        val json = prefs?.getString(index.name, "")
        Log.i(_tag, "Retrieved: $json")
        return Gson().fromJson(json, T::class.java)
    }

    fun saveAppLanguage(language: LocaleHelper.Language) {
        save(CacheIndex.APP_LANGUAGE, language)
    }

    fun getAppLanguage(): LocaleHelper.Language? = get(CacheIndex.APP_LANGUAGE)

    fun saveBalanceVisibility(visible: Boolean) {
        save(CacheIndex.BALANCE_VISIBILITY, visible)
    }

    fun getBalanceVisibility(): Boolean? = get(CacheIndex.BALANCE_VISIBILITY)
}