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
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.digitalphantom.app.weatherapp.data.Channel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URL

class YahooWeatherService() {
    var temperatureUnit = "C"

    suspend fun refreshWeather(location: String?): Result<Channel> = withContext(Dispatchers.Default) {
        val channel = Channel()
        val unit = if (temperatureUnit.equals("f", ignoreCase = true)) "f" else "c"
        val YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\") and u='$unit'", location)
        val endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL))

        val url = URL(endpoint)

        url.openConnection()?.run {
            useCaches = false

            val reader = BufferedReader(InputStreamReader(inputStream))
            val result = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                result.append(line)
            }

            val data = JSONObject(result.toString())
            val queryResults = data.optJSONObject("query")
            val count = queryResults.optInt("count")

            if (count == 0) {
                throw LocationWeatherException("No weather information found for $location")
            }

            val channelJSON = queryResults.optJSONObject("results").optJSONObject("channel")
            channel.populate(channelJSON)

            return@withContext Result.success(channel)
        }

        throw LocationWeatherException("No weather information found for $location")
    }

    private inner class LocationWeatherException(detailMessage: String?) : Exception(detailMessage)
}