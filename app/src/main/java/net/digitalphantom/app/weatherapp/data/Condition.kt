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
package net.digitalphantom.app.weatherapp.data

import org.json.JSONObject
import org.json.JSONException

class Condition : JSONPopulator {
    var code = 0
    var temperature = 0
    var highTemperature = 0
    var lowTemperature = 0
    var description: String? = null
    var day: String? = null

    override fun populate(data: JSONObject?) {
        if (data == null) return

        code = data.optInt("code")
        temperature = data.optInt("temp")
        highTemperature = data.optInt("high")
        lowTemperature = data.optInt("low")
        description = data.optString("text")
        day = data.optString("day")
    }

    override fun toJSON(): JSONObject {
        val data = JSONObject()
        try {
            data.put("code", code)
            data.put("temp", temperature)
            data.put("high", highTemperature)
            data.put("low", lowTemperature)
            data.put("text", description)
            data.put("day", day)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return data
    }
}