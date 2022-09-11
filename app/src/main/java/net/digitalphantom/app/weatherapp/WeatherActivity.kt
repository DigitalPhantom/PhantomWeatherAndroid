/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 - 2022 Yoel Nunez <dev@nunez.guru>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package net.digitalphantom.app.weatherapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import net.digitalphantom.app.weatherapp.listener.WeatherServiceListener
import net.digitalphantom.app.weatherapp.listener.GeocodingServiceListener
import android.location.LocationListener
import android.widget.TextView
import net.digitalphantom.app.weatherapp.service.YahooWeatherService
import net.digitalphantom.app.weatherapp.service.GoogleMapsGeocodingService
import net.digitalphantom.app.weatherapp.service.WeatherCacheService
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.Criteria
import android.content.Intent
import android.location.Location
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import net.digitalphantom.app.weatherapp.fragments.WeatherConditionFragment
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import net.digitalphantom.app.weatherapp.data.Channel
import net.digitalphantom.app.weatherapp.data.LocationResult
import net.digitalphantom.app.weatherapp.viewmodel.MainViewModel

class WeatherActivity : AppCompatActivity(), WeatherServiceListener, GeocodingServiceListener, LocationListener {
    private var weatherIconImageView: ImageView? = null
    private var temperatureTextView: TextView? = null
    private var conditionTextView: TextView? = null
    private var locationTextView: TextView? = null
    private var weatherService: YahooWeatherService? = null
    private var geocodingService: GoogleMapsGeocodingService? = null
    private var cacheService: WeatherCacheService? = null
    private var loadingDialog: ProgressDialog? = null

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private var preferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        weatherIconImageView = findViewById<View>(R.id.weatherIconImageView) as ImageView
        temperatureTextView = findViewById<View>(R.id.temperatureTextView) as TextView
        conditionTextView = findViewById<View>(R.id.conditionTextView) as TextView
        locationTextView = findViewById<View>(R.id.locationTextView) as TextView
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        weatherService = YahooWeatherService()
        preferences!!.getString(getString(R.string.pref_temperature_unit), null)?.let {
            weatherService!!.temperatureUnit = it
        }
        geocodingService = GoogleMapsGeocodingService()
        cacheService = WeatherCacheService()
        if (preferences!!.getBoolean(getString(R.string.pref_needs_setup), true)) {
            startSettingsActivity()
        }
    }

    override fun onStart() {
        super.onStart()
        loadingDialog = ProgressDialog(this)
        loadingDialog!!.setMessage(getString(R.string.loading))
        loadingDialog!!.setCancelable(false)
        loadingDialog!!.show()
        var location: String? = null
        if (preferences!!.getBoolean(getString(R.string.pref_geolocation_enabled), true)) {
            val locationCache = preferences!!.getString(getString(R.string.pref_cached_location), null)
            if (locationCache == null) {
                weatherFromCurrentLocation
            } else {
                location = locationCache
            }
        } else {
            location = preferences!!.getString(getString(R.string.pref_manual_location), null)
        }

        viewModel.refreshWeather(location, this)
    }

    // system's LocationManager
    private val weatherFromCurrentLocation: Unit
        get() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION), GET_WEATHER_FROM_CURRENT_LOCATION)
                return
            }

            // system's LocationManager
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val locationCriteria = Criteria()
            if (isNetworkEnabled) {
                locationCriteria.accuracy = Criteria.ACCURACY_COARSE
            } else if (isGPSEnabled) {
                locationCriteria.accuracy = Criteria.ACCURACY_FINE
            }
            locationManager.requestSingleUpdate(locationCriteria, this, null)
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GET_WEATHER_FROM_CURRENT_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                weatherFromCurrentLocation
            } else {
                loadingDialog!!.hide()
                val messageDialog = AlertDialog.Builder(this)
                        .setMessage(getString(R.string.location_permission_needed))
                        .setPositiveButton(getString(R.string.disable_geolocation)) { dialogInterface, i -> startSettingsActivity() }
                        .create()
                messageDialog.show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.currentLocation -> {
                loadingDialog!!.show()
                weatherFromCurrentLocation
                true
            }
            R.id.settings -> {
                startSettingsActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun serviceSuccess(channel: Channel?) {
        loadingDialog!!.hide()
        if (channel == null) return
        if (channel.item == null) return
        if (channel.units == null) return

        val condition = channel.item?.condition ?: return
        val units = channel.units
        val forecast = channel.item!!.forecast
        val weatherIconImageResource = resources.getIdentifier("icon_" + condition.code, "drawable", packageName)
        weatherIconImageView!!.setImageResource(weatherIconImageResource)
        temperatureTextView!!.text = getString(R.string.temperature_output, condition.temperature, units?.temperature)
        conditionTextView!!.text = condition.description
        locationTextView!!.text = channel.location

        forecast?.indices?.let { forecastList ->
            for (day in forecastList) {
                if (day >= 5) {
                    break
                }
                val currentCondition = forecast[day]
                val viewId = resources.getIdentifier("forecast_$day", "id", packageName)
                val fragment = supportFragmentManager.findFragmentById(viewId) as WeatherConditionFragment?

                fragment?.loadForecast(currentCondition, channel.units!!)
            }
        }

        viewModel.cacheWeather(baseContext, channel)
    }

    override fun serviceFailure(exception: Exception?) {
        loadingDialog?.hide()

        exception?.let { error ->
            Toast.makeText(this@WeatherActivity, error.message.orEmpty(), Toast.LENGTH_LONG).show()
        }
    }

    override fun geocodeSuccess(location: LocationResult?) = location?.let { place ->
        viewModel.refreshWeather(place.address, this)

        val editor = preferences!!.edit()
        editor.putString(getString(R.string.pref_cached_location), place.address)
        editor.apply()
    }

    override fun geocodeFailure(exception: Exception?) {
        viewModel.loadFromCache(baseContext, this)
    }

    override fun onLocationChanged(location: Location) {
        viewModel.refreshLocation(location, this)
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {
        // OPTIONAL: implement your custom logic here
    }

    override fun onProviderEnabled(s: String) {
        // OPTIONAL: implement your custom logic here
    }

    override fun onProviderDisabled(s: String) {
        // OPTIONAL: implement your custom logic here
    }

    companion object {
        var GET_WEATHER_FROM_CURRENT_LOCATION = 0x00001
    }
}