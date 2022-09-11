package net.digitalphantom.app.weatherapp.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import net.digitalphantom.app.weatherapp.data.Channel
import net.digitalphantom.app.weatherapp.listener.GeocodingServiceListener
import net.digitalphantom.app.weatherapp.listener.WeatherServiceListener
import net.digitalphantom.app.weatherapp.service.GoogleMapsGeocodingService
import net.digitalphantom.app.weatherapp.service.WeatherCacheService
import net.digitalphantom.app.weatherapp.service.YahooWeatherService

class MainViewModel(
        private val weatherService: YahooWeatherService,
        private val geocodingService: GoogleMapsGeocodingService,
        private val cacheService: WeatherCacheService
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                        YahooWeatherService(),
                        GoogleMapsGeocodingService(),
                        WeatherCacheService()
                )
            }
        }
    }

    fun refreshWeather(location: String?, listener: WeatherServiceListener) {
        if (location == null) return

        // Create a new coroutine on the UI thread
        viewModelScope.launch {
            val result = try {
                weatherService.refreshWeather(location)
            } catch (exception: Exception) {
                Result.failure(exception)
            }

            if (result.isSuccess) {
                listener.serviceSuccess(result.getOrNull())
            } else {
                listener.serviceFailure(result.exceptionOrNull() as java.lang.Exception?)
            }
        }
    }


    fun refreshLocation(location: Location?, listener: GeocodingServiceListener) {
        if (location == null) return

        // Create a new coroutine on the UI thread
        viewModelScope.launch {
            val result = try {
                geocodingService.refreshLocation(location)
            } catch (exception: Exception) {
                Result.failure(exception)
            }

            if (result.isSuccess) {
                listener.geocodeSuccess(result.getOrNull())
            } else {
                listener.geocodeFailure(result.exceptionOrNull() as java.lang.Exception?)
            }
        }
    }

    fun loadFromCache(context: Context, listener: WeatherServiceListener) {
        // Create a new coroutine on the UI thread
        viewModelScope.launch {
            val result = try {
                cacheService.load(context)
            } catch (exception: Exception) {
                Result.failure(exception)
            }

            if (result.isSuccess) {
                listener.serviceSuccess(result.getOrNull())
            } else {
                listener.serviceFailure(result.exceptionOrNull() as java.lang.Exception?)
            }
        }
    }

    fun cacheWeather(context: Context, data: Channel) {
        // Create a new coroutine on the UI thread
        viewModelScope.launch {
            try {
                cacheService.save(data, context)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

}