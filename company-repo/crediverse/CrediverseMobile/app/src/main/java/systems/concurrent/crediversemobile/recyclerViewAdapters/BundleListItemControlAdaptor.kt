package systems.concurrent.crediversemobile.recyclerViewAdapters

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.fragments.BundleLandingFragment
import systems.concurrent.crediversemobile.fragments.BundleRecipientFragment
import systems.concurrent.crediversemobile.models.BundleCategory
import systems.concurrent.crediversemobile.models.BundleModel
import systems.concurrent.crediversemobile.models.Method
import systems.concurrent.crediversemobile.repositories.BundleRepository
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.services.AppAnalyticsService.Companion.bundleSaleEntry
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.AccountBalancesViewModel

class BundleListItemControlAdaptor {
    // nestedItems can ONLY be ONE OF:
    //  listOf( ...Categories... )
    //  listOf( ...Bundles...)
    sealed class NestedItem {
        data class String(val value: kotlin.String) : NestedItem()
        data class BundleCategory(val value: systems.concurrent.crediversemobile.models.BundleCategory) :
            NestedItem()
    }

    data class BundleMenuItem(
        val menuName: String,
        val categories: List<BundleCategory>? = null,
        val bundleCodes: List<String>? = null
    ) {
        fun hasChildren() = (categories.orEmpty() + bundleCodes.orEmpty()).isNotEmpty()
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        private var bundleRepository: BundleRepository = BundleRepository()

        fun mutateNestedMenu(nestedItem: NestedItem, mutableMenu: MutableList<BundleMenuItem>) {
            if (nestedItem is NestedItem.BundleCategory) {
                val isEmpty =
                    (nestedItem.value.categories.orEmpty() + nestedItem.value.bundle_codes.orEmpty()).isEmpty()
                if (isEmpty) return

                mutableMenu.add(
                    BundleMenuItem(
                        nestedItem.value.name,
                        nestedItem.value.categories,
                        nestedItem.value.bundle_codes
                    )
                )
            } else if (nestedItem is NestedItem.String) {
                mutableMenu.add(BundleMenuItem(nestedItem.value))
            }
        }

        private var accountBalancesViewModel: AccountBalancesViewModel? = null

        private fun setupAccountBalancesViewModel(fragment: Fragment, context: Context) {
            if (accountBalancesViewModel == null) {
                val accountBalancesFactory =
                    InjectorUtils.provideAccountBalancesFactory(context)
                accountBalancesViewModel = ViewModelProvider(
                    fragment, accountBalancesFactory
                )[AccountBalancesViewModel::class.java]
            }
        }

        fun bundleSaleClickListener(
            activity: NavigationActivity, fragment: Fragment, context: Context,
            methodButton: MaterialButton, bundle: BundleModel, bundlePosition: Int
        ) {
            methodButton.setOnClickListener {
                setupAccountBalancesViewModel(fragment, context)
                Log.e(
                    _tag,
                    "Clicked Method Button, bundle position: $bundlePosition; Named: ${bundle.name}"
                )

                val recipient = BundleLandingFragment.getRecipient()

                val chargeAmount = bundle.methods.maxOf { it.charge.amount }

                AccountBalancesViewModel.resetCache()
                accountBalancesViewModel?.viewModelScope?.launch {
                    NavigationActivity.toggleProgressBar(activity, show = true)
                    accountBalancesViewModel?.getBalancesLiveDataOnce { balanceResult ->
                        NavigationActivity.toggleProgressBar(activity, show = false)
                        var balance: Double? = null
                        if (balanceResult.isSuccess) {
                            balance = balanceResult.getOrThrow().balance.toDoubleOrNull()
                        }

                        if (balance != null) {
                            if (chargeAmount > balance) {
                                AppAnalyticsService.addFailedActionEvent(
                                    bundleSaleEntry.withReason(
                                        MasRepository.ErrorMessages.INSUFFICIENT_FUNDS.toString()
                                    )
                                )

                                val formattedBalance = Formatter.formatCustomCurrency(balance)
                                val balanceInsufficient = fragment.getString(
                                    R.string.error_insufficient_funds_with_balance,
                                    formattedBalance,
                                )
                                fragment.activity?.let { activity ->
                                    Toaster.showCustomToast(
                                        activity, balanceInsufficient
                                    )
                                }
                                accountBalancesViewModel = null
                                return@getBalancesLiveDataOnce
                            }
                        }

                        val dialog = Dialog(
                            NavigationManager.getActivity() as Activity,
                            DialogType.CONFIRM,
                            Formatter.combine(activity.getString(R.string.sell), " ${bundle.name}"),
                            Formatter.fromHtml(
                                activity.getString(
                                    R.string.bundle_sale_confirm, bundle.name, recipient,
                                    Formatter.formatCustomCurrency(chargeAmount)
                                )
                            )
                        )
                        dialog.onConfirm {
                            Log.e(_tag, "OK Button Pushed")
                            sellBundle(
                                activity, context, bundle,
                                bundle.methods[0], recipient, chargeAmount
                            )
                        }
                        dialog.show()
                        accountBalancesViewModel = null
                    }
                }
            }
        }

        private fun sellBundle(
            activity: NavigationActivity, context: Context,
            bundle: BundleModel, method: Method, recipient: String, chargeAmount: Double
        ) {
            val bundleSaleData =
                BundleService.BundleSaleData(bundle.name, bundle.code, method.code, recipient)
            /**
             * WARNING --- the `method` above is the correct one ....
             *             DO NOT use the method inside the bundle, that is an array of methods, not the "clicked" one
             */

            NavigationActivity.toggleProgressBar(activity, true)

            @Suppress("DeferredResultUnused")
            CoroutineScope(Dispatchers.IO).async {
                bundleRepository.sellBundle(bundleSaleData, chargeAmount) { bundleResult ->
                    NavigationManager.runOnNavigationUIThread {
                        bundleResult.onSuccess {
                            Toaster.showCustomToast(
                                activity,
                                Formatter.fromHtml(
                                    activity.getString(
                                        R.string.sold_airtime,
                                        Formatter.bold(bundle.name),
                                        recipient,
                                    )
                                ), Toaster.Options().setType(ToasterType.SUCCESS)
                            )
                            BundleLandingFragment.setRecipient("")
                            BundleLandingFragment.switchSlide(BundleLandingFragment.BundleSlide.CHOOSE_RECIPIENT)
                            BundleRecipientFragment.updateModels()
                        }.onFailure {
                            val errorResource = BundleRepository.ErrorMessages.getResourceOrDefault(
                                it.message.toString(), R.string.internal_server_error
                            )
                            val finalString = context.getString(errorResource)

                            val invalidPinMessageReceived =
                                BundleRepository.ErrorMessages.valueOf(it.message.toString()) ==
                                        BundleRepository.ErrorMessages.PIN_INVALID

                            val dialogResource =
                                if (invalidPinMessageReceived) R.string.logout
                                else R.string.c_ok

                            val dialog = Dialog(
                                NavigationManager.getActivity() as Activity, DialogType.ERROR,
                                context.getString(R.string.sell_failure_title),
                                Formatter.combine(Formatter.normal(finalString)),
                                DialogOptions().setCancellable(false)
                                    .setConfirmBtnTextResource(dialogResource)
                            )

                            if (invalidPinMessageReceived) {
                                dialog.onConfirm { LogoutManager.forceLogout() }
                            }
                            dialog.show()
                        }
                        NavigationActivity.toggleProgressBar(activity, false)
                    }
                }
            }
        }
    }
}
