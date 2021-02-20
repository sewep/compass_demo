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
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    var arrow1 = listOf(PointF(-35.0f, 0.0f), PointF(0.0f, -300.0f), PointF(35.0f, 0.0f))
    var arrow2 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, -220.0f), PointF(25.0f, 0.0f))
    var arrow3 = listOf(PointF(-8.0f, 0.0f), PointF(0.0f, 20.0f), PointF(8.0f, 0.0f))
    var arrow4 = listOf(PointF(-16.0f, 0.0f), PointF(0.0f, 34.0f), PointF(16.0f, 0.0f))
    var arrow5 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, 46.0f), PointF(25.0f, 0.0f))
    var arrow6 = listOf(PointF(-25.0f, 0.0f), PointF(0.0f, -46.0f), PointF(25.0f, 0.0f))

    private val locationPermissionCode = 2
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magneticField: Sensor
    private lateinit var locationManager: LocationManager
    var handler: Handler = Handler(Looper.getMainLooper())
    var dialog: Dialog? = null
    var location_curr: Location? = null
    var location_dest: Location? = null

    var mGravity: FloatArray? = null
    var mGeomagnetic: FloatArray? = null
    var azimut: Float = 0f
    var azimutAverage: Float = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        location_dest = Location(LocationManager.GPS_PROVIDER)
        location_dest!!.latitude = 37.4723
        location_dest!!.longitude = -122.221


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

                var dest_dg: Float? = null;

                if (location_dest != null && location_curr != null) {
                    distanceInfo.text = String.format("Distance from the destination: %.0f m.", location_curr!!.distanceTo(location_dest))
                    dest_dg = location_curr!!.bearingTo(location_dest)
                } else {
                    distanceInfo.text = "Enter the destination point.";
                }

                drawCompass(azimut, dest_dg)

                handler.postDelayed(this, 500)
            }
        },200)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, this.magneticField, SensorManager.SENSOR_DELAY_FASTEST);
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this);
    }

    fun drawCompass(dirWalk: Float, dirDestination: Float?) {
        val bitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)


        paintCentralStar(canvas, dirWalk)
        paintTrianglesOnACircle(canvas, dirWalk)
        if (dirDestination != null) paintAzymutArrow(canvas, dirWalk, dirDestination!!)


        // set bitmap as background to ImageView
        compass_view.setImageBitmap(bitmap)
    }

    fun getPaintColor(color: String) : Paint {
        return getPaintColor(Color.parseColor(color))
    }

    fun getPaintColor(color: Int) : Paint {
        val paint_green = Paint()
        paint_green.setStyle(Paint.Style.FILL)
        paint_green.setColor(color)
        return paint_green
    }

    fun paintAzymutArrow(canvas: Canvas, dirWalk: Float, direction: Float) {
        val paint_green = getPaintColor("#00cc00")

        var trans = PointF(400.0f,400.0f)
        var trans2 = PointF(0.0f,-354.0f)
        paintPolygon(canvas, paint_green, arrow6, trans, direction - dirWalk, trans2)
    }

    fun paintTrianglesOnACircle(canvas: Canvas, dirWalk: Float) {
        val paint_red = getPaintColor(Color.RED)
        val paint_gray = getPaintColor("#202020")

        var trans = PointF(400.0f,400.0f)
        var trans2 = PointF(0.0f,-350.0f)

        for (deg in 0..359 step 10) {
            var list_pkt = arrow3
            var paint_c = paint_gray
            if (deg == 0) paint_c = paint_red
            if (deg % 90 == 0) list_pkt = arrow5
            if ((deg-50) % 90 == 0) list_pkt = arrow4

            paintPolygon(canvas, paint_c, list_pkt, trans, deg.toFloat() - dirWalk, trans2)
        }
    }

    fun paintCentralStar(canvas: Canvas, dirWalk: Float) {
        val paint_red = getPaintColor(Color.RED)
        val paint_gray = getPaintColor("#202020")
        val paint_white = getPaintColor(Color.WHITE)


        var trans = PointF(400.0f,400.0f)

        paintPolygon(canvas, paint_red, arrow1, trans, 0.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow1, trans, 90.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow1, trans, 180.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow1, trans, 270.0f - dirWalk)

        paintPolygon(canvas, paint_gray, arrow2, trans, 50.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow2, trans, 140.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow2, trans, 230.0f - dirWalk)
        paintPolygon(canvas, paint_gray, arrow2, trans, 320.0f - dirWalk)

        paintCircle(canvas, paint_gray, trans, 70)
        paintCircle(canvas, paint_white, trans, 50)
        paintCircle(canvas, paint_gray, trans, 30)
    }

    fun paintCircle(canvas: Canvas, paint: Paint, translate: PointF, r: Int) {
        var shapeDrawable: ShapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.setBounds(
                translate.x.toInt()-r,
                translate.y.toInt()-r,
                translate.x.toInt()+r,
                translate.y.toInt()+r)
        shapeDrawable.getPaint().setColor(paint.color)
        shapeDrawable.draw(canvas)
    }

    fun paintPolygon(canvas: Canvas, paint: Paint, points: List<PointF>, translate: PointF, deg: Float) {
        paintPolygon(canvas, paint, points, translate, deg, PointF(0.0f, 0.0F));
    }

    fun paintPolygon(canvas: Canvas, paint: Paint, points: List<PointF>, translate: PointF, deg: Float, translate2: PointF) {
        if (points.isEmpty()) return

        var m = Matrix();
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
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5f, this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("Sensor", "onAccuracyChanged() ");
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            var R = FloatArray(9)
            var I = FloatArray(9)

            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                // orientation contains azimut, pitch and roll
                var orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation);

                azimut = Math.toDegrees(orientation[0].toDouble()).toFloat()
                //Log.i("Sensor", "azimut: " + azimut);
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.i("Sensor", "onLocationChanged() ");
        this.location_curr = location;
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
        dialog!!.setCancelable(false)
        dialog!!.setContentView(R.layout.dialog_gps_destination)
        //val body = dialog.findViewById(R.id.body) as TextView
        //body.text = title
        //val yesBtn = dialog.findViewById(R.id.yesBtn) as Button
        //val noBtn = dialog.findViewById(R.id.noBtn) as TextView
        //yesBtn.setOnClickListener {
        //    dialog.dismiss()
        //}
        //noBtn.setOnClickListener { dialog.dismiss() }
        dialog!!.show()
    }

    fun onClickSave(view: View) {
        dialog!!.dismiss()
        dialog = null
    }
}