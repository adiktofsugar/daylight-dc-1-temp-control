package rocks.sackof.backlight

import android.util.Log
import java.io.File
import kotlin.math.roundToInt

object BacklightController {

    private const val TAG = "Backlight"
    private const val MAIN = "/sys/class/leds/lcd-backlight"
    private const val AMBER = "/sys/class/leds/lcd-backlight-amber"

    data class State(
        val main: Int,
        val amber: Int,
        val mainMax: Int,
        val amberMax: Int,
    )

    fun readState(): State = State(
        main = readInt("$MAIN/brightness") ?: 0,
        amber = readInt("$AMBER/brightness") ?: 0,
        mainMax = readInt("$MAIN/max_brightness") ?: 255,
        amberMax = readInt("$AMBER/max_brightness") ?: 255,
    )

    /**
     * Apply a (temperature, brightness) pair.
     *  temp ∈ [0,1]: 0 = full cool, 1 = full amber
     *  bright ∈ [0,1]
     */
    fun apply(temp: Float, bright: Float, max: State) {
        val mainVal = (bright * (1f - temp) * max.mainMax).roundToInt().coerceIn(0, max.mainMax)
        val amberVal = (bright * temp * max.amberMax).roundToInt().coerceIn(0, max.amberMax)
        writeInt("$MAIN/brightness", mainVal)
        writeInt("$AMBER/brightness", amberVal)
    }

    fun setMain(v: Int) = writeInt("$MAIN/brightness", v)
    fun setAmber(v: Int) = writeInt("$AMBER/brightness", v)

    private fun readInt(path: String): Int? = try {
        File(path).readText().trim().toIntOrNull()
    } catch (_: Exception) {
        // try root fallback for /sys reads (rare; readable for most)
        runSu("cat $path")?.trim()?.toIntOrNull()
    }

    private fun writeInt(path: String, value: Int) {
        // Fast path: direct write (works if Magisk module chmod'd the node).
        try {
            File(path).writeText(value.toString())
            return
        } catch (e: Exception) {
            Log.d(TAG, "direct write to $path failed: ${e.message}; falling back to su")
        }
        // Fallback: shell out to su. Uses /system/bin/sh to avoid quoting weirdness.
        runSu("echo $value > $path")
    }

    private fun runSu(cmd: String): String? = try {
        val proc = ProcessBuilder("su", "-c", cmd)
            .redirectErrorStream(true)
            .start()
        val out = proc.inputStream.bufferedReader().readText()
        proc.waitFor()
        if (proc.exitValue() == 0) out else null
    } catch (e: Exception) {
        Log.w(TAG, "su exec failed: ${e.message}")
        null
    }
}
