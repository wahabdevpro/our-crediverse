package systems.concurrent.crediversemobile.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.databinding.FragmentMobileMoneyCashOutBinding
import systems.concurrent.crediversemobile.models.CurrencyFormatParams
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.repositories.SandboxRepository
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.MobileMoneyViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [MobileMoneyCashOutFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MobileMoneyCashOutFragment :
    ViewBindingFragment<FragmentMobileMoneyCashOutBinding>(FragmentMobileMoneyCashOutBinding::inflate) {

    private val _tag = this::class.java.kotlin.simpleName

    override val thisPage by lazy { NavigationManager.Page.MM_WITHDRAW }

    private lateinit var mobileMoneyViewModel: MobileMoneyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = InjectorUtils.provideMobileMoneyFactory(requireContext())
        mobileMoneyViewModel =
            ViewModelProvider(this, factory)[MobileMoneyViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        useBinding { binding ->
            binding.mmWithdrawBtn.setOnClickListener {
                requireActivity().runOnUiThread {
                    val withdrawAmount = binding.mmWithdrawAmountInput.text.toString()
                    val sourceMsisdn = binding.mmWithdrawSourceInput.text.toString()

                    val maxLimit = 5000

                    Validator.getDoubleNonZero(withdrawAmount, 0.0, maxLimit.toDouble())
                        .onValidationFailure {
                            binding.mmWithdrawAmountInput.error = when {
                                it.isAnyOf(Validator.Failure.ABOVE_MAX) -> {
                                    val limitErrorString = getString(
                                        R.string.mm_max_limit_error,
                                        Formatter.formatCustomCurrency(
                                            maxLimit.toDouble(),
                                            CurrencyFormatParams().withNoDecimalPlaces()
                                        )
                                    )
                                    Formatter.fromHtml("<font color='white' >$limitErrorString</font>")
                                }
                                else -> getString(R.string.invalid_amount_dialog)
                            }
                            binding.mmWithdrawAmountInput.requestFocus()
                        }.onSuccess { amount ->
                            val hasMobileMoneyLoginToken =
                                if (SandboxRepository.isSandboxEnabled) {
                                    // Only valid for sandbox mode - completely ignored otherwise
                                    // Using it here prevents "coupling" the sandbox and the MAS service
                                    CustomUtils.fiftyFiftyChance()
                                } else MasService.hasMobileMoneyToken()

                            if (!hasMobileMoneyLoginToken) {
                                MobileMoneyCashInFragment.openMobileMoneyLoginDialog(
                                    mobileMoneyViewModel,
                                    activity as Activity,
                                    onReady = {
                                        if (it.isSuccess) withdraw(amount, sourceMsisdn)
                                    })
                            } else {
                                Log.e(_tag, "let's attempt a withdraw, there exists an MM token")
                                withdraw(amount, sourceMsisdn)
                            }
                        }
                }
            }
        }
    }

    private fun withdraw(withdrawAmount: Double, sourceMsisdn: String) {
        val confirmAmount = Formatter.formatCustomCurrency(withdrawAmount)
        val dialog = Dialog(
            requireActivity(), DialogType.CONFIRM, "",
            Formatter.fromHtml(getString(R.string.mm_withdraw_confirm, confirmAmount, sourceMsisdn))
        )
        dialog.onConfirm {
            NavigationActivity.toggleProgressBar(
                requireActivity() as NavigationActivity,
                show = true,
                Formatter.fromHtml(
                    getString(R.string.mm_withdraw_processing_notification, sourceMsisdn)
                ), Formatter.fromHtml(
                    getString(R.string.mm_progress_notification, "60"),
                )
            )
            mobileMoneyViewModel.mobileMoneyWithdraw(withdrawAmount, sourceMsisdn) { result ->
                activity?.let {
                    NavigationActivity.toggleProgressBar(
                        requireActivity() as NavigationActivity, false
                    )
                    result
                        .onSuccess {
                            Toaster.showCustomToast(
                                NavigationManager.getActivity(),
                                Formatter.fromHtml(
                                    getString(
                                        R.string.mm_withdraw_success,
                                        Formatter.formatCustomCurrency(withdrawAmount),
                                        sourceMsisdn,
                                    )
                                ), Toaster.Options().setType(ToasterType.SUCCESS)
                            )
                            useBinding { binding ->
                                binding.mmWithdrawAmountInput.setText("")
                                binding.mmWithdrawSourceInput.setText("")
                            }
                        }.onFailure {
                            val finalString = it.getStringFromResourceOrDefault(requireContext())

                            if (it.isAnyOf(MasRepository.ErrorMessages.AUTH)) {
                                dialog.dismiss()
                                Toaster.showCustomToast(
                                    NavigationManager.getActivity(),
                                    getString(R.string.unable_to_authenticate),
                                    Toaster.Options().setType(ToasterType.WARN)
                                )
                                MobileMoneyCashInFragment.openMobileMoneyLoginDialog(
                                    mobileMoneyViewModel,
                                    activity as Activity,
                                    onReady = { loginResult ->
                                        if (loginResult.isSuccess) withdraw(
                                            withdrawAmount,
                                            sourceMsisdn
                                        )
                                    })
                            } else {
                                Dialog(
                                    NavigationManager.getActivity(), DialogType.ERROR, "",
                                    finalString
                                ).show()
                            }
                        }
                }
            }
        }
        dialog.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment
         * @return A new instance of fragment Sell.
         */
        @JvmStatic
        fun newInstance() = MobileMoneyCashOutFragment().apply { arguments = Bundle() }
    }
}
