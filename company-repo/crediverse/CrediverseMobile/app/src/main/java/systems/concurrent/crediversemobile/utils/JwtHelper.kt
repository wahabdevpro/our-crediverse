package systems.concurrent.crediversemobile.utils

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import java.util.Base64

class JwtHelper {
    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        private val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        data class JwtClaim(val sessionId: String, val msisdn: String, val exp: Long, val mobileMoneyToken: String?)

        fun getClaim(jwt: String): JwtClaim? {
            val parts = jwt.split(".")
            return try {
                val charset = charset("UTF-8")
                val claimJsonString =
                    String(Base64.getUrlDecoder().decode(parts[1].toByteArray(charset)), charset)
                gson.fromJson(claimJsonString, JwtClaim::class.java)
            } catch (e: Exception) {
                Log.e(_tag, "Error parsing JWT: $e")
                null
            }
        }
    }
}