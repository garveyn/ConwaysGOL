package com.nick.conwaygameoflife

import android.os.Handler
import android.os.Looper


/**
 * UiUpdater inspired from here: https://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay
 *  answer from user Ravemir: https://stackoverflow.com/a/14234984
*  */
class UiUpdater(private val updater: Runnable, var interval: Long = 2000) {

    private val handler = Handler(Looper.getMainLooper())
    private val checker = Delayer()

    @Synchronized
    fun startUpdates() {
        checker.run()
    }

    @Synchronized
    fun stopUpdates() {
        handler.removeCallbacks(checker)
    }

    private inner class Delayer : Runnable {
        override fun run() {
            updater.run()
            handler.postDelayed(this, interval)
        }

    }


}