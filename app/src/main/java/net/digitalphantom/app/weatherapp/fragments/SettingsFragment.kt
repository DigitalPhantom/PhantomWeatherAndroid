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

import net.digitalphantom.app.weatherapp.R
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.SharedPreferences
import android.os.Bundle
import android.content.Intent
import net.digitalphantom.app.weatherapp.WeatherActivity
import android.view.MenuItem
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat(), OnPreferenceChangeListener, OnSharedPreferenceChangeListener {
    private lateinit var preferences: SharedPreferences
    private lateinit var geolocationEnabledPreference: SwitchPreferenceCompat
    private lateinit var manualLocationPreference: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)

        preferences = preferenceManager.sharedPreferences!!
        preferences.registerOnSharedPreferenceChangeListener(this)

        geolocationEnabledPreference = findPreference(getString(R.string.pref_geolocation_enabled))!!
        manualLocationPreference = findPreference(getString(R.string.pref_manual_location))!!

        bindPreferenceSummaryToValue(manualLocationPreference)
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_temperature_unit)))

        if (!preferences.getBoolean(getString(R.string.pref_needs_setup), false)) {
            val editor = preferences.edit()
            editor.putBoolean(getString(R.string.pref_needs_setup), false)
            editor.apply()
        }

        onSharedPreferenceChanged(preferences, geolocationEnabledPreference.key)
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


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, preferenceKey: String) {
        if (preferenceKey == geolocationEnabledPreference.key) {
            manualLocationPreference.isEnabled = geolocationEnabledPreference.isChecked == false
        }
    }

    private fun bindPreferenceSummaryToValue(preference: androidx.preference.Preference?) = preference?.apply {
        onPreferenceChangeListener = this@SettingsFragment
        onPreferenceChange(preference, preferences.getString(preference.key, null)!!)
    }

    override fun onPreferenceChange(preference: Preference, value: Any?): Boolean {
        val stringValue = value.toString()

        when (preference) {
            is androidx.preference.ListPreference -> {
                val index = preference.findIndexOfValue(stringValue)
                preference.summary = if (index >= 0) preference.entries[index] else null
            }
            is EditTextPreference -> {
                preference.summary = stringValue
            }
        }

        return true
    }
}