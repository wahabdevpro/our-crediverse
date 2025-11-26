package systems.concurrent.crediversemobile.utils

import systems.concurrent.crediversemobile.App
import systems.concurrent.crediversemobile.R

enum class Icon(private val _unicode: String) {
    HOUSE(App.context.getString(R.string.fa_house)),
    RETRO_MOBILE(App.context.getString(R.string.fa_retro_mobile)),
    GIFT(App.context.getString(R.string.fa_gift)),
    QUESTION(App.context.getString(R.string.fa_question)),
    CHECK_DOUBLE(App.context.getString(R.string.fa_check_double)),
    X_MARK(App.context.getString(R.string.fa_x_mark)),
    EXCLAMATION(App.context.getString(R.string.fa_exclamation)),
    INFO(App.context.getString(R.string.fa_info)),
    ERASER(App.context.getString(R.string.fa_eraser)),
    ARROW_RIGHT_TO_BRACKET(App.context.getString(R.string.fa_arrow_right_to_bracket)),
    ARROW_UP_FROM_BRACKET(App.context.getString(R.string.fa_arrow_up_from_bracket)),
    ARROW_RIGHT_ARROW_LEFT(App.context.getString(R.string.fa_arrow_right_arrow_left)),
    CLICK_ROTATE_LEFT(App.context.getString(R.string.fa_clock_rotate_left)),
    ELLIPSES_VERTICAL(App.context.getString(R.string.fa_ellipsis_vertical)),
    FEEDBACK(App.context.getString(R.string.fa_comment)),
    FINGERPRINT(App.context.getString(R.string.fa_fingerprint)),
    USERS(App.context.getString(R.string.fa_users)),
    PERSON_CIRCLE_QUESTION(App.context.getString(R.string.fa_person_circle_question)),
    ID_CARD(App.context.getString(R.string.fa_id_card)),
    USER_TIE(App.context.getString(R.string.fa_user_tie)),
    USER_PEN(App.context.getString(R.string.fa_user_pen)),
    MAP_LOCATION_DOT(App.context.getString(R.string.fa_map_location_dot)),
    WALLET(App.context.getString(R.string.fa_wallet)),
    CHART_PIE(App.context.getString(R.string.fa_chart_pie)),
    CHEVRON_LEFT(App.context.getString(R.string.fa_chevron_left)),
    COINS(App.context.getString(R.string.fa_coins)),
    BULLSEYE(App.context.getString(R.string.fa_bullseye)),
    PENCIL(App.context.getString(R.string.fa_pencil)),
    CHART_COLUMN(App.context.getString(R.string.fa_chart_column));

    val unicode get() = this._unicode
}
