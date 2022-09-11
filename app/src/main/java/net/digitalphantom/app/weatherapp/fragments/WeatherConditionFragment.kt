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
package net.digitalphantom.app.weatherapp.fragments

import net.digitalphantom.app.weatherapp.data.JSONPopulator
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import android.os.AsyncTask
import net.digitalphantom.app.weatherapp.listener.WeatherServiceListener
import net.digitalphantom.app.weatherapp.service.WeatherCacheService.CacheException
import net.digitalphantom.app.weatherapp.R
import net.digitalphantom.app.weatherapp.service.YahooWeatherService.LocationWeatherException
import net.digitalphantom.app.weatherapp.listener.GeocodingServiceListener
import net.digitalphantom.app.weatherapp.data.LocationResult
import net.digitalphantom.app.weatherapp.service.GoogleMapsGeocodingService
import net.digitalphantom.app.weatherapp.service.GoogleMapsGeocodingService.ReverseGeolocationException
import android.preference.PreferenceFragment
import android.preference.Preference.OnPreferenceChangeListener
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.SharedPreferences
import android.preference.SwitchPreference
import android.preference.EditTextPreference
import android.os.Bundle
import android.preference.PreferenceManager
import android.content.Intent
import net.digitalphantom.app.weatherapp.WeatherActivity
import android.preference.Preference
import android.preference.ListPreference
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import net.digitalphantom.app.weatherapp.data.Condition
import net.digitalphantom.app.weatherapp.data.Units

class WeatherConditionFragment : Fragment() {
    private var weatherIconImageView: ImageView? = null
    private var dayLabelTextView: TextView? = null
    private var highTemperatureTextView: TextView? = null
    private var lowTemperatureTextView: TextView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_weather_condition, container, false)
        weatherIconImageView = view.findViewById<View>(R.id.weatherIconImageView) as ImageView
        dayLabelTextView = view.findViewById<View>(R.id.dayTextView) as TextView
        highTemperatureTextView = view.findViewById<View>(R.id.highTemperatureTextView) as TextView
        lowTemperatureTextView = view.findViewById<View>(R.id.lowTemperatureTextView) as TextView
        return view
    }

    fun loadForecast(forecast: Condition, units: Units) {
        val weatherIconImageResource = resources.getIdentifier("icon_" + forecast.code, "drawable", activity!!.packageName)
        weatherIconImageView!!.setImageResource(weatherIconImageResource)
        dayLabelTextView.setText(forecast.day)
        highTemperatureTextView!!.text = getString(R.string.temperature_output, forecast.highTemperature, units.temperature)
        lowTemperatureTextView!!.text = getString(R.string.temperature_output, forecast.lowTemperature, units.temperature)
    }
}