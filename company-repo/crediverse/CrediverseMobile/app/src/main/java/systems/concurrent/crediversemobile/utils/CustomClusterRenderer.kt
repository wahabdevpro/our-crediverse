package systems.concurrent.crediversemobile.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import systems.concurrent.crediversemobile.App
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.models.AgentLocationDataModel

class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<AgentLocationDataModel>,
    private val layoutInflater: LayoutInflater
) : DefaultClusterRenderer<AgentLocationDataModel>(context, map, clusterManager), GoogleMap.InfoWindowAdapter {

    override fun onClusterItemRendered(clusterItem: AgentLocationDataModel, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)
        marker.tag = clusterItem
    }

    override fun onBeforeClusterItemRendered(item: AgentLocationDataModel, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(item.balance)))
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View? {
        val markerInfoView = layoutInflater.inflate(R.layout.custom_marker_info_layout, null)
        val agentNameTextView = markerInfoView.findViewById<TextView>(R.id.agent_name)
        val agentMSISDNTextView = markerInfoView.findViewById<TextView>(R.id.agent_msisdn)
        val locationTimeTextView = markerInfoView.findViewById<TextView>(R.id.agent_location_updated_time)
        val agentBalanceTextView = markerInfoView.findViewById<TextView>(R.id.agent_balance)

        val agentData = marker.tag as? AgentLocationDataModel
        agentNameTextView.text = agentData?.agentName
        agentMSISDNTextView.text = App.context.getString(R.string.agent_msisdn, agentData?.msisdn)
        locationTimeTextView.text = App.context.getString(R.string.agent_location_last_updated, agentData?.lastUpdated)
        agentBalanceTextView.text = App.context.getString(R.string.agent_balance, agentData?.balance)

        return markerInfoView
    }

    private fun getMarkerColor(userBalance: String?): Float {
        val balance = userBalance?.toIntOrNull() ?: 0
        return if (balance > 3000) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
    }
}




