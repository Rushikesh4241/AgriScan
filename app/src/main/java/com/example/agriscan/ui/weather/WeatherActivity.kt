package com.example.agriscan.ui.weather

import android.widget.ImageView
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.agriscan.R
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import java.util.*

class WeatherActivity : AppCompatActivity() {

    lateinit var txtTemp: TextView
    lateinit var txtCondition: TextView
    lateinit var txtLocation: TextView
    lateinit var animWeather: LottieAnimationView
    lateinit var rvHourly: RecyclerView
    lateinit var rvDaily: RecyclerView
    lateinit var rootLayout: RelativeLayout

    lateinit var txtTime: TextView


    lateinit var imgWeatherIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        txtTemp = findViewById(R.id.txtTemp)
        txtCondition = findViewById(R.id.txtCondition)
        txtLocation = findViewById(R.id.txtLocation)
        animWeather = findViewById(R.id.animWeather)
        rvHourly = findViewById(R.id.rvHourly)
        rvDaily = findViewById(R.id.rvDaily)
        rootLayout = findViewById(R.id.rootLayout)
        imgWeatherIcon = findViewById(R.id.imgWeatherIcon)
        txtTime = findViewById(R.id.txtTime)
        startClock()

        getLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        client.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                getWeather(lat, lon) // ✅ accurate location
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getWeather(lat: Double, lon: Double) {

        val urlCurrent =
            "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=f44ecadc473671d02d41fd7922db7657&units=metric"

        val urlForecast =
            "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&appid=f44ecadc473671d02d41fd7922db7657&units=metric"

        // CURRENT WEATHER
        val request1 = JsonObjectRequest(Request.Method.GET, urlCurrent, null,
            { response ->
                val temp = response.getJSONObject("main").getDouble("temp")

                val weatherObj = response.getJSONArray("weather").getJSONObject(0)
                val condition = weatherObj.getString("description")

                val mainObj = response.getJSONObject("main")
                val feelsLike = mainObj.optDouble("feels_like", temp) // ✅ safe fallback

                val city = response.getString("name")

                val sys = response.getJSONObject("sys")
                val sunrise = sys.getLong("sunrise") * 1000
                val sunset = sys.getLong("sunset") * 1000
                val currentTime = System.currentTimeMillis()

                txtLocation.text = city
                txtTemp.text = "${temp.toInt()}°C"
                txtCondition.text = "$condition (Feels like ${feelsLike.toInt()}°C)"

                setAnimation(condition)
                setBackground(sunrise, sunset)
            },
            { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                error.printStackTrace()
            })

        // FORECAST
        val request2 = JsonObjectRequest(Request.Method.GET, urlForecast, null,
            { response ->
                val list = response.getJSONArray("list")
                setupHourly(list)
                setupDaily(list)
            },
            {
                Toast.makeText(this, "Forecast Error", Toast.LENGTH_SHORT).show()
            })

        val queue = Volley.newRequestQueue(this)
        queue.add(request1)
        queue.add(request2)
    }

    private fun setAnimation(condition: String) {
        try {
            when {
                condition.contains("clear", true) -> {
                    animWeather.setAnimation("sunny.json")
                    imgWeatherIcon.setImageResource(R.drawable.ic_sunny)
                }

                condition.contains("rain", true) ||
                        condition.contains("drizzle", true) -> {
                    animWeather.setAnimation("rain.json")
                    imgWeatherIcon.setImageResource(R.drawable.ic_rain)
                }

                condition.contains("cloud", true) -> {
                    animWeather.setAnimation("cloud.json")
                    imgWeatherIcon.setImageResource(R.drawable.ic_cloud)
                }

                condition.contains("mist", true) ||
                        condition.contains("fog", true) -> {
                    animWeather.setAnimation("cloud.json")
                    imgWeatherIcon.setImageResource(R.drawable.ic_cloud)
                }

                else -> {
                    animWeather.setAnimation("cloud.json")
                    imgWeatherIcon.setImageResource(R.drawable.ic_cloud)
                }
            }

            animWeather.playAnimation()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setBackground(sunrise: Long, sunset: Long) {
        val now = System.currentTimeMillis()

        if (now in sunrise..sunset) {
            rootLayout.setBackgroundResource(R.drawable.bg_day)
        } else {
            rootLayout.setBackgroundResource(R.drawable.bg_night)
        }
    }

    private fun setupHourly(array: JSONArray) {
        val list = ArrayList<HourlyModel>()

        for (i in 0 until 8) {
            val obj = array.getJSONObject(i)
            val temp = obj.getJSONObject("main").getDouble("temp")

            val dt = obj.getLong("dt") * 1000
            val time = java.text.SimpleDateFormat("hh a", Locale.getDefault())
                .format(Date(dt))

            list.add(HourlyModel(time, temp))
        }

        rvHourly.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvHourly.adapter = HourlyAdapter(list)
    }

    private fun setupDaily(array: JSONArray) {
        val list = ArrayList<DailyModel>()

        for (i in 0 until array.length() step 8) {
            val obj = array.getJSONObject(i)
            val temp = obj.getJSONObject("main").getDouble("temp")

            list.add(DailyModel("Day ${i/8}", temp))
        }

        rvDaily.layoutManager = LinearLayoutManager(this)
        rvDaily.adapter = DailyAdapter(list)
    }
    private fun startClock() {
        val handler = android.os.Handler(mainLooper)

        val runnable = object : Runnable {
            override fun run() {
                val time = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(Date())

                txtTime.text = time
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }
}