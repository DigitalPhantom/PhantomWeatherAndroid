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
 */
package net.digitalphantom.app.weatherapp.service;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import net.digitalphantom.app.weatherapp.data.Channel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YahooWeatherService {
    private WeatherServiceCallback callback;
    private Context context;
    private Exception error;

    private final String CACHED_WEATHER_FILE = "weather.data";

    public YahooWeatherService(WeatherServiceCallback callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    public void refreshWeather(String location) {

        new AsyncTask<String, Void, Channel>() {
            @Override
            protected Channel doInBackground(String... strings) {

                String location = strings[0];

                Channel channel = loadCache(location);

                if (channel != null) {
                    return channel;
                } else {
                    channel = new Channel();
                }

                String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")", location);

                String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));

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

                    JSONObject data = new JSONObject(result.toString());

                    JSONObject queryResults = data.optJSONObject("query");

                    int count = queryResults.optInt("count");

                    if (count == 0) {
                        error = new LocationWeatherException("No weather information found for " + location);
                        return null;
                    }

                    JSONObject channelJSON = queryResults.optJSONObject("results").optJSONObject("channel");

                    loadMetadata(location, channelJSON);

                    channel.populate(channelJSON);

                    cacheWeatherData(channel);

                    return channel;

                } catch (Exception e) {
                    error = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Channel channel) {

                if (channel == null && error != null) {
                    callback.serviceFailure(error);
                } else {
                    callback.serviceSuccess(channel);
                }

            }

        }.execute(location);
    }

    private void loadMetadata(String location, JSONObject channelJSON) throws ParseException, JSONException {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm aa z", Locale.US);

        Date lastBuildDate = sdf.parse(channelJSON.optString("lastBuildDate"));

        long ttl = channelJSON.optLong("ttl");

        long expiration = ttl * 60 * 1000 + lastBuildDate.getTime();

        channelJSON.put("expiration", expiration);
        channelJSON.put("requestLocation", location);
    }

    private void cacheWeatherData(Channel channel) {
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(CACHED_WEATHER_FILE, Context.MODE_PRIVATE);
            outputStream.write(channel.toJSON().toString().getBytes());
            outputStream.close();

        } catch (IOException e) {
            // IGNORE: file save operation failed
        }
    }

    private Channel loadCache(String location) {
        try {
            FileInputStream inputStream = context.openFileInput(CACHED_WEATHER_FILE);

            StringBuilder cache = new StringBuilder();
            int content;
            while ((content = inputStream.read()) != -1) {
                cache.append((char) content);
            }

            inputStream.close();

            JSONObject jsonCache = new JSONObject(cache.toString());

            Channel channel = new Channel();
            channel.populate(jsonCache);

            long now = (new Date()).getTime();

            if (channel.getExpiration() > now && channel.getLocation().equalsIgnoreCase(location)) {
                return channel;
            }

        } catch (Exception e) {
            context.deleteFile(CACHED_WEATHER_FILE);
        }

        return null;
    }

    public class LocationWeatherException extends Exception {
        public LocationWeatherException(String detailMessage) {
            super(detailMessage);
        }
    }
}
