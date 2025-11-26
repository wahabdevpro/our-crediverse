package systems.concurrent.crediversemobile.models

import com.google.protobuf.DoubleValue
import com.google.protobuf.Int32Value

data class SsApiCoordinatesModel(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Int,
    val age: Int,
)

data class GrpcCoordinatesModel(
    val latitude: DoubleValue,
    val longitude: DoubleValue,
    val accuracy: Int32Value,
    val age: Int32Value,
)