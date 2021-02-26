package pl.mr_electronics.compass.controller

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import pl.mr_electronics.compass.model.CompassModel

class MagneticSensorController : SensorEventListener {
    private var activity: Activity
    private var compassModel: CompassModel
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magneticField: Sensor
    private var mGravity: FloatArray? = null
    private var mGeomagnetic: FloatArray? = null


    constructor(activity: Activity, compassModel: CompassModel) {
        this.activity = activity
        this.compassModel = compassModel
    }

    fun initSrevice() {
        sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            this.accelerometer = it
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            this.magneticField = it
        }
    }

    fun registerService() {
        sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, this.magneticField, SensorManager.SENSOR_DELAY_FASTEST)
    }

    fun unregisterService() {
        sensorManager.unregisterListener(this)
    }

    fun getAngle(): Float {
        return compassModel.oriantationToSet
    }

    fun setAndgle(deg: Float) {
        compassModel.oriantationToSet = deg
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("Sensor", "onAccuracyChanged() ")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values

        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values

        if (mGravity != null && mGeomagnetic != null) {
            val r = FloatArray(9)
            val i = FloatArray(9)

            if (SensorManager.getRotationMatrix(r, i, mGravity, mGeomagnetic)) {

                // orientation contains azimut, pitch and roll
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)

                val azimut = Math.toDegrees(orientation[0].toDouble()).toFloat()
                setAndgle(-azimut)
            }
        }
    }
}