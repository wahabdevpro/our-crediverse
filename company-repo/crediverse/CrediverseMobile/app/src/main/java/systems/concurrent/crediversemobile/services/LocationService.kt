package systems.concurrent.crediversemobile.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.protobuf.DoubleValue
import com.google.protobuf.Int32Value
import systems.concurrent.crediversemobile.models.GrpcCoordinatesModel
import systems.concurrent.crediversemobile.models.SsApiCoordinatesModel
import java.util.*
import kotlin.math.roundToInt

@SuppressLint("MissingPermission")
class LocationService(val context: Context) {
    enum class LocationErrorType { LOCATION_NOT_FOUND, PERMISSION_DENIED }

    companion object {
        private var grpcCoordinates: GrpcCoordinatesModel? = null
        private var ssApiCoordinates: SsApiCoordinatesModel? = null
        private var locationErrorType: LocationErrorType = LocationErrorType.LOCATION_NOT_FOUND

        fun getGrpcLocation(context: Context? = null): GrpcCoordinatesModel? {
            context?.let { updateLocation(context) }
            return grpcCoordinates
        }

        fun getSsApiLocation(context: Context? = null): SsApiCoordinatesModel? {
            context?.let { updateLocation(context) }
            return ssApiCoordinates
        }

        fun getLocationErrorType() = locationErrorType

        fun updateLocation(context: Context) {
            val locationClient = LocationServices.getFusedLocationProviderClient(context)

            val fineLocationPermissionGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarseLocationPermissionGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineLocationPermissionGranted || coarseLocationPermissionGranted) {
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token,
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        val accuracy = location.accuracy.roundToInt()
                        val locationAge = (Date().time - location.time).toInt()

                        val latitude = location.latitude
                        val longitude = location.longitude

                        grpcCoordinates =
                            GrpcCoordinatesModel(
                                latitude.let { DoubleValue.of(it) },
                                longitude.let { DoubleValue.of(it) },
                                accuracy.let { Int32Value.of(it) },
                                locationAge.let { Int32Value.of(it) }
                            )

                        ssApiCoordinates =
                            SsApiCoordinatesModel(latitude, longitude, accuracy, locationAge)
                    } else {
                        locationErrorType = LocationErrorType.LOCATION_NOT_FOUND
                    }
                }
            } else {
                locationErrorType = LocationErrorType.PERMISSION_DENIED
            }
        }
    }
}
