package com.example.chadrick.datalabeling.Models

import android.app.Application
import android.content.Context
import com.squareup.leakcanary.LeakCanary

/**
 * Created by chadrick on 17. 12. 14.
 */
class App : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: App? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        // initialize for any

        // Use ApplicationContext.
        // example: SharedPreferences etc...
        val context: Context = App.applicationContext()

        if(LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        LeakCanary.install(this)
    }
}