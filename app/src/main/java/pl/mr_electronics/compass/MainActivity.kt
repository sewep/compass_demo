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


class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private var arrow1 = listOf(PointF(-35.0f, 0.0f), PointF(0.0f, -300.0f), PointF(35.0f, 0.0f))
    private var arrow2 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, -220.0f), PointF(25.0f, 0.0f))
    private var arrow3 = listOf(PointF(-8.0f, 0.0f), PointF(0.0f, 20.0f), PointF(8.0f, 0.0f))
    private var arrow4 = listOf(PointF(-16.0f, 0.0f), PointF(0.0f, 34.0f), PointF(16.0f, 0.0f))
    private var arrow5 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, 46.0f), PointF(25.0f, 0.0f))
    private var arrow6 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, -46.0f), PointF(25.0f, 0.0f))

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                azimutAverage = azimutAverage * 0.8f + azimut * 0.2f

                var destDg: Float? = null

                if (!gpsEnabled) {
                    distanceInfo.text = getString(R.string.turn_on_gps)
                }
                else if (System.currentTimeMillis() - tsGetGpsLocation > 10000) {
                    distanceInfo.text = getString(R.string.no_gps_signal)
                } else {
                    if (locationDest != null && locationCurr != null) {
                        distanceInfo.text = String.format(getString(R.string.distance_fom_the_destination), locationCurr!!.distanceTo(locationDest))
                        destDg = locationCurr!!.bearingTo(locationDest)
                    } else {
                        distanceInfo.text = getString(R.string.enter_the_detination_point)
                    }
                }

                drawCompass(azimut, destDg)

                handler.postDelayed(this, 500)
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

    fun drawCompass(dirWalk: Float, dirDestination: Float?) {
        val bitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)


        paintCentralStar(canvas, dirWalk)
        paintTrianglesOnACircle(canvas, dirWalk)
        if (dirDestination != null) paintAzymutArrow(canvas, dirWalk, dirDestination)

        // set bitmap as background to ImageView
        compass_view.setImageBitmap(bitmap)
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

    private fun paintAzymutArrow(canvas: Canvas, dirWalk: Float, direction: Float) {
        val paintGreen = getPaintColor("#00cc00")

        val trans = PointF(400.0f,400.0f)
        val trans2 = PointF(0.0f,-354.0f)
        paintPolygon(canvas, paintGreen, arrow6, trans, direction - dirWalk, trans2)
    }

    private fun paintTrianglesOnACircle(canvas: Canvas, dirWalk: Float) {
        val paintRed = getPaintColor(Color.RED)
        val paintGray = getPaintColor("#202020")

        val trans = PointF(400.0f,400.0f)
        val trans2 = PointF(0.0f,-350.0f)

        for (deg in 0..359 step 10) {
            var listPkt = arrow3
            var paintC = paintGray
            if (deg == 0) paintC = paintRed
            if (deg % 90 == 0) listPkt = arrow5
            if ((deg-50) % 90 == 0) listPkt = arrow4

            paintPolygon(canvas, paintC, listPkt, trans, deg.toFloat() - dirWalk, trans2)
        }
    }

    private fun paintCentralStar(canvas: Canvas, dirWalk: Float) {
        val paintRed = getPaintColor(Color.RED)
        val paintGray = getPaintColor("#202020")
        val paintWhite = getPaintColor(Color.WHITE)


        val trans = PointF(400.0f,400.0f)

        paintPolygon(canvas, paintRed, arrow1, trans, 0.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow1, trans, 90.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow1, trans, 180.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow1, trans, 270.0f - dirWalk)

        paintPolygon(canvas, paintGray, arrow2, trans, 50.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow2, trans, 140.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow2, trans, 230.0f - dirWalk)
        paintPolygon(canvas, paintGray, arrow2, trans, 320.0f - dirWalk)

        paintCircle(canvas, paintGray, trans, 70)
        paintCircle(canvas, paintWhite, trans, 50)
        paintCircle(canvas, paintGray, trans, 30)
    }

    private fun paintCircle(canvas: Canvas, paint: Paint, translate: PointF, r: Int) {
        val shapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.setBounds(
                translate.x.toInt()-r,
                translate.y.toInt()-r,
                translate.x.toInt()+r,
                translate.y.toInt()+r)
        shapeDrawable.paint.color = paint.color
        shapeDrawable.draw(canvas)
    }

    private fun paintPolygon(canvas: Canvas, paint: Paint, points: List<PointF>, translate: PointF, deg: Float) {
        paintPolygon(canvas, paint, points, translate, deg, PointF(0.0f, 0.0F))
    }

    private fun paintPolygon(canvas: Canvas, paint: Paint, points: List<PointF>, translate: PointF, deg: Float, translate2: PointF) {
        if (points.isEmpty()) return

        val m = Matrix()
        m.postTranslate(translate2.x, translate2.y)
        m.postRotate(deg)
        m.postTranslate(translate.x, translate.y)

        canvas.save()
        canvas.setMatrix(m)

        // Draw
        val path = Path()
        path.reset()
        path.moveTo(points.last().x, points.last().y)
        for (pkt in points) {
            path.lineTo(pkt.x, pkt.y)
        }
        canvas.drawPath(path, paint)

        canvas.restore()
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


