package systems.concurrent.crediversemobile.fragments

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentBundleRecipientBinding
import systems.concurrent.crediversemobile.repositories.BundleRepository
import systems.concurrent.crediversemobile.services.Dialog
import systems.concurrent.crediversemobile.services.DialogType
import systems.concurrent.crediversemobile.services.InjectorUtils
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.*
import java.net.ConnectException

/**
 * A simple [Fragment] subclass.
 * Use the [BundleRecipientFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BundleRecipientFragment :
    ViewBindingFragment<FragmentBundleRecipientBinding>(FragmentBundleRecipientBinding::inflate) {

    override val thisPage: NavigationManager.Page? = null

    private val _tag = this::class.java.kotlin.simpleName

    private val fromHistoryViewModel = FromHistoryViewModel()

    private var isSlideNavigationRequest = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fromHistoryViewModel.getCachedActionOnce()?.let {
            BundleLandingFragment.setRecipient(it.recipient)
        }
    }

    private fun initialiseFactories() {
        val balancesFactory = InjectorUtils.provideAccountBalancesFactory(requireContext())
        val txFactory = InjectorUtils.provideTransactionHistoryFactory(requireContext())
        val bundleFactory = InjectorUtils.provideBundleFactory()

        accountBalancesViewModel =
            ViewModelProvider(this, balancesFactory)[AccountBalancesViewModel::class.java]
        transactionHistoryViewModel =
            ViewModelProvider(this, txFactory)[TransactionHistoryViewModel::class.java]
        bundleViewModel =
            ViewModelProvider(this, bundleFactory)[BundleViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseFactories()

        useBinding { binding ->
            /**
             * FIXME --- this is a workaround. French "wraps" because the text is longer...
             *           But the button will not adjust it's size accordingly
             *           So we force it here ... REALLY poor workaround
             *           but can't find solutions for this at the moment
             */
            if (LocaleHelper.getCurrentLanguage(requireContext()) == LocaleHelper.Language.FR) {
                binding.chooseBundleButton.minHeight = 64
            }

            binding.bundleFromHistoryButton.setOnClickListener {
                NavigationManager.setPage(NavigationManager.Page.HISTORY)
            }

            binding.progressBar.visibility = View.INVISIBLE
            binding.bundleContent.visibility = View.VISIBLE
        }

        BundleLandingFragment.getRecipientLiveData().observe(viewLifecycleOwner) {
            useBinding { binding -> binding.recipientInput.setText(it) }
        }

        bundleViewModel.viewModelScope.launch {
            try {
                initializeBundlesUI()
            } catch (e: Exception) {
                Toast.makeText(activity?.applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
            bundleButtonWatchInputs()
            setupListeners()
        }
    }

    private fun initializeBundlesUI() {
        bundleViewModel.getBundlesLiveData()
            .observe(viewLifecycleOwner) { resultBundleList ->
                // we only care for these results HERE in the Recipient Fragment, if we have requested them
                if (isSlideNavigationRequest) {
                    resultBundleList.onSuccess {
                        BundleLandingFragment.switchSlide(BundleLandingFragment.BundleSlide.BUNDLE_LIST)
                        isSlideNavigationRequest = false
                    }.onFailure {
                        val message = it.getStringFromResourceOrDefault(requireContext())

                        Dialog(
                            activity as Activity, DialogType.WARN,
                            getString(R.string.bundle_list), message
                        ).show()

                        Log.e(_tag, "${it.message.toString()} -- responding with '$message'")
                    }
                    toggleProgressBar(show = false)
                }
            }
    }

    private fun bundleButtonWatchInputs() {
        useBinding { binding ->
            var hasRecipient = binding.recipientInput.text?.isNotEmpty()
            binding.chooseBundleButton.isEnabled = hasRecipient == true

            val toggleChooseCallback = object : TextWatcher {
                // don't need these - but the OVERRIDE is required by TextWatcher
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    hasRecipient = binding.recipientInput.text?.isNotEmpty()
                    binding.chooseBundleButton.isEnabled = hasRecipient == true
                }
            }

            binding.recipientInput.addTextChangedListener(toggleChooseCallback)
        }
    }

    private fun setupListeners() {
        useBinding { binding ->
            binding.chooseBundleButton.setOnClickListener {
                val recipient = binding.recipientInput.text.toString()

                ActivityUtils.hideKeyboard(requireActivity(), requireView())
                // TODO -- find another way to wait for the KB to close, rather than holding up the thread (sync option?)
                Thread.sleep(50)

                BundleLandingFragment.setRecipient(recipient)
                toggleProgressBar(show = true)

                @Suppress("DeferredResultUnused")
                CoroutineScope(Dispatchers.IO).async {
                    isSlideNavigationRequest = true
                    bundleViewModel.updateBundlesListLiveData(recipient) {
                        bundleViewModel.viewModelScope.launch {
                            toggleProgressBar(show = false)
                        }
                    }
                }
            }
        }
    }

    private fun toggleProgressBar(show: Boolean) {
        requireActivity().runOnUiThread {
            useBinding { binding ->
                if (show) {
                    ActivityUtils.hideKeyboard(requireActivity(), requireView())
                    binding.fadedBackground.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.fadedBackground.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of fragment AirtimeSaleHistory.
         */
        @JvmStatic
        fun newInstance() = BundleRecipientFragment().apply { arguments = Bundle() }

        private lateinit var accountBalancesViewModel: AccountBalancesViewModel
        private lateinit var transactionHistoryViewModel: TransactionHistoryViewModel
        private lateinit var bundleViewModel: BundleViewModel

        fun updateModels() {
            ViewModelUtils.resetAllViewModelCaches()
            accountBalancesViewModel.updateBalancesLiveData()
        }
    }
}
