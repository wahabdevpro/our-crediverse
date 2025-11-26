package systems.concurrent.crediversemobile.utils

import android.content.Context
import io.grpc.Metadata
import systems.concurrent.crediversemobile.R
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class CustomUtils {
    class History<T> {
        private val history = mutableListOf<T>()

        fun reset() {
            while (history.size > 0) {
                history.removeAt(history.size - 1)
            }
        }

        fun push(item: T) {
            history.add(item)
        }

        fun pop(): T? {
            if (history.isNotEmpty()) {
                return history.removeAt(history.size - 1)
            }
            return null
        }

        val size get() = history.size
    }

    companion object {
        fun metadataKeyOf(name: String): Metadata.Key<String> {
            return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER)
        }

        fun getUuid(length: Int = 8): String {
            val uuid = UUID.randomUUID()
            val mostSignificantBits = uuid.mostSignificantBits
            val leastSignificantBits = uuid.leastSignificantBits

            // Concatenate the most significant and least significant bits as a hexadecimal string
            val shortUUID = java.lang.Long.toHexString(mostSignificantBits) +
                    java.lang.Long.toHexString(leastSignificantBits)

            // Take the first 8 characters
            return shortUUID.substring(0, length)
        }

        fun nowEpoch() = Instant.now().epochSecond

        val kotlinRandom get() = kotlin.random.Random(SecureRandom().nextLong())

        fun fiftyFiftyChance(): Boolean {
            return kotlinRandom.nextInt(0, 2).takeIf { it == 1 } == null
        }

        fun timeAgo(
            resources: android.content.res.Resources, epoch: Long,
            withDaysAndWeeks: Boolean = false,
            returnsDateOnly: Boolean = false
        ): String {
            val currentInstant = Instant.now()
            val epochInstant = Instant.ofEpochSecond(epoch)

            var futureTense = false
            var timeDifference = Duration.between(epochInstant, currentInstant)

            // Time is negative? - it's in the future.....
            if (timeDifference.seconds < 0) {
                timeDifference = Duration.between(currentInstant, epochInstant)
                futureTense = true
            }


            val lessThanAMinuteResource =
                if (futureTense) R.string.in_a_moment else R.string.just_now

            val withTense: (str: String) -> String = {
                if (futureTense) resources.getString(R.string.time_future_tense, it)
                else resources.getString(R.string.time_past_tense, it)
            }

            return when {
                timeDifference.seconds < Time.InSeconds.ONE_MINUTE ->
                    resources.getString(lessThanAMinuteResource)
                timeDifference.seconds < Time.InSeconds.ONE_HOUR ->
                    resources.getString(
                        R.string.x_minutes, Time.Math.roundToMinutes(timeDifference.seconds)
                    )
                timeDifference.seconds < Time.InSeconds.ONE_DAY -> {
                    resources.getString(
                        R.string.x_hours, Time.Math.roundToHours(timeDifference.seconds)
                    )
                }
                else -> {
                    val getFormattedDateString: (epoch: Long) -> String = {
                        if (returnsDateOnly) Formatter.dateStringFromEpoch(it, "MMMM d, yyyy")
                        else Formatter.dateStringFromEpoch(it, "EEE, MMM d, h:mm a")
                    }

                    if (!withDaysAndWeeks) return getFormattedDateString(epoch)

                    when {
                        timeDifference.seconds >= Time.InSeconds.ONE_DAY && timeDifference.seconds < Time.InSeconds.ONE_WEEK -> {
                            val days = Time.Math.roundToDays(timeDifference.seconds)
                            withTense(resources.getString(R.string.x_days, days))
                        }
                        timeDifference.seconds >= Time.InSeconds.ONE_WEEK && timeDifference.seconds < Time.InSeconds.FOUR_WEEKS -> {
                            val weeks = Time.Math.roundToWeeks(timeDifference.seconds)
                            withTense(resources.getString(R.string.x_weeks, weeks))
                        }
                        else -> getFormattedDateString(epoch)
                    }
                }
            }
        }

        fun dpToPx(context: Context, dp: Int): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }

        fun dateFromEpoch(deprecationDate: Long): String {
            val instant = Instant.ofEpochSecond(deprecationDate)

            // Define a custom date format
            val formatter = DateTimeFormatter
                .ofPattern("MMMM d, yyyy", Locale.ENGLISH)
                .withZone(ZoneId.systemDefault())

            // Format the Instant as a string using the custom format
            return formatter.format(instant)
        }
    }
}
