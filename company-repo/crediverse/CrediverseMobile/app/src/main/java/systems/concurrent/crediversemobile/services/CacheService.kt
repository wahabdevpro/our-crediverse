package systems.concurrent.crediversemobile.services

import android.content.Context
import java.io.File
import android.util.Log
import com.google.gson.Gson
import systems.concurrent.crediversemobile.models.*
import systems.concurrent.crediversemobile.utils.Encryptor
import systems.concurrent.crediversemobile.utils.LocaleHelper
import java.io.IOException
import java.util.*

class CacheService(context: Context?) {

    // MUST be internal + PublishedApi because inline functions touch it
    @PublishedApi
    internal val context = context

    @PublishedApi
    internal val _tag = CacheService::class.java.kotlin.simpleName

    class InvalidCacheIndexType(msg: String) : Exception(msg)

    @PublishedApi
    internal fun getNow() = Date().time / 1000

    data class CacheItem(val createdTime: Long, val lifeTime: Int, val data: String)

    @PublishedApi
    internal val encryptor = Encryptor()

    data class CacheIndex(private val _name: String) {
        val name get() = _name
    }

    @PublishedApi
    internal fun clear(index: CacheIndex) {
        try {
            Log.d(_tag, "Clearing cache for ${index.name}")
            val cacheDir = context?.cacheDir
            val cacheFile = File(cacheDir, index.name)
            if (cacheFile.exists()) cacheFile.delete()
        } catch (e: Exception) {
            Log.e(_tag, "Error clearing cache item ${index.name}: $e")
        }
    }

    @PublishedApi
    internal fun <T> rawSave(index: CacheIndex, data: T, lifeTime: Int? = null) {
        Log.d(_tag, "Saving cache for ${index.name}")
        val cacheDir = context?.cacheDir
        val cacheFile = File(cacheDir, index.name)

        try {
            clear(index)
            cacheFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            val now = getNow()
            val dataJson = Gson().toJson(data)
            val json = Gson().toJson(
                CacheItem(now, lifeTime ?: DEFAULT_LIFETIME_SECONDS, dataJson)
            )
            val encryptedJson = encryptor.encrypt(json) ?: return
            cacheFile.writeText(encryptedJson)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.w(_tag, "Problem saving cache; fallback is in-memory.")
        }
        Log.d(_tag, "Saving completed")
    }

    @PublishedApi
    internal inline fun <reified T> rawGet(index: CacheIndex): T? {
        Log.d(_tag, "Retrieving cache for ${index.name}")
        return try {
            val cacheDir = context?.cacheDir
            val cacheFile = File(cacheDir, index.name)
            val encryptedJson = cacheFile.readText()
            val json = encryptor.decrypt(encryptedJson) ?: return null
            Log.d(_tag, "Retrieved: $json")

            val cacheItem = Gson().fromJson(json, CacheItem::class.java)
            val now = getNow()

            val isExpired =
                (cacheItem.createdTime + cacheItem.lifeTime) < now &&
                        cacheItem.lifeTime != NEVER_EXPIRES

            if (isExpired) clear(index)

            Gson().fromJson(
                if (isExpired) "" else cacheItem.data,
                T::class.java
            )
        } catch (e: IOException) {
            Log.w(_tag, "CacheService.rawGet() error: ${e.message}")
            null
        }
    }

    @PublishedApi
    internal inline fun <reified T> getCacheIndexType(suffix: String = ""): CacheIndex? {
        return when (T::class) {
            AccountInfoResponseModel::class -> CacheIndex("ACCOUNT_INFO")
            BalancesResponseModel::class -> CacheIndex("BALANCES")
            TransactionModel::class -> CacheIndex("TRANSACTION")
            LocaleHelper.Language::class -> CacheIndex("LANGUAGE")
            SalesSummaryValue::class -> {
                if (suffix.isNotEmpty()) CacheIndex("ACTIVITY_SUMMARY_${suffix}")
                else {
                    Log.w(_tag, "Suffix empty for caching ActivitySummaryValue, skipping.")
                    null
                }
            }
            else -> null
        }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T> getOrNull(suffix: String = ""): T? {
        val cacheIndex = getCacheIndexType<T>(suffix)
        return cacheIndex?.let { rawGet(cacheIndex) }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T> save(data: T, lifeTime: Int? = null, suffix: String = "") {
        val classType = T::class
        if (!classType.isInstance(data))
            throw InvalidCacheIndexType("Expected $classType")

        val cacheIndex = getCacheIndexType<T>(suffix) ?: return
        rawSave(cacheIndex, data, lifeTime)
    }

    companion object {
        fun lifetimeDays(days: Int) = 86400 * days

        const val NEVER_EXPIRES = 0
        private const val DEFAULT_LIFETIME_SECONDS = 3600 // 1 hour
    }
}
