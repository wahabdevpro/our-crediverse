package systems.concurrent.crediversemobile.view_models

import androidx.lifecycle.ViewModel

class FromHistoryViewModel : ViewModel() {

    companion object {
        data class CachedAction(var recipient: String, var amount: String)

        private var cachedAction: CachedAction? = null

        fun setCachedActionData(recipient: String, amount: String) {
            cachedAction = CachedAction(recipient, amount)
        }
    }

    fun getCachedActionOnce(): CachedAction? {
        val cachedActionFinal = cachedAction
        cachedAction = null
        return cachedActionFinal
    }
}