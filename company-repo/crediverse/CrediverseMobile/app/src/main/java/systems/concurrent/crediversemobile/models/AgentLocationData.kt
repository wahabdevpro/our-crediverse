package systems.concurrent.crediversemobile.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class AgentLocationDataModel(
    val agentName:String,
    val msisdn:String,
    val latLong:String,
    val balance:String,
    val lastUpdated:String
) : ClusterItem {
    override fun getPosition(): LatLng {
        val parts = latLong.split(",")
        val lat = parts[0].toDouble()
        val long = parts[1].toDouble()
        return LatLng(lat, long)
    }

    override fun getTitle(): String {

        return agentName
    }

    override fun getSnippet(): String {

        return "MSISDN: $msisdn, Balance: $balance, LastUpdated: $lastUpdated"
    }

    override fun getZIndex(): Float? {

        return null
    }

}
