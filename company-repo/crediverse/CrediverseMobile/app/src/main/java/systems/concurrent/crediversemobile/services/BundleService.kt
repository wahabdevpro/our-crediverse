package systems.concurrent.crediversemobile.services

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import systems.concurrent.crediversemobile.App
import systems.concurrent.crediversemobile.BuildConfig
import systems.concurrent.crediversemobile.models.*
import systems.concurrent.crediversemobile.utils.AppFlag
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class BundleService {
    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        private val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        private var rootUrl: String? = null

        private const val ORIGIN_CHANNEL = "SmartApp"

        private var client: OkHttpClient = OkHttpClient()

        enum class HttpMethods { GET, POST }

        private fun setupConnection() {
            if (!rootUrl.isNullOrEmpty()) return

            val clientBuilder = OkHttpClient.Builder()
            val caPath = AppFlag.Network.ssapiCAPath

            client = if (caPath != "NONE") {
                val certificateFactory = CertificateFactory.getInstance("X.509")
                val certificateInput: InputStream = App.context.assets.open(caPath)
                val certificate = certificateFactory.generateCertificate(certificateInput)

                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(null, null)
                keyStore.setCertificateEntry("ca", certificate)

                val trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(keyStore)

                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustManagerFactory.trustManagers, null)

                clientBuilder.sslSocketFactory(
                    sslContext.socketFactory,
                    trustManagerFactory.trustManagers[0] as X509TrustManager
                ).build()
            } else {
                clientBuilder.build()
            }

            rootUrl = "https://${AppFlag.Network.ssapiHostname}:${AppFlag.Network.ssapiPort}"
        }

        private inline fun <reified T> makeRequest(
            method: HttpMethods, urlSuffix: String, body: RequestBody?, bearerToken: String,
            crossinline onCompletion: (Result<T>) -> Unit
        ) {
            try {
                setupConnection()

                if (method == HttpMethods.POST && body == null) throw Exception("(POST) No body provided")

                val requestBuilder =
                    Request.Builder()
                        .url(URL(rootUrl + urlSuffix))
                        .header("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer $bearerToken")
                val request = when (method) {
                    HttpMethods.GET -> requestBuilder.get()
                    HttpMethods.POST -> requestBuilder.post(body!!)
                }
                // Enqueue ASYNC request
                client.newCall(request.build()).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onCompletion(Result.failure(e))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            if (response.body == null) throw Exception("OKHttp - Empty Response Body")
                            val json = response.body!!.string()

                            if (response.code != 200) throw Exception(json)
                            val finalResult = gson.fromJson(json, T::class.java)

                            onCompletion(Result.success(finalResult))
                        } catch (e: Exception) {
                            onCompletion(Result.failure(e))
                        }
                    }
                })
            } catch (err: Exception) {
                Log.e(_tag, "Error when executing get request: " + err.localizedMessage)
                onCompletion(Result.failure(err))
            }
        }

        private const val SELL_BUNDLE_URL = "/bundles/:bundleCode/:method/:beneficiary"
        private const val GET_BUNDLES_URL = "/bundles/:msisdn"

        private val MEDIA_TYPE_JSON = "application/json".toMediaType()
    }

    fun getBundles(
        msisdn: String, bearerToken: String, callback: (Result<SmartshopBundlesListGet>) -> Unit
    ) {
        makeRequest(
            HttpMethods.GET, GET_BUNDLES_URL.replace(":msisdn", msisdn),
            body = null, bearerToken
        ) { result -> callback(result) }
    }

    data class BundleSaleData(
        val bundleName: String, val bundleCode: String, val method: String, val beneficiary: String
    )

    fun sellBundle(
        bundleSaleData: BundleSaleData,
        bearerToken: String,
        coordinates: SsApiCoordinatesModel?,
        onCompletion: (Result<BundleModel>) -> Unit
    ) {
        if (bundleSaleData.bundleName.isEmpty() || bundleSaleData.bundleCode.isEmpty() ||
            bundleSaleData.method.isEmpty() || bundleSaleData.beneficiary.isEmpty()
        ) {
            onCompletion(Result.failure(Exception("One or more of bundleName|bundleCode|method|beneficiary is empty")))
            return
        }

        val sellBundleUrl = SELL_BUNDLE_URL
            .replace(":bundleCode", bundleSaleData.bundleCode)
            .replace(":method", bundleSaleData.method)
            .replace(":beneficiary", bundleSaleData.beneficiary)

        val request = SmartshopSellBundleRequestProperties(
            originTransactionId = null,
            ORIGIN_CHANNEL,
            listOf(CustomParameters("agent_pin", MasService.getAgentPin() ?: "")),
            geoCoordinates = coordinates,
            options = null
        )

        val json = gson.toJson(request)
        val jsonMediaType = json.toRequestBody(MEDIA_TYPE_JSON)

        makeRequest<SmartshopSellBundleResponse>(
            HttpMethods.POST, sellBundleUrl, jsonMediaType, bearerToken
        ) { result ->
            result
                .onFailure {
                    onCompletion(Result.failure(it))
                }
                .onSuccess { smartshopSellResponse ->
                    onCompletion(Result.success(smartshopSellResponse.outcome.bundle))
                }
        }
    }
}
