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
import android.view.MenuItem
import android.view.ViewGroup

class SettingsFragment : PreferenceFragment(), OnPreferenceChangeListener, OnSharedPreferenceChangeListener {
    private var preferences: SharedPreferences? = null
    private var geolocationEnabledPreference: SwitchPreference? = null
    private var manualLocationPreference: EditTextPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.app_preferences)
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        geolocationEnabledPreference = findPreference(getString(R.string.pref_geolocation_enabled)) as SwitchPreference
        manualLocationPreference = findPreference(getString(R.string.pref_manual_location)) as EditTextPreference
        bindPreferenceSummaryToValue(manualLocationPreference)
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_temperature_unit)))
        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(this)
        onSharedPreferenceChanged(null, null)
        if (!preferences.getBoolean(getString(R.string.pref_needs_setup), false)) {
            val editor = preferences.edit()
            editor.putBoolean(getString(R.string.pref_needs_setup), false)
            editor.apply()
        }
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            startActivity(Intent(activity, WeatherActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (geolocationEnabledPreference!!.isChecked) {
            manualLocationPreference!!.isEnabled = false
        } else {
            manualLocationPreference!!.isEnabled = true
        }
    }

    private fun bindPreferenceSummaryToValue(preference: Preference?) {
        preference!!.onPreferenceChangeListener = this
        onPreferenceChange(preference, preferences!!.getString(preference.key, null)!!)
    }

    override fun onPreferenceChange(preference: Preference, value: Any): Boolean {
        val stringValue = value.toString()
        if (preference is ListPreference) {
            val listPreference = preference
            val index = listPreference.findIndexOfValue(stringValue)
            preference.setSummary(if (index >= 0) listPreference.entries[index] else null)
        } else (preference as? EditTextPreference)?.summary = stringValue
        return true
    }
}