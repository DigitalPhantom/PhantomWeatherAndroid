/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Yoel Nunez <dev@nunez.guru>
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
package net.digitalphantom.app.weatherapp.service;

import android.location.Location;
import android.os.AsyncTask;

import net.digitalphantom.app.weatherapp.data.LocationResult;
import net.digitalphantom.app.weatherapp.listener.GeocodingServiceListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class GoogleMapsGeocodingService {
    private GeocodingServiceListener listener;
    private Exception error;

    public GoogleMapsGeocodingService(GeocodingServiceListener listener) {
        this.listener = listener;
    }

    public void refreshLocation(Location location) {
        new AsyncTask<Location, Void, LocationResult>() {
            @Override
            protected LocationResult doInBackground(Location... locations) {

                Location location = locations[0];

                String endpoint = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s", location.getLatitude(), location.getLongitude(), API_KEY);

                try {
                    URL url = new URL(endpoint);

                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);

                    InputStream inputStream = connection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    reader.close();

                    JSONObject data = new JSONObject(result.toString());

                    JSONArray results = data.optJSONArray("results");

                    if (results.length() == 0) {
                        error = new ReverseGeolocationException("Could not reverse geocode " + location.getLatitude() + ", " + location.getLongitude());

                        return null;
                    }

                    LocationResult locationResult = new LocationResult();
                    locationResult.populate(results.optJSONObject(0));

                    return locationResult;

                } catch (Exception e) {
                    error = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(LocationResult location) {

                if (location == null && error != null) {
                    listener.geocodeFailure(error);
                } else {
                    listener.geocodeSuccess(location);
                }

            }

        }.execute(location);
    }

    // OPTIONAL: Your Google Maps API KEY
    private static final String API_KEY = "";

    private class ReverseGeolocationException extends Exception {
        public ReverseGeolocationException(String detailMessage) {
            super(detailMessage);
        }
    }
}
