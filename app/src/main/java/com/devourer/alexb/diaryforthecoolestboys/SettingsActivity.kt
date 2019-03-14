package com.devourer.alexb.diaryforthecoolestboys

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w("Main", "SettingsActivity | onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_layout, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        Log.w("Main", "registerOnSharedPreferenceChangeListener | key -> $key")
        val flakesIntent = Intent("flakes")
        when (key){
            "Show flakes" -> {
                Log.w("Main", "key -> $key")
                flakesIntent.putExtra("key", "Show flakes")
                if (sharedPreferences.getBoolean("Show flakes", false))
                    flakesIntent.putExtra("isShow", true)
                else
                    flakesIntent.putExtra("isShow", false)
                LocalBroadcastManager.getInstance(this).sendBroadcast(flakesIntent)
            }
            "Choose flakes" -> {
                Log.w("Main", "key -> $key")
                flakesIntent.putExtra("key", "Choose flakes")
                LocalBroadcastManager.getInstance(this).sendBroadcast(flakesIntent)
            }
            else -> { }
        }
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(preferenceListener)
        Log.w("Main", "SettingsActivity | onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.w("Main", "SettingsActivity | onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w("Main", "SettingsActivity | onDestroy")
    }
}