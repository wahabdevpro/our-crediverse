package systems.concurrent.crediversemobile.utils

import java.util.*

class Scheduler(private val _intervalSeconds: Int = 60) {
    private val timer = Timer()
    private var running = false
    private var method: (() -> Unit)? = null

    fun method(method: () -> Unit) {
        this.method = method
    }

    fun start(intervalSeconds: Int = _intervalSeconds, delaySeconds: Int = 0) {
        if (running || method == null) return

        val delayInMillis = if (delaySeconds == 0) 0L else delaySeconds * 1000L
        val periodInMillis = intervalSeconds * 1000L

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                method?.invoke()
            }
        }, delayInMillis, periodInMillis)
        running = true
    }

    fun stop() {
        if (!running) return
        timer.cancel()
    }
}