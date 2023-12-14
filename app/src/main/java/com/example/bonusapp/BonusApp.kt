package com.example.bonusapp

import android.app.Application

class BonusApp : Application() {
    //I want to initialize the ReminderRepository as soon as the app starts, so this is the best
    //place to do it.
    override fun onCreate() {
        super.onCreate()
        ReminderRepository.initialize(this)
    }
}