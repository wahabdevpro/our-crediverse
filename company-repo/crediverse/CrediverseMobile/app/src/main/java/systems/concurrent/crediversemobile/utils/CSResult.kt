package systems.concurrent.crediversemobile.utils

import systems.concurrent.crediversemobile.repositories.BundleRepository
import systems.concurrent.crediversemobile.repositories.MasRepository

sealed class CSResult<out T>(val throwable: Throwable) {

    data class Success<out T>(val value: T) :
        CSResult<T>(CSException(MasRepository.ErrorMessages.INTERNAL_SERVER_ERROR))

    data class ValidationFailure(val exception: ValidatorException) :
        CSResult<Nothing>(exception)

    data class Failure(val exception: CSException) :
        CSResult<Nothing>(exception)

    data class MasFailure(val error: MasRepository.ErrorMessages) :
        CSResult<Nothing>(CSException(error))

    data class BundleFailure(val error: BundleRepository.ErrorMessages, val code: Int) :
        CSResult<Nothing>(CSException(error, code))
}

val CSResult<*>.isSuccess: Boolean get() = this is CSResult.Success
val CSResult<*>.isFailure: Boolean get() = this is CSResult.Failure
val CSResult<*>.isValidationFailure: Boolean get() = this is CSResult.ValidationFailure

fun <T> CSResult<T>.getOrThrow(): T {
    return if (this is CSResult.Success) value
    else throw throwable
}


fun <T> CSResult<T>.onSuccess(action: (T) -> Unit): CSResult<T> {
    if (this is CSResult.Success) action(value)

    return this
}

fun <T> CSResult<T>.onValidationFailure(action: (ValidatorException) -> Unit): CSResult<T> {
    if (this is CSResult.ValidationFailure) action(exception)

    return this
}

fun <T> CSResult<T>.onFailure(action: (CSException) -> Unit): CSResult<T> {
    when (this) {
        is CSResult.MasFailure -> action(CSException(error))
        is CSResult.BundleFailure -> action(CSException(error, code))
        is CSResult.Failure -> action(exception)
        else -> {}
    }
    return this
}

// Extension function to convert Result to CSResult
fun <T> Result<T>.toCSResult(): CSResult<T> {
    val getFinalException = { t: Throwable? ->
        val finalException = CSException(
            MasRepository.ErrorMessages.errorMessageExistsInStringOrNull(t?.message ?: "")
                ?: MasRepository.ErrorMessages.INTERNAL_SERVER_ERROR
        )

        CSResult.Failure(finalException)
    }

    return try {
        when {
            this.isSuccess -> CSResult.Success(getOrThrow())
            else -> getFinalException(exceptionOrNull())
        }
    } catch (t: Throwable) {
        getFinalException(t)
    }
}