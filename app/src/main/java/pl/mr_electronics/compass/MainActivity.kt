package pl.mr_electronics.compass

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pl.mr_electronics.compass.controller.CompassController
import pl.mr_electronics.compass.controller.GpsController
import pl.mr_electronics.compass.controller.MagneticSensorController
import pl.mr_electronics.compass.view.SetLocationDialog


class MainActivity : AppCompatActivity() {

    private var handler: Handler = Handler(Looper.getMainLooper())

    private var compassController = CompassController()
    private var gpsController = GpsController(this, compassController.compassModel)
    private var magneticSensorController = MagneticSensorController(this, compassController.compassModel)

    companion object {
        lateinit var context: Context
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_main)

        compassController.setCompassView(compass_view)
        gpsController.initGps()
        magneticSensorController.initSrevice()

        gpsController.setDestinationLocation(37.4723, -122.221) // default value

        handler.postDelayed(rTimer,100)
    }

    private val rTimer: Runnable = object : Runnable {
        override fun run() {
            compassController.moveCurrentAngle()
            distanceInfo.text = gpsController.getMessage()

            handler.postDelayed(this, 50) // repeat
        }
    }

    override fun onResume() {
        super.onResume()
        magneticSensorController.registerService()
    }

    override fun onPause() {
        super.onPause()
        magneticSensorController.unregisterService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == gpsController.locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                gpsController.initGps()
            }
            else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onClickGPS(view: View) {
        SetLocationDialog(this, gpsController).show()
    }

}


