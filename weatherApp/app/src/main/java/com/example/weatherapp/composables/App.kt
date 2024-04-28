package com.example.weatherapp.composables

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.weatherapp.fetching.FetchViewModel
import com.example.weatherapp.location.LocationViewModel
import com.example.weatherapp.location.ReverseGeo

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun App() {
    val viewModel: LocationViewModel = viewModel()
    val fetchModel: FetchViewModel = viewModel()
    val reverseGeo: ReverseGeo = viewModel()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // Check if all requested permissions have been granted
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                // Start location updates through the ViewModel if permissions are granted
                viewModel.startLocationUpdates()
            }
        }
    )
    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
    Column (){
        Text(text = "Weather App")

        CurrentLocationDisplay(viewModel.location.collectAsState(), fetchModel, reverseGeo)
    }
}

@Composable
fun CurrentLocationDisplay(location: State<Location?>, fetchModel: FetchViewModel, reverseGeo: ReverseGeo) {
    var isLoading by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(location.value) {
        location.value?.let { loc ->
            try {
                latitude = loc.latitude
                longitude = loc.longitude
                if (name.isEmpty()) {
                    val cityName = reverseGeo.fetchGeoData(latitude, longitude)
                    name = cityName ?: "Unknown"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Fetching failed")
            } finally {
                isLoading = false
            }
        }
    }


    if (isLoading) {
        CircularProgressIndicator()
    } else {
        if (name.isNotEmpty()) {
            Text(text = name)
        } else {
            CircularProgressIndicator()

        }
    }










    /*
    if (isLoading){
        CircularProgressIndicator()
    }
     */

        /*
        LazyColumn (modifier = Modifier.padding(top = 20.dp)){
            items(fetchModel.weatherData.value) {
                Text(text = "Temp: ${it.current.temperature_2m}")
                Text(text = "${it.timezone}")
                it.hourly.time.forEachIndexed { i, time ->
                    val temperature = it.hourly.temperature_2m[i]
                    Text(text = "Time: $time, Temperature: $temperature")
                }
            }
        }
         */
}




