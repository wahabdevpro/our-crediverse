package systems.concurrent.crediversemobile

import android.app.Application
import android.content.Context

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set // Make the setter private to prevent modification from outside

        // A safer way to get the application context
        val context: Context
            get() = instance.applicationContext
    }
}
