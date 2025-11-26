package systems.concurrent.crediversemobile.utils

import systems.concurrent.crediversemobile.BuildConfig

object FeatureToggle {
    object Nav {
        const val hasBundlePage = BuildConfig.nav_bundle_page_enabled
        const val hasTransferPage: Boolean = BuildConfig.nav_transfer_page_enabled
        const val hasStatsPage: Boolean = BuildConfig.nav_stats_page_enabled
        const val hasExecuStatsPage: Boolean = BuildConfig.nav_execu_stats_page_enabled
        const val hasTeamPages: Boolean = BuildConfig.nav_team_navigation_enabled
        const val canBuyWithMobileMoney: Boolean = BuildConfig.nav_buy_with_mobile_money_enabled
        const val hasMobileMoneyDeposit: Boolean = BuildConfig.nav_mobile_money_deposit_enabled
        const val hasMobileMoneyWithdraw: Boolean = BuildConfig.nav_mobile_money_withdraw_enabled

        object Team {
            const val membersLocationMap = BuildConfig.team_agents_location_view_enabled
        }
    }
    object LoginPage {
        const val requestLocationPermission = BuildConfig.request_location_permission_enabled
    }

    object HomePage {
        const val showTotalAndTradeBonus = BuildConfig.show_total_and_trade_bonus
        const val showWeeklySalesBarGraph = BuildConfig.home_page_show_weekly_bar_graph
        const val showTodaySalesBreakdown = BuildConfig.home_page_show_today_sales_breakdown
    }

    object TeamMemberPage {
        const val showWeeklySalesBarGraph = BuildConfig.team_page_show_weekly_bar_graph
    }

    object AgentProfile {
        const val showsMyLocationButton = BuildConfig.my_location_enabled
        const val showsChangeMyPinButton = BuildConfig.change_pin_enabled
        const val showsUpdateMyProfileButton = BuildConfig.update_profile_enabled
        const val canChangeProfileNameFields = BuildConfig.allow_editing_profile_name_fields
    }

    object Stats {
        const val showSalesProfit = BuildConfig.sales_profit_enabled
        const val showCreditPurchased = BuildConfig.credit_purchased_enabled
    }
}

object AppFlag {
    object Analytics {
        const val eventFlushCount = BuildConfig.analytics_event_flush_count
    }

    object System {
        const val versionCode = BuildConfig.VERSION_CODE
        const val versionName = BuildConfig.VERSION_NAME
        const val buildNumber = BuildConfig.BUILD_NUMBER
    }

    object Sandbox {
        const val sandboxEnabled = BuildConfig.SANDBOX_ENABLED
        const val sandboxAutoLogin = BuildConfig.SANDBOX_AUTO_LOGIN
    }

    object LoginPage {
        const val forcedLanguage = BuildConfig.force_language
        const val defaultAppLanguage = BuildConfig.default_app_language

        const val logoHasPrimaryBackground = BuildConfig.logo_has_primary_background
        const val logoHasSecondaryBackground = BuildConfig.logo_has_secondary_background
    }

    object Network {
        const val masHostname = BuildConfig.mas_hostname
        const val masPort = BuildConfig.mas_port
        const val masCAPath = BuildConfig.mas_ca_path

        const val ssapiHostname = BuildConfig.ssapi_hostname
        const val ssapiPort = BuildConfig.ssapi_port
        const val ssapiCAPath = BuildConfig.ssapi_ca_path
    }

    object Stats {
        const val startOfWeek = BuildConfig.start_of_week
    }

    object Balance {
        const val updateThresholdSeconds = BuildConfig.balance_update_threshold_seconds
    }

    object TeamMembers {
        const val locationFilePath = BuildConfig.team_member_locations
    }

    object Currency {
        const val patternWithoutSymbol = BuildConfig.currency_pattern_without_currency
        const val pattern = BuildConfig.currency_pattern
        const val symbol = BuildConfig.currency_symbol
        val decimalSeparator = BuildConfig.currency_decimal_separator[0]
        val groupSeparator = BuildConfig.currency_group_separator[0]
        const val localeCode = BuildConfig.currency_locale_code
    }
}
