package com.oneandonly.pythoncompiler

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

/**
 * Starts the embedded CPython runtime once, for the whole process.
 */
class PyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }
}
