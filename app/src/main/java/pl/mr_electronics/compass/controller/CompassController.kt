package pl.mr_electronics.compass.controller

import pl.mr_electronics.compass.model.CompassModel
import pl.mr_electronics.compass.view.CompassView

class CompassController {

    var compassModel = CompassModel()
    var compass_view: CompassView? = null

    fun setCompassView(compass_view: CompassView?) {
        this.compass_view = compass_view
    }

    fun setMessageTextControl() {

    }

    fun moveCurrentAngle(): Float {
        val nangle = compassModel.moveToDestinationAngle()
        if (compass_view != null) {
            compass_view!!.presentAngle = nangle
            compass_view!!.azymutAngle = compassModel.azymutToTarget
            compass_view!!.invalidate()
        }

        return nangle
    }
}