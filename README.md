This is the Assignment 2 submission from Dhruv Malik 2020373

The Weather App with a database

MainActivity

onCreate(savedInstanceState: Bundle?)
Initializes the activity when it is first created.
Sets the content view of the activity to the WeatherApp.

isNetworkAvailable(context: Context): Boolean
Checks if the device has an active internet connection.
Returns true if there is an internet connection; otherwise, returns false.

fetchWeatherData
Fetches weather data from the Open Meteo API.
Handles cases where the device is online or offline.
Inserts fetched data into the Room database if available.
If no data is found, calculates the average of past available values for the requested date.

WeatherApp

WeatherApp(weatherDatabase: WeatherDatabase, context: Context)
Composable function representing the UI of the WeatherApp.
Displays input fields for latitude, longitude, and date along with a button to fetch weather data.
Displays loading indicator while fetching data.
Displays maximum and minimum temperatures once data is fetched.
getPreviousYearsPattern(date: String): String
Helper function to generate a pattern of previous years' dates for database query.

WeatherDao

insertWeatherData(weatherData: WeatherData)
Inserts weather data into the Room database.
Replaces existing data if a conflict occurs.

getWeatherData(latitude: Double, longitude: Double, date: String): WeatherData?
Retrieves weather data from the Room database based on latitude, longitude, and date.
getPastWeatherData(date: String): List<WeatherData>
Retrieves past weather data from the Room database based on the date.

WeatherData

Represents a data class for weather data, including latitude, longitude, date, max temperature, and min temperature.

WeatherDatabase
getInstance(context: Context): WeatherDatabase
Singleton pattern to get an instance of the Room database.

--------------------------------


For Q1:
Utilizing the API and downloading the data - 10 marks

Proper implementation of API calls to fetch weather data.
Successful retrieval and processing of data from the API.
Creation of the UI - 10 marks

Implementation of user interface elements such as text fields, buttons, and loading indicators.
Proper layout and arrangement of UI components for user interaction.
Parsing of JSON files - 15 marks

Successful parsing of JSON responses from the API to extract relevant weather information.
Proper handling of JSON parsing errors and exceptions.
Proper output and running code - 15 marks

Correct display of weather data on the UI after fetching from the API.
Code runs without runtime errors or crashes.
App functions as expected with accurate weather information displayed.
Validation of user input, proper error messages, and running app - 10 marks

Validation of user input for latitude, longitude, and date fields.
Display of appropriate error messages for invalid input or network errors.
App runs smoothly and handles edge cases gracefully.
For Q2:
Creation of database and schema - 10 marks

Proper implementation of Room database with appropriate schema for storing weather data.
Successful creation of database tables and entities.
Insertion of data into the database and sending queries - 15 marks

Correct insertion of weather data into the database upon retrieval from the API.
Implementation of queries to retrieve weather data based on specified criteria such as latitude, longitude, and date.
Identification of cases where calculation is necessary, and computing it - 10 marks

Proper identification of cases where calculation of averages for past weather data is required.
Accurate computation of average temperatures based on past available data.
Checking network connectivity and its associated logic - 10 marks

Implementation of logic to check network connectivity status before making API calls.
Handling of scenarios where the device is offline and retrieving data from the local database.
Proper error messages, correct output, and running app - 15 marks

Display of relevant error messages for database query failures, network errors, or other exceptions.
Correct output of average temperatures calculated from past data.
App functions smoothly, providing accurate weather information even in offline scenarios.


