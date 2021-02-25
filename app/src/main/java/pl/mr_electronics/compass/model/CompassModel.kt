package pl.mr_electronics.compass.model

import android.location.Location

class CompassModel {

    var currOrientation: Float = 0.0f
    var oriantationToSet: Float = 0.0f
    var angleSpeed: Float = 2.0f
    var azymutToTarget: Float = 0.0f

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
}