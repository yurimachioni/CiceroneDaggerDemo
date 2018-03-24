package com.machioni.ciceronedaggerdemo.common

import android.app.Application
import com.machioni.ciceronedaggerdemo.common.di.ApplicationComponent
import com.machioni.ciceronedaggerdemo.common.di.ApplicationModule
import com.machioni.ciceronedaggerdemo.common.di.DaggerApplicationComponent

class MyApplication : Application() {
    companion object {
        lateinit var daggerComponent: ApplicationComponent

    }

    override fun onCreate() {
        super.onCreate()

        daggerComponent = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(applicationContext))
                .build()
    }
}