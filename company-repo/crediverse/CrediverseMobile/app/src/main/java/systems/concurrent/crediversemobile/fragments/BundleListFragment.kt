package systems.concurrent.crediversemobile.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.recyclerViewAdapters.BundleListItemAdapter
import systems.concurrent.crediversemobile.databinding.FragmentBundleListBinding
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.BundleViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [BundleListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BundleListFragment :
    ViewBindingFragment<FragmentBundleListBinding>(FragmentBundleListBinding::inflate) {

    override val thisPage: NavigationManager.Page? = null

    private val _tag = this::class.java.kotlin.simpleName

    private lateinit var bundleViewModel: BundleViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        val bundleViewModelFactory = InjectorUtils.provideBundleFactory()
        bundleViewModel =
            ViewModelProvider(this, bundleViewModelFactory)[BundleViewModel::class.java]

        BundleLandingFragment.getRecipientLiveData().observe(viewLifecycleOwner) { recipient ->
            useBinding { binding ->
                binding.recipientText.text = recipient
            }
        }

        bundleViewModel.viewModelScope.launch {
            val layoutManager = LinearLayoutManager(context)
            val recyclerView = view.findViewById<RecyclerView>(R.id.bundle_list_recycler_view)
            recyclerView.layoutManager = layoutManager
            // prevents the scroll action from cutting off at the padding
            recyclerView.clipToPadding = false

            toggleProgressBar(show = true)

            bundleViewModel.getBundlesLiveData()
                .observe(viewLifecycleOwner) { resultBundleList ->
                    toggleProgressBar(show = false)
                    resultBundleList.onSuccess { bundleList ->
                        useBinding { it.problemText.visibility = View.GONE }

                        val recipient = BundleLandingFragment.getRecipient()
                        useBinding { binding ->
                            binding.recipientText.text = Formatter.fromHtml(
                                getString(R.string.bundle_sale_recipient, recipient)
                            )
                        }
                        recyclerView.adapter = BundleListItemAdapter(
                            bundleList, this@BundleListFragment,
                            requireActivity() as NavigationActivity
                        )
                    }.onFailure {
                        val message = it.getStringFromResourceOrDefault(requireContext())
                        useBinding { binding ->
                            binding.problemText.text = message
                            binding.problemText.visibility = View.VISIBLE
                        }

                        Log.e(_tag, "${it.message.toString()} -- responding with '$message'")
                    }
                }
        }
    }

    private fun setupListeners() {
        useBinding { binding ->
            binding.backButton.setOnClickListener {
                BundleLandingFragment.switchSlide(BundleLandingFragment.BundleSlide.CHOOSE_RECIPIENT)
            }
        }
    }

    private fun toggleProgressBar(show: Boolean) {
        useBinding { binding ->
            if (show) {
                binding.bundleListRecyclerView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.bundleListRecyclerView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of fragment AirtimeSaleHistory.
         */
        @JvmStatic
        fun newInstance() = BundleListFragment().apply { arguments = Bundle() }
    }
}