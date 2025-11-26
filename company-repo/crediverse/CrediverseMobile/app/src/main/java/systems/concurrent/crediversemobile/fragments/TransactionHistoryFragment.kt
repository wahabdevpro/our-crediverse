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
import systems.concurrent.crediversemobile.databinding.FragmentSaleTxHistoryBinding
import systems.concurrent.crediversemobile.models.TransactionModel
import systems.concurrent.crediversemobile.recyclerViewAdapters.TransactionHistoryItemAdapter
import systems.concurrent.crediversemobile.services.*
import systems.concurrent.crediversemobile.utils.*
import systems.concurrent.crediversemobile.view_models.TransactionHistoryViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [TransactionHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionHistoryFragment :
    ViewBindingFragment<FragmentSaleTxHistoryBinding>(FragmentSaleTxHistoryBinding::inflate) {

    override val thisPage: NavigationManager.Page? = null

    private val _tag = this::class.java.kotlin.simpleName

    private lateinit var viewModel: TransactionHistoryViewModel

    private var isFetching = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = InjectorUtils.provideTransactionHistoryFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[TransactionHistoryViewModel::class.java]

        viewModel.viewModelScope.launch {
            val layoutManager = LinearLayoutManager(context)
            val recyclerView = view.findViewById<RecyclerView>(R.id.sale_history_recycler_view)
            recyclerView.layoutManager = layoutManager

            /*
            val swipyLayout = view.findViewById<SwipyRefreshLayout>(R.id.tx_history_list)
            swipyLayout.setOnRefreshListener { direction ->
                Log.e(
                    _tag, "Refresh triggered at "
                            + if (direction == SwipyRefreshLayoutDirection.TOP) "top" else "bottom"
                )
                GlobalScope.launch {
                    delay(2000)
                    swipyLayout.isRefreshing = false
                }

            }
            */

            var page = 0
            getTransactionsPageOrEmpty(page) firstPage@{

                val transactionList = it.toMutableList()

                viewModel.viewModelScope.launch {
                    recyclerView.adapter =
                        TransactionHistoryItemAdapter(transactionList, requireContext())
                }

                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        viewModel.viewModelScope.launch {
                            val canScrollDown = recyclerView.canScrollVertically(1)
                            if (!canScrollDown) {
                                // This means that the user has reached the end of the RecyclerView
                                Log.e(_tag, "Requesting new data.....")
                                Log.e(_tag, "Page " + (page + 1))
                                if (!isFetching) {
                                    showProgressBar()
                                    isFetching = true

                                    getTransactionsPageOrEmpty(page + 1) nextPages@{ nextPage ->
                                        if (nextPage.isNotEmpty()) page++
                                        else {
                                            Log.e(_tag, "Reached the last page")
                                            isFetching = false
                                            hideProgressBar(showError = false)
                                            return@nextPages
                                        }

                                        // Helps prevent page load problems when the UI frame is unavailable
                                        recyclerView.post(Runnable {
                                            (recyclerView.adapter as TransactionHistoryItemAdapter).addPage(
                                                nextPage
                                            )
                                        })
                                        Log.e(
                                            _tag,
                                            "Size of all pages up to page $page:  ${transactionList.size}"
                                        )

                                        viewModel.viewModelScope.launch {
                                            // short wait time before user can ask to load another page
                                            delay(200)
                                            isFetching = false
                                        }
                                        hideProgressBar(showError = false)
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getTransactionsPageOrEmpty(
        pageNumber: Int,
        callback: (List<TransactionModel>) -> Unit
    ) {
        val transactionsPerPage = PAGE_SIZE
        viewModel.viewModelScope.launch {
            viewModel.getNextPage(transactionsPerPage, pageNumber) { txListResult ->
                txListResult.onSuccess { txList ->
                    if (txList.isEmpty()) {
                        viewModel.viewModelScope.launch {
                            bindingOrNull?.let { binding ->
                                if (pageNumber == 0) {
                                    binding.txListError.setTextColor(requireContext().getColor(R.color.warn))
                                    binding.txListError.text = getString(R.string.tx_list_empty)
                                    hideProgressBar(showError = true)
                                } else {
                                    Toaster.showCustomToast(
                                        NavigationManager.getActivity(),
                                        getString(R.string.no_more_transactions)
                                    )
                                    hideProgressBar(showError = false)
                                }
                            }
                        }
                    } else {
                        hideProgressBar(showError = false)
                    }
                    callback(txList)
                }.onFailure { csException ->
                    Log.e(_tag, "Exception...${csException.message}")
                    val message = csException.getStringFromResourceOrDefault(requireContext())

                    viewModel.viewModelScope.launch {
                        bindingOrNull?.let { binding ->
                            if (pageNumber == 0) {
                                binding.txListError.text = message
                                hideProgressBar(showError = true)
                            } else {
                                Dialog(
                                    NavigationManager.getActivity(),
                                    DialogType.ERROR, "", message
                                ).show()
                            }
                        }
                    }
                    // Empty list returned if there was a failure
                    callback(listOf())
                }
            }
        }
    }

    private fun showProgressBar() {
        useBinding { binding ->
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar(showError: Boolean) {
        viewModel.viewModelScope.launch {
            bindingOrNull?.let { binding ->
                binding.progressBar.visibility = View.GONE
                if (showError) {
                    binding.txListError.visibility = View.VISIBLE
                } else {
                    binding.txHistoryList.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 10

        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of fragment AirtimeSaleHistory.
         */
        @JvmStatic
        fun newInstance() = TransactionHistoryFragment().apply { arguments = Bundle() }
    }
}