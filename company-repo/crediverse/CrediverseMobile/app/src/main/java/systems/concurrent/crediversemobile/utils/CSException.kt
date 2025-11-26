package systems.concurrent.crediversemobile.utils

import android.content.Context
import systems.concurrent.crediversemobile.repositories.BundleRepository
import systems.concurrent.crediversemobile.repositories.MasRepository

open class CSException : Exception {
    var _code: Int? = null
    val code get() = _code

    fun toCSResultFailure(): CSResult.Failure {
        return CSResult.Failure(this)
    }

    private var error: Any

    constructor(error: String) : super(error) {
        this.error = MasRepository.ErrorMessages.errorMessageExistsInStringOrNull(error)
            ?: MasRepository.ErrorMessages.INTERNAL_SERVER_ERROR
    }

    constructor(error: MasRepository.ErrorMessages) : super(error.toString()) {
        this.error = error
    }

    constructor(error: BundleRepository.ErrorMessages, code: Int) : super(error.toString()) {
        this._code = code
        this.error = error
    }

    fun isAnyOf(vararg errors: BundleRepository.ErrorMessages): Boolean {
        if (this.error !is BundleRepository.ErrorMessages) return false
        return errors.firstOrNull { return this.error == it } != null
    }

    fun isAnyOf(vararg errors: MasRepository.ErrorMessages): Boolean {
        if (this.error !is MasRepository.ErrorMessages) return false
        return errors.firstOrNull { return this.error == it } != null
    }

    fun getStringFromResourceOrDefault(
        context: Context,
        defaultResource: Int = MasRepository.ErrorMessages.INTERNAL_SERVER_ERROR.resource,
        code: Int? = null
    ): String {
        var resource = defaultResource

        if (this.error is BundleRepository.ErrorMessages)
            resource = (this.error as BundleRepository.ErrorMessages).resource
        else if (this.error is MasRepository.ErrorMessages)
            resource = (this.error as MasRepository.ErrorMessages).resource

        return if (code != null) context.getString(resource, code.toString())
        else context.getString(resource)
    }
}

fun Throwable.toCSException(): CSException {
    return CSException(this.message.toString())
}
