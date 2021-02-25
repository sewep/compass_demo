package pl.mr_electronics.compass

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pl.mr_electronics.compass.controller.CompassController
import pl.mr_electronics.compass.controller.GpsController


class MainActivity : AppCompatActivity(), SensorEventListener {

    private val locationPermissionCode = 2
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magneticField: Sensor
    private lateinit var locationManager: LocationManager
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var dialog: Dialog? = null
    private var locationCurr: Location? = null
    private var locationDest: Location? = null

    private var gpsEnabled: Boolean = false
    private var mGravity: FloatArray? = null
    private var mGeomagnetic: FloatArray? = null
    private var azimut: Float = 0f
    private var tsGetGpsLocation: Long = 0

    private var compassController = CompassController()
    private var gpsController = GpsController(this, compassController.compassModel)

    companion object {
        lateinit var context: Context
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_main)

        compassController.setCompassView(compass_view)
        gpsController.setDestinationLocation(37.4723, -122.221);
        gpsController.initGps()


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            this.accelerometer = it
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            this.magneticField = it
        }



        handler.postDelayed(object : Runnable {
            override fun run() {

                compassController.moveCurrentAngle()
                distanceInfo.text = gpsController.getMessage()

                handler.postDelayed(this, 100)
            }
        },200)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, this.magneticField, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
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

                azimut = Math.toDegrees(orientation[0].toDouble()).toFloat()
                //Log.i("Sensor", "azimut: " + azimut);

                compassController.setAndgle(-azimut)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == gpsController.locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                gpsController.initGps()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onClickGPS(view: View) {
        dialog = Dialog(this)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(R.layout.dialog_gps_destination)
        dialog!!.show()

        if (locationDest != null) {
            dialog!!.findViewById<EditText>(R.id.new_lat).setText(locationDest!!.latitude.toString())
            dialog!!.findViewById<EditText>(R.id.new_long).setText(locationDest!!.longitude.toString())
        }

        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun onClickSave(view: View) {
        val newLat = dialog!!.findViewById<EditText>(R.id.new_lat)
        val newLong = dialog!!.findViewById<EditText>(R.id.new_long)

        try {
            locationDest = Location(LocationManager.GPS_PROVIDER)
            locationDest!!.latitude = newLat.text.toString().toDouble()
            locationDest!!.longitude = newLong.text.toString().toDouble()
        } catch (ex: Exception) {

        }

        dialog!!.dismiss()
        dialog = null
    }

    fun onClickCancel(view: View) {
        dialog!!.dismiss()
        dialog = null
    }
}


