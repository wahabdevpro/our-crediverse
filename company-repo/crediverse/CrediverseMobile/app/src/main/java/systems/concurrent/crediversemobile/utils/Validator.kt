package systems.concurrent.crediversemobile.utils

fun Throwable.toValidatorException(): ValidatorException {
    return ValidatorException(this.message.toString())
}

class ValidatorException : Exception {
    private var errorMessage: Validator.Failure

    constructor(error: String) : super(error) {
        this.errorMessage = Validator.Failure.findOrNull(error)
            ?: Validator.Failure.UNKNOWN
    }

    constructor(error: Validator.Failure) : super(error.toString()) {
        this.errorMessage = error
    }

    fun isAnyOf(vararg errors: Validator.Failure): Boolean {
        return errors.firstOrNull { this.errorMessage == it } != null
    }
}

object Validator {
    enum class Failure {
        UNKNOWN, BELOW_MIN, ABOVE_MAX, IS_ZERO, IS_NULL;

        companion object {
            fun findOrNull(string: String): Failure? {
                return values().firstOrNull { it.toString() == string }
            }
        }
    }

    fun getDoubleNonZero(
        value: String, minValue: Double? = null, maxValue: Double? = null,
    ): CSResult<Double> {
        return when (val converted = value.toDoubleOrNull()) {
            null -> CSResult.ValidationFailure(ValidatorException(Failure.IS_NULL))
            else -> {
                if (converted == 0.0) CSResult.ValidationFailure(ValidatorException(Failure.IS_ZERO))
                else if (minValue != null && converted < minValue) CSResult.ValidationFailure(
                    ValidatorException(Failure.BELOW_MIN)
                )
                else if (maxValue != null && converted > maxValue) CSResult.ValidationFailure(
                    ValidatorException(Failure.ABOVE_MAX)
                )
                else CSResult.Success(converted)
            }
        }
    }
}
