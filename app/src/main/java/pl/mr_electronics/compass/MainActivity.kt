package pl.mr_electronics.compass

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import pl.mr_electronics.compass.controller.CompassController


class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

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
    private var azimutAverage: Float = 0f
    private var tsGetGpsLocation: Long = 0

    private var compassController = CompassController()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compassController.setCompassView(compass_view)

        locationDest = Location(LocationManager.GPS_PROVIDER)
        locationDest!!.latitude = 37.4723
        locationDest!!.longitude = -122.221


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            this.accelerometer = it
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            this.magneticField = it
        }

        getLocation()


        handler.postDelayed(object : Runnable {
            override fun run() {

                if (!gpsEnabled) {
                    distanceInfo.text = getString(R.string.turn_on_gps)
                }
                else if (System.currentTimeMillis() - tsGetGpsLocation > 10000) {
                    distanceInfo.text = getString(R.string.no_gps_signal)
                } else {
                    if (locationDest != null && locationCurr != null) {
                        distanceInfo.text = String.format(getString(R.string.distance_fom_the_destination), locationCurr!!.distanceTo(locationDest))
                        compassController.setAzymut(locationCurr!!.bearingTo(locationDest))
                    } else {
                        distanceInfo.text = getString(R.string.enter_the_detination_point)
                    }
                }

                compassController.moveCurrentAngle()

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

    private fun getPaintColor(color: String) : Paint {
        return getPaintColor(Color.parseColor(color))
    }

    private fun getPaintColor(color: Int) : Paint {
        val paintGreen = Paint()
        paintGreen.style = Paint.Style.FILL
        paintGreen.color = color
        return paintGreen
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!gpsEnabled) return
        } catch (ex: java.lang.Exception) {
        }

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5f, this)
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

    override fun onLocationChanged(location: Location) {
        Log.i("Sensor", "onLocationChanged() ")
        this.locationCurr = location
        tsGetGpsLocation = System.currentTimeMillis()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                getLocation()
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


