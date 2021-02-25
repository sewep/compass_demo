package pl.mr_electronics.compass.controller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pl.mr_electronics.compass.model.CompassModel

class GpsController : LocationListener {

    private var activity: Activity
    private var compassModel: CompassModel
    private lateinit var locationManager: LocationManager
    val locationPermissionCode = 2


    constructor(activity: Activity, compassModel: CompassModel) {
        this.activity = activity
        this.compassModel = compassModel
    }

    fun setDestinationLocation(lat: Double, long: Double) {
        var locationDest = Location(LocationManager.GPS_PROVIDER)
        locationDest.latitude = lat
        locationDest.longitude = long
        setDestinationLocation(locationDest)
    }

    fun setDestinationLocation(location: Location) {
        compassModel.destinationLocation = location
        compassModel.calcGPS()
    }

    fun setCurrentLocation(location: Location) {
        compassModel.currentLocation = location
        compassModel.calcGPS()
    }

    fun getMessage(): String {
        return compassModel.messageGps
    }

    fun initGps() {
        locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            compassModel.gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!compassModel.gpsEnabled) return
        } catch (ex: java.lang.Exception) {
        }

        if ((ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5f, this)
    }

    override fun onLocationChanged(location: Location) {
        setCurrentLocation(location)
    }

}