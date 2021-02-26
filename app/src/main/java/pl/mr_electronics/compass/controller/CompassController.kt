package pl.mr_electronics.compass.controller

import android.content.Context
import android.location.Location
import android.location.LocationManager
import pl.mr_electronics.compass.model.CompassModel
import pl.mr_electronics.compass.view.CompassView
import pl.mr_electronics.compass.view.MainActivity

class CompassController {

    var compassModel = CompassModel()
    var compass_view: CompassView? = null

    fun setCompassView(compass_view: CompassView?) {
        this.compass_view = compass_view
    }

    fun moveCurrentAngle(): Float {
        val nangle = compassModel.moveToDestinationAngle()
        if (compass_view != null) {
            compass_view!!.presentAngle = nangle
        }
        return nangle
    }

    fun moveCurrentAzymut(): Float {
        val nazymut = compassModel.moveToTargetAzymut()
        if (compass_view != null) {
            compass_view!!.azymutAngle = nazymut
            compass_view!!.azymutEnabled = compassModel.gpsEnabled
        }
        return nazymut
    }

    fun invalidate() {
        if (compass_view != null) {
            moveCurrentAngle()
            moveCurrentAzymut()
            compass_view!!.invalidate()
        }
    }

    fun destinationLocationFromPreferences() {
        val pref = MainActivity.context.getSharedPreferences("cfg", Context.MODE_PRIVATE)

        var destinationLocation = Location(LocationManager.GPS_PROVIDER)
        destinationLocation.latitude = pref.getFloat("destinationLocation_latitude", 37.4723f).toDouble()
        destinationLocation.longitude = pref.getFloat("destinationLocation_longitude", -122.221f).toDouble()
        compassModel.destinationLocation = destinationLocation
    }

    fun saveDestinationLocationInPreferences() {
        if (compassModel.destinationLocation != null) {
            val pref = MainActivity.context.getSharedPreferences("cfg", Context.MODE_PRIVATE)
            pref.edit().putFloat(
                "destinationLocation_latitude",
                compassModel.destinationLocation!!.latitude?.toFloat()
            ).apply()
            pref.edit().putFloat(
                "destinationLocation_longitude",
                compassModel.destinationLocation!!.longitude?.toFloat()
            ).apply()
        }
    }
}