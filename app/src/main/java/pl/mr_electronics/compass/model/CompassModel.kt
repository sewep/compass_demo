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
    var azymutToPresent: Float = 0.0f
    var distaceToTarget: Float = 0f
    var messageGps: String = ""

    var currentLocation: Location? = null
    var destinationLocation: Location? = null

    var lastTimeCalculationsAngle = System.currentTimeMillis()
    var lastTimeCalculationsAzymut = System.currentTimeMillis()

    constructor() {

    }


    fun moveToDestinationAngle(): Float {
        val currTime = System.currentTimeMillis()
        val delta = (currTime - lastTimeCalculationsAngle).toFloat()
        lastTimeCalculationsAngle = currTime
        val delta_s = Math.min(delta / 1000.0f, 1.0f)

        val step = MathTools.DegToAngle(currOrientation, oriantationToSet) * delta_s * angleSpeed
        currOrientation += step
        return currOrientation
    }

    fun moveToTargetAzymut(): Float {
        val currTime = System.currentTimeMillis()
        val delta = (currTime - lastTimeCalculationsAzymut).toFloat()
        lastTimeCalculationsAzymut = currTime
        val delta_s = Math.min(delta / 1000.0f, 1.0f)

        val step = MathTools.DegToAngle(azymutToPresent, azymutToTarget) * delta_s * angleSpeed
        azymutToPresent += step
        return azymutToPresent
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
                messageGps = String.format(MainActivity.context.getString(R.string.distance_fom_the_destination), MathTools.MettersToHumanRedable(distaceToTarget))
                azymutToTarget = currentLocation!!.bearingTo(destinationLocation)
            } else {
                messageGps = MainActivity.context.getString(R.string.enter_the_detination_point)
            }
        }
    }
}