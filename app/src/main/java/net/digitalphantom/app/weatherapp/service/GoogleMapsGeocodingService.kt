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

import org.json.JSONObject
import net.digitalphantom.app.weatherapp.data.LocationResult
import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.digitalphantom.app.weatherapp.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL

class GoogleMapsGeocodingService() {
    suspend fun refreshLocation(location: Location?): Result<LocationResult> = withContext(Dispatchers.IO) {
        val endpoint = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s", location?.latitude, location?.longitude, BuildConfig.GoogleMapsApiKey)

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
        val results = data.optJSONArray("results")

        if (results?.length()?.or(0) == 0) {
            throw ReverseGeolocationException("Could not reverse geocode " + location?.latitude + ", " + location?.longitude)
        }

        val locationResult = LocationResult()
        locationResult.populate(results.optJSONObject(0))
        return@withContext Result.success(locationResult)
    }

    private inner class ReverseGeolocationException(detailMessage: String?) : Exception(detailMessage)
}