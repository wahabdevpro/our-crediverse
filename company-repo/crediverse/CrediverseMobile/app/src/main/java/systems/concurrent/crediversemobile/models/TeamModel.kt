package systems.concurrent.crediversemobile.models

data class TeamMemberModel(
    val msisdn: String,
    val firstName: String,
    val surname: String,
    val stockBalance: BalancesResponseModel,
    var salesTarget: SalesTargets?
)

data class TeamModel(
    val count: Int,
    val team: List<TeamMemberModel>,
)

data class SalesTargets(
    var dailyAmount: String?,
    var weeklyAmount: String?,
    var monthlyAmount: String?
)

data class TeamMembership(
    val msisdn: String,
    val teamLeadMsisdn: String,
    val salesTarget: SalesTargets?
)
