package com.jonassode.shortsorpants

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val prefs = context.getSharedPreferences("location", Context.MODE_PRIVATE)
        val latStr = prefs.getString("lat", null)
        val lonStr = prefs.getString("lon", null)

        if (latStr == null || lonStr == null) {
            pendingResult.finish()
            return
        }

        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()
        if (lat == null || lon == null) {
            pendingResult.finish()
            return
        }

        Thread {
            try {
                fetchWeatherAndNotify(context, lat, lon)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    private fun fetchWeatherAndNotify(context: Context, lat: Double, lon: Double) {
        try {
            val urlStr = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lon" +
                "&daily=temperature_2m_max,precipitation_sum" +
                "&timezone=auto&forecast_days=1"

            val conn = URL(urlStr).openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            val json = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val daily = JSONObject(json).getJSONObject("daily")
            val maxTemp = daily.getJSONArray("temperature_2m_max").getDouble(0)
            val precipitation = daily.getJSONArray("precipitation_sum").optDouble(0, 0.0)

            val isShorts = maxTemp >= SHORTS_TEMP_THRESHOLD_C && precipitation < MAX_PRECIPITATION_MM
            val emoji = if (isShorts) "🩳" else "👖"
            val clothing = if (isShorts) "Shorts" else "Pants"
            val tempText = "%.1f°C peak".format(maxTemp)
            val rainText = if (precipitation > 0) "🌧️ %.1f mm rain".format(precipitation) else "☀️ No rain"

            val title = "$emoji Wear $clothing today!"
            val body = "🌡️ $tempText · $rainText"

            NotificationHelper.show(context, title, body)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch weather or show notification", e)
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val REQUEST_CODE = 42
        private const val SHORTS_TEMP_THRESHOLD_C = 25.0
        private const val MAX_PRECIPITATION_MM = 0.1

        fun schedule(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val pendingIntent = buildPendingIntent(context)

            val triggerAt = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }.timeInMillis

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        private fun buildPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java)
            return PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
