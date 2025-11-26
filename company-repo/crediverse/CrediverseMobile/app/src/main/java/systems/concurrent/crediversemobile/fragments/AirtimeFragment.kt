package systems.concurrent.crediversemobile.fragments

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.databinding.FragmentAirtimeBinding
import systems.concurrent.crediversemobile.repositories.MasRepository
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.*

/**
 * A simple [Fragment] subclass.
 * Use the [AirtimeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AirtimeFragment :
    ViewBindingFragment<FragmentAirtimeBinding>(FragmentAirtimeBinding::inflate) {

    override val thisPage by lazy { NavigationManager.Page.AIRTIME }

    private val _tag = this::class.java.kotlin.simpleName
    private lateinit var accountBalancesViewModel: AccountBalancesViewModel
    private lateinit var transactionHistoryViewModel: TransactionHistoryViewModel
    private lateinit var transactViewModel: TransactViewModel

    private val fromHistoryViewModel = FromHistoryViewModel()

    private lateinit var masRepository: MasRepository

    private var watchingInputs = false

    private var amountInput: TextInputEditText? = null
    private var recipientInput: TextInputEditText? = null

    private var resumedViaRepeatSale = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        masRepository = MasRepository(requireContext())

        fromHistoryViewModel.getCachedActionOnce()?.let {
            useBinding { binding ->
                if (!watchingInputs) sellButtonWatchInputs()
                binding.amountInput.setText(it.amount)
                binding.recipientInput.setText(it.recipient)
                resumedViaRepeatSale = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        useBinding { binding ->
            binding.amountInput.error = null
            if (resumedViaRepeatSale) {
                resumedViaRepeatSale = false
                return@useBinding
            }
            binding.recipientInput.setText("")
            binding.amountInput.setText("")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val airtimeViewModelFactory =
            InjectorUtils.provideTransactionHistoryFactory(requireContext())
        transactionHistoryViewModel = ViewModelProvider(
            this, airtimeViewModelFactory
        )[TransactionHistoryViewModel::class.java]

        val accountBalancesFactory =
            InjectorUtils.provideAccountBalancesFactory(requireContext())
        accountBalancesViewModel =
            ViewModelProvider(this, accountBalancesFactory)[AccountBalancesViewModel::class.java]

        val transactFactory =
            InjectorUtils.provideMasTransactFactory(requireContext())
        transactViewModel =
            ViewModelProvider(this, transactFactory)[TransactViewModel::class.java]

        accountBalancesViewModel.viewModelScope.launch {
            if (!watchingInputs) sellButtonWatchInputs()
            setupListeners()
        }

        useBinding { binding ->
            binding.progressBar.visibility = View.INVISIBLE
            binding.sellContent.visibility = View.VISIBLE

            binding.recipientInput.setText("")
            binding.amountInput.setText("")

            binding.sellFromHistoryButton.setOnClickListener {
                NavigationManager.setPage(NavigationManager.Page.HISTORY)
            }
        }
    }

    private fun setupListeners() {
        useBinding { binding ->
            binding.sellButton.setOnClickListener { _ ->
                activity?.let { ActivityUtils.hideKeyboard(it) }
                AccountBalancesViewModel.resetCache()
                accountBalancesViewModel.viewModelScope.launch {
                    NavigationActivity.toggleProgressBar(
                        requireActivity() as NavigationActivity, show = true
                    )
                    accountBalancesViewModel.getBalancesLiveDataOnce { balanceResult ->
                        NavigationActivity.toggleProgressBar(
                            requireActivity() as NavigationActivity, show = false
                        )
                        var balance: Double? = null
                        if (balanceResult.isSuccess) {
                            balance = balanceResult.getOrThrow().balance.toDoubleOrNull()
                        }

                        val amountEntered =
                            binding.amountInput.text.toString().toDoubleOrNull() ?: 0.0

                        if (balance != null && amountEntered > balance) {
                            val formattedBalance = Formatter.formatCustomCurrency(balance)
                            val balanceInsufficient = getString(
                                R.string.error_insufficient_funds_with_balance,
                                formattedBalance,
                            )
                            binding.amountInput.error =
                                Formatter.fromHtml("<font color='white'>$balanceInsufficient</font>")
                            binding.amountInput.requestFocus()
                            return@getBalancesLiveDataOnce
                        }

                        val amount = Formatter.formatCustomCurrency(
                            binding.amountInput.text.toString().toDoubleOrNull() ?: 0.0
                        )
                        val recipient = binding.recipientInput.text.toString()
                        val dialogMessage = getString(R.string.sell_confirm, amount, recipient)

                        val dialog = Dialog(
                            activity as Activity, DialogType.CONFIRM,
                            getString(R.string.please_confirm),
                            Formatter.fromHtml(dialogMessage)
                        )
                        dialog.onConfirm {
                            sellAirtime()
                        }
                        dialog.show()
                    }
                }
            }
        }
    }

    private fun sellAirtime() {
        useBinding { binding ->
            val recipient = binding.recipientInput.text.toString()
            val amount = binding.amountInput.text.toString().toDoubleOrNull()

            if (amount == null || amount == 0.0) {
                Dialog(
                    activity as Activity, DialogType.WARN, "",
                    getString(R.string.invalid_amount_dialog)
                ).show()
                return@useBinding
            }

            NavigationActivity.toggleProgressBar(requireActivity() as NavigationActivity, true)
            ActivityUtils.hideKeyboard(requireActivity())

            // THE ONLY TIME WE DON'T USE A VIEW MODEL FACTORY.
            //  Because the Sell action does not store information.
            //  It does however trigger updates to ::: Balances and Account History
            transactViewModel.sellAirtime(amount, recipient) { sellAirtimeResult ->
                sellAirtimeResult.onSuccess {
                    if (it.followUpRequired) {
                        NavigationManager.runOnNavigationUIThread {
                            Dialog(
                                activity as Activity, DialogType.WARN,
                                getString(R.string.sell_failure_title),
                                getString(R.string.error_follow_up)
                            ).show()
                        }
                    } else {
                        Toaster.showCustomToast(
                            NavigationManager.getActivity(),
                            Formatter.fromHtml(
                                getString(
                                    R.string.sold_airtime,
                                    Formatter.formatCustomCurrency(amount), recipient,
                                )
                            ), Toaster.Options().setType(ToasterType.SUCCESS)
                        )
                    }

                    // because the balance is retrieved on this page, we must reset the cached value
                    // Then, it's either a page refresh, or update it ourselves to trigger the mutable livedata change
                    // in our case, preferable to make only the call to get balance -- rather than refresh the whole page
                    ViewModelUtils.resetAllViewModelCaches()
                    accountBalancesViewModel.updateBalancesLiveData()
                    useBinding {
                        binding.recipientInput.setText("")
                        binding.amountInput.setText("")
                        binding.amountInput.error = null
                    }
                }.onFailure {
                    val errorResource = MasRepository.ErrorMessages.getResourceOrDefault(it)
                    val finalString =
                        requireContext().resources?.getString(errorResource).toString()
                    Dialog(
                        activity as Activity, DialogType.ERROR,
                        getString(R.string.sell_failure_title), finalString
                    ).show()
                }
                NavigationActivity.toggleProgressBar(
                    requireActivity() as NavigationActivity,
                    false
                )

                useBinding {
                    /**
                     * Stop text change listeners... causes crash because we are running in a coroutine scope
                     *  Enable listeners again after we are done clearing the values
                     */
                    amountInput!!.removeTextChangedListener(toggleSellCallback)
                    amountInput!!.setText("")
                    amountInput!!.error = null
                    amountInput!!.addTextChangedListener(toggleSellCallback)

                    recipientInput!!.removeTextChangedListener(toggleSellCallback)
                    recipientInput!!.setText("")
                    recipientInput!!.addTextChangedListener(toggleSellCallback)
                }
            }
        }
    }

    private val toggleSellCallback = object : TextWatcher {
        // Required Overrides
        override fun beforeTextChanged(
            s: CharSequence?, start: Int, count: Int, after: Int
        ) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            useBinding { binding ->
                val hasRecipient = binding.recipientInput.text?.isNotEmpty()
                val hasAmount = binding.amountInput.text?.isNotEmpty()
                binding.sellButton.isEnabled = hasRecipient == true && hasAmount == true
            }
        }
    }

    private fun sellButtonWatchInputs() {
        watchingInputs = true
        useBinding { binding ->
            val hasRecipient = binding.recipientInput.text?.isNotEmpty()
            val hasAmount = binding.amountInput.text?.isNotEmpty()
            binding.sellButton.isEnabled = hasRecipient == true && hasAmount == true

            amountInput = amountInput ?: binding.amountInput
            recipientInput = recipientInput ?: binding.recipientInput

            amountInput!!.addTextChangedListener(toggleSellCallback)
            recipientInput!!.addTextChangedListener(toggleSellCallback)
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of this fragment
         * @return A new instance of fragment Sell.
         */
        @JvmStatic
        fun newInstance() = AirtimeFragment().apply { arguments = Bundle() }
    }
}
