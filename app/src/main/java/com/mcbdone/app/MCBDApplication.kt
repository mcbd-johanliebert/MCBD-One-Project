package com.mcbdone.app

import android.app.Application

class MCBDApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MCBDApplication
            private set
    }
}
