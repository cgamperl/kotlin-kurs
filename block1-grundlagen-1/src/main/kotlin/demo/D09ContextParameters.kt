package demo

/**
 * Demo 9 – Context parameters.
 *
 * A stable language feature since Kotlin 2.4; it needs NO compiler flag.
 *
 * The problem it solves: a function needs something from its environment -
 * a logger, a transaction, a tenant id. Until now there were two ways, and
 * both come at a price:
 *
 *   a) pass it through   -> every signature along the way carries the
 *                           parameter, including the functions that never
 *                           touch it
 *   b) hold it as a field -> the class becomes stateful and harder to test
 *
 * Context parameters are the third way: the dependency is in the signature,
 * so it is visible and type-checked - but at the call site it is not passed,
 * it is resolved from the context.
 */

class AuditLog(private val source: String) {
    private val lines = mutableListOf<String>()

    fun write(text: String) {
        val line = "[$source] $text"
        lines += line
        println(line)
    }

    fun lineCount(): Int = lines.size
}

/**
 * `context(log: AuditLog)` - the function requires an AuditLog in context.
 *
 * Note the parameter list: it contains only what belongs to the subject
 * matter. The logger does not appear there.
 */
context(log: AuditLog)
fun startDevice(device: Device) {
    log.write("starting ${device.name}")
    // … the actual work would go here …
    log.write("${device.id} is online")
}

context(log: AuditLog)
fun stopDevice(device: Device) {
    log.write("stopping ${device.name}")
}

/**
 * And here is the actual benefit: this function never uses the logger
 * itself. It still passes it on, without carrying it as a parameter -
 * because it stands in the matching context.
 *
 * In a deep call chain that is the difference between changing one
 * signature and changing twelve.
 */
context(log: AuditLog)
fun restart(device: Device) {
    stopDevice(device)
    startDevice(device)
}

fun main() {
    val device = Device(DeviceId("cam-04"), "Camera Studio B", DeviceType.CAMERA)
    val log = AuditLog("maintenance")

    // The context is provided by making the value the receiver - here with
    // `with`. Inside the block every function requiring an AuditLog as its
    // context becomes callable.
    with(log) {
        restart(device)
    }

    // Outside the block:
    //     restart(device)
    // is a compile error, because no AuditLog is in context.
    //
    // That is the decisive difference from a global variable or a
    // ThreadLocal: availability is checked statically, not hoped for at
    // runtime.

    println()
    println("log lines: ${log.lineCount()}")

    // Outlook: when two functions of the same name differ only in their
    // context type, the desired context can be stated explicitly since 2.4.
    // That is still behind -Xexplicit-context-arguments and deliberately
    // not used here.
}
