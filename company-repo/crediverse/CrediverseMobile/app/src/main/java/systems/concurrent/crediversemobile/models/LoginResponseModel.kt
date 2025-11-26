package systems.concurrent.crediversemobile.models

enum class AuthStatus {
    UNKNOWN_AUTHENTICATION_STATUS,
    AUTHENTICATED,
    REQUIRE_OTP
}

data class LoginResponseModel(
    val agentId: String,
    val agentMsisdn: String,
    val agentPin: String,
    val loginToken: String,
    val refreshToken: String,
    val authenticationStatus: AuthStatus,
    val message: String
)
