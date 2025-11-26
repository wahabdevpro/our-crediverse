package systems.concurrent.crediversemobile.overlays

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import systems.concurrent.crediversemobile.App
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.models.AgentLocationDataModel
import systems.concurrent.crediversemobile.services.AppAnalyticsService
import systems.concurrent.crediversemobile.services.Toaster
import systems.concurrent.crediversemobile.services.ToasterType
import systems.concurrent.crediversemobile.utils.AppFlag
import systems.concurrent.crediversemobile.utils.NavigationManager
import java.io.BufferedReader

class TeamMemberLocationOverlay<Binding : ViewBinding>(
    contextThemeWrapper: ContextThemeWrapper,
    parent: ViewGroup,
    private val activity: NavigationActivity,
    private val savedInstanceState: Bundle?,
    private val mapViewResource: Int = R.id.map_view,
    mapLayoutResource: Int = R.layout.map_layout
) : OverlayInflatorWithBinding<Binding>(contextThemeWrapper, parent, mapLayoutResource),
    OnMapReadyCallback {

    private var mapView: MapView? = null

    private lateinit var clusterManager: ClusterManager<AgentLocationDataModel>

    /**
     * Helper functions to be used by parent activity/fragment...
     */
    fun onResume() {
        mapView?.onResume()
    }

    fun onPause() {
        mapView?.onPause()
    }

    fun onDestroy() {
        mapView?.onDestroy()
    }

    fun onLowMemory() {
        mapView?.onLowMemory()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun openOverlay() {
        super.openOverlay()
        mapView = mapView ?: activity.findViewById(mapViewResource)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)

        AppAnalyticsService.addNavigationEvent(NavigationManager.Page.TEAM_MEMBER_MAP.toString())
    }

    private fun addAgentMarkers() {
        val agentLocationPath = AppFlag.TeamMembers.locationFilePath
        val bufferReader = BufferedReader(App.context.assets.open(agentLocationPath).reader())
        val csvParser = CSVParser.parse(bufferReader, CSVFormat.DEFAULT)
        csvParser
            // remove headings
            .filterIndexed { index, _ -> index != 0 }
            .forEach {
                val agentLocation = AgentLocationDataModel(
                    agentName = it.get(0),
                    msisdn = it.get(1),
                    latLong = it.get(2),
                    balance = it.get(3),
                    lastUpdated = it.get(4)
                )
                clusterManager.addItem(agentLocation)
            }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        try {
            clusterManager = ClusterManager(activity, googleMap)
            googleMap.setOnCameraIdleListener(clusterManager)

            val renderer = DefaultClusterRenderer(activity, googleMap, clusterManager)
            clusterManager.renderer = renderer

            addAgentMarkers()

            // Set camera bounds - helps to ensure the camera surrounds all markers comfortably
            val boundsBuilder = LatLngBounds.builder()
            for (item in clusterManager.algorithm.items) {
                boundsBuilder.include(item.position)
            }
            val padding = 100 // Adjust padding as needed
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding)
            googleMap.animateCamera(cameraUpdate)
        } catch (ex: Exception) {
            Log.e(_tag, ex.message.toString())
            Toaster.showCustomToast(
                activity, ex.message.toString(),
                Toaster.Options().setType(ToasterType.WARN)
            )
        }
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName
    }
}
