package com.lindum.mlkit.vision

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import java.util.Locale

/**
 * Monitors device temperature.
 */
class TemperatureMonitor(context: Context) : SensorEventListener {
    var sensorReadingsCelsius: MutableMap<String?, Float?> = HashMap<String?, Float?>()

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    init {
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in allSensors) {
            // Assumes sensors with "temperature" substring in their names are temperature sensors.
            // Those sensors may measure the temperature of different parts of the device. It makes more
            // sense to track the change of themselves, e.g. compare the reading before and after running
            // a detector for a certain amount of time, rather than relying on their absolute values at a
            // certain time.
            if (sensor.getName().lowercase(Locale.getDefault()).contains("temperature")) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun logTemperature() {
        for (entry in sensorReadingsCelsius.entries) {
            val tempC: Float = entry.value!!
            // Skips likely invalid sensor readings
            if (tempC < 0) {
                continue
            }
            val tempF = tempC * 1.8f + 32f
            Log.i(TAG, String.format(Locale.US, "%s:\t%.1fC\t%.1fF", entry.key, tempC, tempF))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        sensorReadingsCelsius.put(sensorEvent.sensor.getName(), sensorEvent.values[0])
    }

    companion object {
        private const val TAG = "TemperatureMonitor"
    }
}
