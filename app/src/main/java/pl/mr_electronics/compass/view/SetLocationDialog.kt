package pl.mr_electronics.compass.view

import android.app.Dialog
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import kotlinx.android.synthetic.main.dialog_gps_destination.*
import pl.mr_electronics.compass.R
import pl.mr_electronics.compass.controller.GpsController

class SetLocationDialog(context: Context, private val gpsController: GpsController) : Dialog(context)  {

    init {
        setCancelable(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_gps_destination)

        val width = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        this.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        var currDestination = gpsController.getDestination()
        if (currDestination != null) {
            findViewById<EditText>(R.id.new_lat).setText(currDestination!!.latitude.toString())
            findViewById<EditText>(R.id.new_long).setText(currDestination!!.longitude.toString())
        }

        bt_save.setOnClickListener(onClickSave)
        bt_cancel.setOnClickListener(onClickCancel)
    }

    val onClickSave = View.OnClickListener { view ->
        val newLat = findViewById<EditText>(R.id.new_lat)
        val newLong = findViewById<EditText>(R.id.new_long)

        try {
            gpsController.setDestinationLocation(
                newLat.text.toString().toDouble(),
                newLong.text.toString().toDouble())
        } catch (ex: Exception) {
            Log.e("SetLocationDialog", ex.message.toString())
        }

        dismiss()
    }

    val onClickCancel = View.OnClickListener { view ->
        dismiss()
    }
}