package systems.concurrent.crediversemobile.utils

import systems.concurrent.crediversemobile.BuildConfig

object FeatureToggle {
    object Nav {
        val hasBundlePage get() = FeatureConfigStore.getBoolean("nav_bundle_page_enabled", BuildConfig.nav_bundle_page_enabled)
        val hasTransferPage get() = FeatureConfigStore.getBoolean("nav_transfer_page_enabled", BuildConfig.nav_transfer_page_enabled)
        val hasStatsPage get() = FeatureConfigStore.getBoolean("nav_stats_page_enabled", BuildConfig.nav_stats_page_enabled)
        val hasExecuStatsPage get() = FeatureConfigStore.getBoolean("nav_execu_stats_page_enabled", BuildConfig.nav_execu_stats_page_enabled)
        val hasTeamPages get() = FeatureConfigStore.getBoolean("nav_team_navigation_enabled", BuildConfig.nav_team_navigation_enabled)
        val canBuyWithMobileMoney get() = FeatureConfigStore.getBoolean("nav_buy_with_mobile_money_enabled", BuildConfig.nav_buy_with_mobile_money_enabled)
        val hasMobileMoneyDeposit get() = FeatureConfigStore.getBoolean("nav_mobile_money_deposit_enabled", BuildConfig.nav_mobile_money_deposit_enabled)
        val hasMobileMoneyWithdraw get() = FeatureConfigStore.getBoolean("nav_mobile_money_withdraw_enabled", BuildConfig.nav_mobile_money_withdraw_enabled)

        object Team {
            val membersLocationMap get() = FeatureConfigStore.getBoolean("team_agents_location_view_enabled", BuildConfig.team_agents_location_view_enabled)
        }
    }
    object LoginPage {
        val requestLocationPermission get() = FeatureConfigStore.getBoolean("request_location_permission_enabled", BuildConfig.request_location_permission_enabled)
    }

    object HomePage {
        val showTotalAndTradeBonus get() = FeatureConfigStore.getBoolean("show_total_and_trade_bonus", BuildConfig.show_total_and_trade_bonus)
        val showWeeklySalesBarGraph get() = FeatureConfigStore.getBoolean("home_page_show_weekly_bar_graph", BuildConfig.home_page_show_weekly_bar_graph)
        val showTodaySalesBreakdown get() = FeatureConfigStore.getBoolean("home_page_show_today_sales_breakdown", BuildConfig.home_page_show_today_sales_breakdown)
        val showSalesTarget get() = FeatureConfigStore.getBoolean("home_page_show_sales_target", BuildConfig.home_page_show_sales_target)
    }

    object TeamMemberPage {
        val showWeeklySalesBarGraph get() = FeatureConfigStore.getBoolean("team_page_show_weekly_bar_graph", BuildConfig.team_page_show_weekly_bar_graph)
    }

    object AgentProfile {
        val showsMyLocationButton get() = FeatureConfigStore.getBoolean("my_location_enabled", BuildConfig.my_location_enabled)
        val showsChangeMyPinButton get() = FeatureConfigStore.getBoolean("change_pin_enabled", BuildConfig.change_pin_enabled)
        val showsUpdateMyProfileButton get() = FeatureConfigStore.getBoolean("update_profile_enabled", BuildConfig.update_profile_enabled)
        val canChangeProfileNameFields get() = FeatureConfigStore.getBoolean("allow_editing_profile_name_fields", BuildConfig.allow_editing_profile_name_fields)
    }

    object Stats {
        val showSalesProfit get() = FeatureConfigStore.getBoolean("sales_profit_enabled", BuildConfig.sales_profit_enabled)
        val showCreditPurchased get() = FeatureConfigStore.getBoolean("credit_purchased_enabled", BuildConfig.credit_purchased_enabled)
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
        const val masTlsAuthority = BuildConfig.mas_tls_authority

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
