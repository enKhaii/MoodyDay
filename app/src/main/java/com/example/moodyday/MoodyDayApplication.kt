package com.example.moodyday

import android.app.Application
import com.example.moodyday.data.AppContainer

class MoodyDayApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}