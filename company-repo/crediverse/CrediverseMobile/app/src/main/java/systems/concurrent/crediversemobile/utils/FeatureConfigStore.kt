package systems.concurrent.crediversemobile.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import systems.concurrent.crediversemobile.BuildConfig
import systems.concurrent.masapi.MasApi

/**
 * Runtime feature toggle store backed by SharedPreferences.
 *
 * Replaces compile-time BuildConfig toggles with runtime-configurable values.
 * BuildConfig values serve as fallback defaults when no remote config is available.
 *
 * Usage:
 *  1. Call [initialize] once after successful login (before navigating to home)
 *  2. Call [syncFromFeatureList] with the API response, or [syncFromRemote] for defaults
 *  3. Read values via [getBoolean] with BuildConfig fallback
 *  4. Call [clear] on logout
 */
object FeatureConfigStore {
    private const val PREFS_NAME = "FEATURE_CONFIG_STORE"
    private val _tag = "FeatureConfigStore"

    private var prefs: SharedPreferences? = null

    /**
     * Maps each backend Feature enum to our internal SharedPreferences key.
     * The backend key names differ from ours (e.g. FEATURE_SHOW_LOCATION → my_location_enabled).
     */
    private val featureToKeyMap = mapOf(
        // Navigation
        MasApi.Feature.FEATURE_NAV_BUNDLE_PAGE to "nav_bundle_page_enabled",
        MasApi.Feature.FEATURE_NAV_STATS_PAGE to "nav_stats_page_enabled",
        MasApi.Feature.FEATURE_NAV_TRANSFER_PAGE to "nav_transfer_page_enabled",
        MasApi.Feature.FEATURE_NAV_EXECU_STATS_PAGE to "nav_execu_stats_page_enabled",
        MasApi.Feature.FEATURE_NAV_BUY_WITH_MOBILE_MONEY to "nav_buy_with_mobile_money_enabled",
        MasApi.Feature.FEATURE_NAV_TEAM_NAVIGATION to "nav_team_navigation_enabled",
        MasApi.Feature.FEATURE_NAV_MOBILE_MONEY_DEPOSIT to "nav_mobile_money_deposit_enabled",
        MasApi.Feature.FEATURE_NAV_MOBILE_MONEY_WITHDRAW to "nav_mobile_money_withdraw_enabled",

        // Team
        MasApi.Feature.FEATURE_TEAM_AGENTS_LOCATION_VIEW to "team_agents_location_view_enabled",
        MasApi.Feature.FEATURE_TEAM_PAGE_SHOW_WEEKLY_BAR_GRAPH to "team_page_show_weekly_bar_graph",

        // Home Dashboard
        MasApi.Feature.FEATURE_SHOW_TOTAL_AND_TRADE_BONUS to "show_total_and_trade_bonus",
        MasApi.Feature.FEATURE_HOME_PAGE_SHOW_WEEKLY_BAR_GRAPH to "home_page_show_weekly_bar_graph",
        MasApi.Feature.FEATURE_HOME_PAGE_SHOW_TODAY_SALES_BREAKDOWN to "home_page_show_today_sales_breakdown",

        // Agent Profile
        MasApi.Feature.FEATURE_SHOW_LOCATION to "my_location_enabled",
        MasApi.Feature.FEATURE_CHANGE_PIN to "change_pin_enabled",
        MasApi.Feature.FEATURE_UPDATE_PROFILE to "update_profile_enabled",
        MasApi.Feature.FEATURE_ALLOW_EDITING_PROFILE_NAME_FIELDS to "allow_editing_profile_name_fields",

        // Unused / reserved
        MasApi.Feature.FEATURE_SALES_PROFIT to "sales_profit_enabled",
        MasApi.Feature.FEATURE_CREDIT_PURCHASED to "credit_purchased_enabled",
    )

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.i(_tag, "Initialized")
    }

    /**
     * Sync feature toggles from the getActiveFeatures API response.
     *
     * Logic: presence in the list = enabled (true), absence = disabled (false).
     * Mobile-only keys (not in backend) always use BuildConfig defaults.
     */
    fun syncFromFeatureList(activeFeatures: List<MasApi.Feature>) {
        val config = mutableMapOf<String, Boolean>()

        // All server-controlled keys start as false (absent = disabled)
        featureToKeyMap.values.forEach { config[it] = false }

        // Features present in the response are enabled
        activeFeatures.forEach { feature ->
            if (feature == MasApi.Feature.FEATURE_UNSPECIFIED) return@forEach
            featureToKeyMap[feature]?.let { key -> config[key] = true }
        }

        // Mobile-only keys — no backend equivalent, always use BuildConfig
        config["request_location_permission_enabled"] = BuildConfig.request_location_permission_enabled
        config["home_page_show_sales_target"] = BuildConfig.home_page_show_sales_target

        syncFromRemote(config)
    }

    /**
     * Load feature toggles into the store.
     *
     * @param config Map of toggle keys to boolean values.
     *               Pass null to load hardcoded defaults (fallback).
     */
    fun syncFromRemote(config: Map<String, Boolean>? = null) {
        val effective = config ?: getDefaults()
        prefs?.edit()?.apply {
            effective.forEach { (key, value) -> putBoolean(key, value) }
            apply()
        }
        Log.i(_tag, "Synced ${effective.size} feature toggles")
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return prefs?.getBoolean(key, default) ?: default
    }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
        Log.i(_tag, "Cleared")
    }

    /**
     * Default values matching current BuildConfig.
     * Used as fallback when the API call fails.
     */
    private fun getDefaults(): Map<String, Boolean> = mapOf(
        // Navigation
        "nav_bundle_page_enabled" to BuildConfig.nav_bundle_page_enabled,
        "nav_stats_page_enabled" to BuildConfig.nav_stats_page_enabled,
        "nav_transfer_page_enabled" to BuildConfig.nav_transfer_page_enabled,
        "nav_execu_stats_page_enabled" to BuildConfig.nav_execu_stats_page_enabled,
        "nav_buy_with_mobile_money_enabled" to BuildConfig.nav_buy_with_mobile_money_enabled,
        "nav_team_navigation_enabled" to BuildConfig.nav_team_navigation_enabled,
        "nav_mobile_money_deposit_enabled" to BuildConfig.nav_mobile_money_deposit_enabled,
        "nav_mobile_money_withdraw_enabled" to BuildConfig.nav_mobile_money_withdraw_enabled,

        // Teams
        "team_agents_location_view_enabled" to BuildConfig.team_agents_location_view_enabled,
        "team_page_show_weekly_bar_graph" to BuildConfig.team_page_show_weekly_bar_graph,

        // Login
        "request_location_permission_enabled" to BuildConfig.request_location_permission_enabled,

        // Home Dashboard
        "show_total_and_trade_bonus" to BuildConfig.show_total_and_trade_bonus,
        "home_page_show_weekly_bar_graph" to BuildConfig.home_page_show_weekly_bar_graph,
        "home_page_show_today_sales_breakdown" to BuildConfig.home_page_show_today_sales_breakdown,
        "home_page_show_sales_target" to BuildConfig.home_page_show_sales_target,

        // Agent Profile
        "my_location_enabled" to BuildConfig.my_location_enabled,
        "change_pin_enabled" to BuildConfig.change_pin_enabled,
        "update_profile_enabled" to BuildConfig.update_profile_enabled,
        "allow_editing_profile_name_fields" to BuildConfig.allow_editing_profile_name_fields,

        // Stats
        "sales_profit_enabled" to BuildConfig.sales_profit_enabled,
        "credit_purchased_enabled" to BuildConfig.credit_purchased_enabled,
    )
}
