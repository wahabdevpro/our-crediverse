package systems.concurrent.crediversemobile

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
    }

    override fun getBaseContext(): Context {
        mContext = super.getBaseContext()
        return mContext as Context
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null
        val context: Context get() = mContext!!
    }
}