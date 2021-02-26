package pl.mr_electronics.compass.controller

import pl.mr_electronics.compass.model.CompassModel
import pl.mr_electronics.compass.view.CompassView

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
}