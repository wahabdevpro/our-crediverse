package systems.concurrent.crediversemobile.utils

import systems.concurrent.crediversemobile.utils.Time.InSeconds.ONE_DAY
import systems.concurrent.crediversemobile.utils.Time.InSeconds.ONE_HOUR
import systems.concurrent.crediversemobile.utils.Time.InSeconds.ONE_MINUTE
import systems.concurrent.crediversemobile.utils.Time.InSeconds.ONE_WEEK
import kotlin.math.roundToLong

object Time {
    object InSeconds {
        const val ONE_MINUTE = 60
        const val TEN_MINUTES = 60 * 10
        const val FIFTEEN_MINUTES = 60 * 10

        const val ONE_HOUR = 3600
        const val TWO_HOURS = ONE_HOUR * 2

        const val ONE_DAY = 86400
        const val TWO_DAYS = ONE_DAY * 2
        const val THIRTY_DAYS = ONE_DAY * 30

        const val ONE_WEEK = ONE_DAY * 7

        // This is used to indicate "one week ago" if between 7 days and 10.5 days .... otherwise 'two weeks ago'
        const val ONE_WEEK_BOUNDARY = ONE_DAY * 3.5
        const val TWO_WEEKS = ONE_DAY * 7 * 2
        const val THREE_WEEKS = ONE_DAY * 7 * 3
        const val FOUR_WEEKS = ONE_DAY * 7 * 4
    }

    object Math {
        fun roundToMinutes(minutesInSeconds: Long) = (minutesInSeconds.toDouble() / ONE_MINUTE).roundToLong()
        fun roundToHours(hoursInSeconds: Long) = (hoursInSeconds.toDouble() / ONE_HOUR).roundToLong()
        fun roundToDays(daysInSeconds: Long) = (daysInSeconds.toDouble() / ONE_DAY).roundToLong()
        fun roundToWeeks(weeksInSeconds: Long) = (weeksInSeconds.toDouble() / ONE_WEEK).roundToLong()
    }
}