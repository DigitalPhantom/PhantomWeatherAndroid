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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.digitalphantom.app.weatherapp.data.Condition
import net.digitalphantom.app.weatherapp.data.Units
import net.digitalphantom.app.weatherapp.databinding.FragmentWeatherConditionBinding

class WeatherConditionFragment : Fragment() {
    private var _binding: FragmentWeatherConditionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWeatherConditionBinding.inflate(inflater, container, false)

        return binding.root
    }

    fun loadForecast(forecast: Condition, units: Units) {
        val weatherIconImageResource = resources.getIdentifier("icon_" + forecast.code, "drawable", requireActivity().packageName)

        binding.apply {
            weatherIconImageView.setImageResource(weatherIconImageResource)
            dayTextView.text = forecast.day
            highTemperatureTextView.text = getString(R.string.temperature_output, forecast.highTemperature, units.temperature)
            lowTemperatureTextView.text = getString(R.string.temperature_output, forecast.lowTemperature, units.temperature)
        }
    }
}