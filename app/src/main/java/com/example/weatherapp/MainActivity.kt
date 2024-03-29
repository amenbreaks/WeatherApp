package com.example.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL


@Entity(tableName = "weather_data")
data class WeatherData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val latitude: Double,
    val longitude: Double,
    val date: String,
    val maxTemperature: String,
    val minTemperature: String
)

@Database(entities = [WeatherData::class], version = 1, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherData: WeatherData)
    @Query("SELECT * FROM weather_data WHERE latitude = :latitude AND longitude = :longitude AND date = :date")
    suspend fun getWeatherData(latitude: Double, longitude: Double, date: String): WeatherData?
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
//                    val weatherDatabase = WeatherDatabase.getInstance(applicationContext)
//                    WeatherApp(weatherDatabase, applicationContext)
                    WeatherApp()
                }
            }
        }
    }

    @Composable
    fun WeatherApp() {
        var date by remember { mutableStateOf("") }
        var latitude by remember { mutableStateOf("") }
        var longitude by remember { mutableStateOf("") }
        var maxTemperature by remember { mutableStateOf("") }
        var minTemperature by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Weather App", fontSize = 24.sp, color = Color.White)

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text(text = "Latitude") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text(text = "Longitude") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = date,
                onValueChange = { date = it },
                label = { Text(text = "Date (YYYY-MM-DD)") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (date.isNotEmpty() && latitude.isNotEmpty() && longitude.isNotEmpty()) {
                        isLoading = true
                        // Launch coroutine to fetch weather data
                        coroutineScope.launch {
                            fetchWeatherData(latitude.toDouble(), longitude.toDouble(), date) { maxTemp, minTemp ->
                                maxTemperature = maxTemp
                                minTemperature = minTemp
                                isLoading = false
                            }
                        }
                    }
                }
            ) {
                Text(text = "Get Weather Data")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Text(text = "Loading...")
            } else {
                Text(text = "Max Temperature: $maxTemperature\u00B0 C")
                Text(text = "Min Temperature: $minTemperature\u00B0 C")
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }

    private suspend fun fetchWeatherData(
        latitude: Double,
        longitude: Double,
        date: String,
//        weatherDatabase: WeatherDatabase,
//        context: Context,
        callback: (String, String) -> Unit
    ) {
        try {
//            if (isNetworkAvailable(context)) {
            val apiUrl =
                "https://archive-api.open-meteo.com/v1/archive?latitude=$latitude&longitude=$longitude&start_date=$date&end_date=$date&hourly=temperature_2m"

            // Log the API URL
            println("Fetching weather data from: $apiUrl")

            val response = withContext(Dispatchers.IO) {
                URL(apiUrl).readText()
            }

            // Log the API response
            println("Received response: $response")

            val jsonResponse = JSONObject(response)
            val hourlyData = jsonResponse.getJSONObject("hourly")
            val temperatureList = hourlyData.getJSONArray("temperature_2m")
            val temperatures = mutableListOf<Double>()
            for (i in 0 until temperatureList.length()) {
                temperatures.add(temperatureList.getDouble(i))
            }

            // Calculate max and min temperatures
            val maxTemp = temperatures.maxOrNull()?.toString() ?: ""
            val minTemp = temperatures.minOrNull()?.toString() ?: ""

            // Insert weather data into the database
            val weatherData = WeatherData(
                latitude = latitude,
                longitude = longitude,
                date = date,
                maxTemperature = maxTemp,
                minTemperature = minTemp
            )
//                weatherDatabase.weatherDao().insertWeatherData(weatherData)

            callback(maxTemp, minTemp)
//            } else {
//                // If no internet, query the database
//                val storedWeatherData =
//                    weatherDatabase.weatherDao().getWeatherData(latitude, longitude, date)
//                if (storedWeatherData != null) {
//                    callback(storedWeatherData.maxTemperature, storedWeatherData.minTemperature)
//                } else {
//                    // If no matching data found in the database, callback with dummy strings
//                    callback("Not Found", "Not Found")
//                }
//            }
        } catch (e: Exception) {
            // Log any exceptions that occur during the fetch operation
            println("Error fetching weather data: ${e.message}")
        }
    }
}
