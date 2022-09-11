package net.digitalphantom.app.weatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import net.digitalphantom.app.weatherapp.listener.WeatherServiceListener
import net.digitalphantom.app.weatherapp.service.YahooWeatherService

class WeatherViewModel(
        private val weatherService: YahooWeatherService
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WeatherViewModel(YahooWeatherService())
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
}