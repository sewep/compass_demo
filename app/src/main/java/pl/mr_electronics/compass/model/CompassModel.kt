package pl.mr_electronics.compass.model

import android.location.Location
import pl.mr_electronics.compass.view.MainActivity
import pl.mr_electronics.compass.R

class CompassModel {

    var currOrientation: Float = 0.0f
    var oriantationToSet: Float = 0.0f
    var angleSpeed: Float = 1.0f
    var gpsEnabled: Boolean = false
    var azymutToTarget: Float = 0.0f
    var distaceToTarget: Float = 0f
    var messageGps: String = ""

    var currentLocation: Location? = null
    var destinationLocation: Location? = null

    var lastTimeCalculations = System.currentTimeMillis()



    fun moveToDestinationAngle(): Float {
        val currTime = System.currentTimeMillis()
        val delta = (currTime - lastTimeCalculations).toFloat()
        lastTimeCalculations = currTime

        val delta_s = Math.min(delta / 1000.0f, 1.0f)
        val step = calcCorrectionAngle() * delta_s * angleSpeed
        currOrientation += step
        return currOrientation
    }

    fun calcCorrectionAngle() : Float {
        var correction = oriantationToSet - currOrientation
        if (Math.abs(correction) > 180) {
            if (correction < 0)
                correction = (360 + correction)
            else
                correction = -(360 - correction)
        }
        return correction
    }

    fun calcGPS() {
        if (!gpsEnabled) {
            messageGps = MainActivity.context.getString(R.string.turn_on_gps)
        }
        else if (false) {
            messageGps = MainActivity.context.getString(R.string.no_gps_signal)
        } else {
            if (destinationLocation != null && currentLocation != null) {
                distaceToTarget = currentLocation!!.distanceTo(destinationLocation)
                messageGps = String.format(MainActivity.context.getString(R.string.distance_fom_the_destination), distaceToTarget)
                azymutToTarget = currentLocation!!.bearingTo(destinationLocation)
            } else {
                messageGps = MainActivity.context.getString(R.string.enter_the_detination_point)
            }
        }
    }
}