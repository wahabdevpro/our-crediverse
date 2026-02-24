package systems.concurrent.crediversemobile.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import systems.concurrent.crediversemobile.BuildConfig

/**
 * Runtime feature toggle store backed by SharedPreferences.
 *
 * Replaces compile-time BuildConfig toggles with runtime-configurable values.
 * BuildConfig values serve as fallback defaults when no remote config is available.
 *
 * Usage:
 *  1. Call [initialize] once after successful login (before navigating to home)
 *  2. Call [syncFromRemote] to load feature config (defaults for now, API response in future)
 *  3. Read values via [getBoolean] with BuildConfig fallback
 *  4. Call [clear] on logout
 */
object FeatureConfigStore {
    private const val PREFS_NAME = "FEATURE_CONFIG_STORE"
    private val _tag = "FeatureConfigStore"

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        Log.i(_tag, "Initialized")
    }

    /**
     * Load feature toggles into the store.
     *
     * @param config Map of toggle keys to boolean values from the server.
     *               Pass null to load hardcoded defaults (for use until backend API is ready).
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
     * These serve as the baseline until the backend API provides real values.
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
