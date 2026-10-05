package demo

/**
 * The shared model for block 2.
 *
 * It builds on what appeared in block 1 - data classes, value classes,
 * default arguments - and is extended step by step by the following demos
 * with generics, enums, sealed classes and extensions.
 */

@JvmInline
value class DeviceId(val value: String) {
    override fun toString(): String = value
}

data class Device(
    val id: DeviceId,
    val name: String,
    val type: DeviceType,
    val location: String,
    val utilisation: Int = 0,
)

/** A single measurement taken from a device. */
data class Measurement(
    val deviceId: DeviceId,
    val second: Int,
    val value: Double,
)

/** Test data that shows up in several demos. */
val sampleDevices: List<Device> = listOf(
    Device(DeviceId("cam-04"), "Camera Studio B", DeviceType.CAMERA, "Studio B", 42),
    Device(DeviceId("cam-09"), "Camera Studio A", DeviceType.CAMERA, "Studio A", 88),
    Device(DeviceId("rtr-01"), "Router Control Room 1", DeviceType.ROUTER, "Control Room 1", 95),
    Device(DeviceId("rtr-02"), "Router Control Room 2", DeviceType.ROUTER, "Control Room 2", 12),
    Device(DeviceId("int-07"), "Intercom Desk", DeviceType.INTERCOM, "Control Room 1", 5),
    Device(DeviceId("sen-12"), "Temperature Sensor", DeviceType.SENSOR, "Plant Room", 60),
)

val sampleMeasurements: List<Measurement> = listOf(
    Measurement(DeviceId("sen-12"), 0, 21.4),
    Measurement(DeviceId("sen-12"), 1, 22.8),
    Measurement(DeviceId("sen-12"), 2, 71.2),
    Measurement(DeviceId("sen-12"), 3, 24.1),
    Measurement(DeviceId("cam-04"), 0, 38.0),
    Measurement(DeviceId("cam-04"), 1, 39.5),
)
