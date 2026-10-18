package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Real-time Hardware Compass Provider utilizing the device's IMU/Magnetometer sensors.
 * On modern flagship devices like the Samsung Galaxy S25 Ultra, this provides high-precision
 * azimuth heading for true direction-of-travel radar orientation.
 */
class CompassOrientationProvider(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _azimuthDegrees = MutableStateFlow(0f)
    val azimuthDegrees: StateFlow<Float> = _azimuthDegrees.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(false)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private var rotationVectorSensor: Sensor? = null
    private var isListening = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    init {
        rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        _isSensorAvailable.value = rotationVectorSensor != null
    }

    fun startListening() {
        if (isListening || rotationVectorSensor == null) return
        sensorManager?.registerListener(
            this,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Convert azimuth from radians to degrees [0, 360)
        var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        if (azimuth < 0) {
            azimuth += 360f
        }

        // Low-pass filter with shortest-angular-distance smoothing across 0/360 wrap
        val current = _azimuthDegrees.value
        var delta = (azimuth - current) % 360f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f

        if (abs(delta) > 0.8f) {
            var smoothed = (current + delta * 0.35f) % 360f
            if (smoothed < 0) smoothed += 360f
            _azimuthDegrees.value = smoothed
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Sensor accuracy changed callback
    }
}
