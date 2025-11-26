package systems.concurrent.crediversemobile.view_models

class ViewModelUtils {
    companion object {
        /**
         * This FIX request is here in case new models are added, then you would need to add the "cache reset" here.
         * You should not remove this FIXME - it should serve as a reminder to others to check this code before build/commit/release/ etc
         */
        fun resetAllViewModelCaches() {
            TransactionHistoryViewModel.resetCache()
            AccountInfoViewModel.resetCache()
            AccountBalancesViewModel.resetCache()
        }
    }
}