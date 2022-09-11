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

import android.content.Context
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
import android.view.ViewGroup
import net.digitalphantom.app.weatherapp.data.Channel
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder

class WeatherCacheService(private val context: Context) {
    private var error: Exception? = null
    private val CACHED_WEATHER_FILE = "weather.data"
    fun save(channel: Channel?) {
        object : AsyncTask<Channel, Void?, Void?>() {
            override fun doInBackground(channels: Array<Channel>): Void? {
                val outputStream: FileOutputStream
                try {
                    outputStream = context.openFileOutput(CACHED_WEATHER_FILE, Context.MODE_PRIVATE)
                    outputStream.write(channels[0].toJSON().toString().toByteArray())
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return null
            }
        }.execute(channel)
    }

    fun load(listener: WeatherServiceListener?) {
        object : AsyncTask<WeatherServiceListener?, Void?, Channel?>() {
            private var weatherListener: WeatherServiceListener? = null
            override fun doInBackground(serviceListeners: Array<WeatherServiceListener?>): Channel? {
                weatherListener = serviceListeners[0]
                try {
                    val inputStream = context.openFileInput(CACHED_WEATHER_FILE)
                    val cache = StringBuilder()
                    var content: Int
                    while (inputStream.read().also { content = it } != -1) {
                        cache.append(content.toChar())
                    }
                    inputStream.close()
                    val jsonCache = JSONObject(cache.toString())
                    val channel = Channel()
                    channel.populate(jsonCache)
                    return channel
                } catch (e: FileNotFoundException) { // cache file doesn't exist
                    error = CacheException(context.getString(R.string.cache_exception))
                } catch (e: Exception) {
                    error = e
                }
                return null
            }

            override fun onPostExecute(channel: Channel?) {
                if (channel == null && error != null) {
                    weatherListener!!.serviceFailure(error)
                } else {
                    weatherListener!!.serviceSuccess(channel)
                }
            }
        }.execute(listener)
    }

    private inner class CacheException internal constructor(detailMessage: String?) : Exception(detailMessage)
}