package systems.concurrent.crediversemobile.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.databinding.FragmentMobileMoneyCashInBinding
import systems.concurrent.crediversemobile.models.CurrencyFormatParams
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.repositories.SandboxRepository
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.MobileMoneyViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [MobileMoneyCashInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MobileMoneyCashInFragment :
    ViewBindingFragment<FragmentMobileMoneyCashInBinding>(FragmentMobileMoneyCashInBinding::inflate) {

    private val _tag = this::class.java.kotlin.simpleName

    override val thisPage by lazy { NavigationManager.Page.MM_DEPOSIT }

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
            binding.mmDepositBtn.setOnClickListener {
                requireActivity().runOnUiThread {
                    val depositAmount = binding.mmAmountInput.text.toString()
                    val recipient = binding.mmRecipientInput.text.toString()

                    val maxLimit = 5000

                    Validator.getDoubleNonZero(depositAmount, 0.0, maxLimit.toDouble())
                        .onValidationFailure {
                            binding.mmAmountInput.error = when {
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
                            binding.mmAmountInput.requestFocus()
                        }.onSuccess { amount ->
                            val hasMobileMoneyLoginToken =
                                if (SandboxRepository.isSandboxEnabled) {
                                    // Only valid for sandbox mode - completely ignored otherwise
                                    // Using it here prevents "coupling" the sandbox and the MAS service
                                    CustomUtils.fiftyFiftyChance()
                                } else MasService.hasMobileMoneyToken()

                            if (!hasMobileMoneyLoginToken) {
                                openMobileMoneyLoginDialog(mobileMoneyViewModel,
                                    activity as Activity,
                                    onReady = {
                                        if (it.isSuccess) deposit(amount, recipient)
                                    })
                            } else {
                                deposit(amount, recipient)
                                Log.e(_tag, "let's attempt a deposit, there exists an MM token")
                            }
                        }
                }
            }
        }
    }

    private fun deposit(depositAmount: Double, recipient: String) {
        val confirmAmount = Formatter.formatCustomCurrency(depositAmount)
        val dialog = Dialog(
            requireActivity(), DialogType.CONFIRM, "",
            Formatter.fromHtml(getString(R.string.mm_deposit_confirm, confirmAmount, recipient))
        )
        dialog.onConfirm {
            NavigationActivity.toggleProgressBar(
                requireActivity() as NavigationActivity,
                show = true, progressTextTop = null, Formatter.fromHtml(
                    getString(R.string.mm_deposit_processing_notification, recipient)
                )
            )
            mobileMoneyViewModel.mobileMoneyDeposit(depositAmount, recipient) { result ->
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
                                        R.string.mm_deposit_success,
                                        Formatter.formatCustomCurrency(depositAmount),
                                        recipient,
                                    )
                                ), Toaster.Options().setType(ToasterType.SUCCESS)
                            )
                            useBinding { binding ->
                                binding.mmAmountInput.setText("")
                                binding.mmRecipientInput.setText("")
                            }
                        }.onFailure {
                            val finalString = it.getStringFromResourceOrDefault(requireContext())

                            if (it.isAnyOf(MasRepository.ErrorMessages.MM_UNAUTHORIZED)) {
                                dialog.dismiss()
                                Toaster.showCustomToast(
                                    NavigationManager.getActivity(),
                                    getString(R.string.unable_to_authenticate),
                                    Toaster.Options().setType(ToasterType.WARN)
                                )
                                openMobileMoneyLoginDialog(mobileMoneyViewModel,
                                    activity as Activity,
                                    onReady = { loginResult ->
                                        if (loginResult.isSuccess) deposit(depositAmount, recipient)
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
        fun openMobileMoneyLoginDialog(
            mobileMoneyViewModel: MobileMoneyViewModel, activity: Activity,
            onReady: (CSResult<Unit>) -> Unit
        ) {
            val dialog = LoginDialog(activity, activity.getString(R.string.mobile_money_login))
            dialog.onLoginGetCredentials { username, password ->
                if (username.isEmpty() || password.isEmpty()) {
                    val errMsg = activity.getString(R.string.cannot_be_empty)
                    when {
                        username.isEmpty() -> dialog.showUsernameInputError(errMsg)
                        password.isEmpty() -> dialog.showPasswordInputError(errMsg)
                    }
                    return@onLoginGetCredentials
                }

                dialog.toggleInputProgress(show = true)
                mobileMoneyViewModel.mobileMoneyLogin(username, password) { loginResult ->
                    dialog.toggleInputProgress(show = false)
                    loginResult
                        .onSuccess {
                            dialog.dismiss()
                        }.onFailure {
                            Dialog(
                                activity, DialogType.ERROR, "",
                                it.getStringFromResourceOrDefault(activity.applicationContext)
                            ).show()
                        }
                    onReady(loginResult)
                }
            }.show()
        }

        /**
         * Use this factory method to create a new instance of this fragment
         * @return A new instance of fragment Sell.
         */
        @JvmStatic
        fun newInstance() = MobileMoneyCashInFragment().apply { arguments = Bundle() }
    }
}
