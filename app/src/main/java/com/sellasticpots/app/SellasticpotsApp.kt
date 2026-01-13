package com.sellasticpots.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SellasticpotsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Force light mode globally - disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}

