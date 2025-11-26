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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.services.LocationService
import systems.concurrent.crediversemobile.services.Toaster
import systems.concurrent.crediversemobile.services.ToasterType

class AgentLocationOverlay<Binding : ViewBinding>(
    contextThemeWrapper: ContextThemeWrapper,
    parent: ViewGroup,
    private val activity: NavigationActivity,
    private val savedInstanceState: Bundle?,
    private val mapViewResource: Int = R.id.map_view,
    mapLayoutResource: Int = R.layout.map_layout
) : OverlayInflatorWithBinding<Binding>(contextThemeWrapper, parent, mapLayoutResource),
    OnMapReadyCallback {

    private var mapView: MapView? = null

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

    override fun openOverlay() {
        super.openOverlay()
        mapView = mapView ?: activity.findViewById(mapViewResource)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
    }

    /**
     * We manage whether the permissions are enabled already in a separate context
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        try {
            val location = LocationService.getGrpcLocation(activity)
            Log.i(_tag, "Location retrieved? " + (location != null).toString())
            if (location != null) {
                val latitude: Double = location.latitude.value
                val longitude: Double = location.longitude.value
                val myLocation = LatLng(latitude, longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(myLocation)
                        .title(activity.getString(R.string.my_location_text))
                        .visible(true)
                )
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10f))
            } else {
                val locationErrorType = LocationService.getLocationErrorType()
                val locationErrorMessage =
                    if (locationErrorType == LocationService.LocationErrorType.PERMISSION_DENIED) {
                        activity.getString(R.string.missing_location_permission)
                    } else {
                        activity.getString(R.string.location_not_found)
                    }
                Toaster.showCustomToast(
                    activity, locationErrorMessage,
                    Toaster.Options().setType(ToasterType.WARN)
                )
            }
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
