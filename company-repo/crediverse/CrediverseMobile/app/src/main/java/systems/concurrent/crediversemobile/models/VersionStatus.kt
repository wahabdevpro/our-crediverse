package systems.concurrent.crediversemobile.models

import android.content.Context
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.timeAgo
import systems.concurrent.crediversemobile.utils.Formatter

enum class VersionPriority(private val code: Int) {
    WHO_CARES(0), LOWEST(1), LOW(2), MEDIUM(3), HIGH(4), CRITICAL(5);

    fun isCritical() = this == CRITICAL
    fun notCritical() = this != CRITICAL
    fun isHigh() = this == HIGH

    fun matches(priority: VersionPriority) = this == priority

    companion object {
        fun fromCode(code: Int?): VersionPriority? {
            return when (code) {
                0 -> WHO_CARES
                1 -> LOWEST
                2 -> LOW
                3 -> MEDIUM
                4 -> HIGH
                5 -> CRITICAL
                else -> null
            }
        }
    }
}

data class VersionStatus(
    val ok: Boolean,
    val deprecationDate: Long?,
    val versionCodeLatest: Int,
    val versionNameLatest: String,
    val updatePriority: VersionPriority? = null,
    val updateDownloadUrl: String? = null,
) {
    private val hasDownloadUrl = !updateDownloadUrl.isNullOrEmpty()

    fun updateRequired(): Boolean {
        if (!hasDownloadUrl) return false
        return !ok || updatePriority?.isCritical() == true
    }

    fun updateAvailable() = hasDownloadUrl && updatePriority?.notCritical() == true

    fun deprecationString(context: Context): CharSequence {
        if (deprecationDate == null) return ""

        return Formatter.combine(
            "\n" + context.getString(R.string.version_deprecation_string) + "\n",
            Formatter.bold(
                timeAgo(
                    context.resources, deprecationDate,
                    withDaysAndWeeks = true, returnsDateOnly = true
                )
            )
        )
    }
}
