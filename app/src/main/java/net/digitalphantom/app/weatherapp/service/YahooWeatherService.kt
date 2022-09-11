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
package net.digitalphantom.app.weatherapp.service

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
import android.net.Uri
import net.digitalphantom.app.weatherapp.WeatherActivity
import android.preference.Preference
import android.preference.ListPreference
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import net.digitalphantom.app.weatherapp.data.Channel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL

class YahooWeatherService(private val listener: WeatherServiceListener) {
    private var error: Exception? = null
    private var temperatureUnit = "C"
        set

    fun refreshWeather(location: String?) {
        object : AsyncTask<String, Void?, Channel?>() {
            override fun doInBackground(locations: Array<String>): Channel? {
                val location = locations[0]
                val channel = Channel()
                val unit = if (temperatureUnit.equals("f", ignoreCase = true)) "f" else "c"
                val YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\") and u='$unit'", location)
                val endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL))
                try {
                    val url = URL(endpoint)
                    val connection = url.openConnection()
                    connection.useCaches = false
                    val inputStream = connection.getInputStream()
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                    reader.close()
                    val data = JSONObject(result.toString())
                    val queryResults = data.optJSONObject("query")
                    val count = queryResults.optInt("count")
                    if (count == 0) {
                        error = LocationWeatherException("No weather information found for $location")
                        return null
                    }
                    val channelJSON = queryResults.optJSONObject("results").optJSONObject("channel")
                    channel.populate(channelJSON)
                    return channel
                } catch (e: Exception) {
                    error = e
                }
                return null
            }

            override fun onPostExecute(channel: Channel?) {
                if (channel == null && error != null) {
                    listener.serviceFailure(error)
                } else {
                    listener.serviceSuccess(channel)
                }
            }
        }.execute(location)
    }

    private inner class LocationWeatherException internal constructor(detailMessage: String?) : Exception(detailMessage)
}