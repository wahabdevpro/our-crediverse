package systems.concurrent.crediversemobile.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentTransactionHistoryBinding
import systems.concurrent.crediversemobile.services.TXPagerAdapter
import systems.concurrent.crediversemobile.utils.NavigationManager
import systems.concurrent.crediversemobile.utils.ViewBindingFragment

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment :
    ViewBindingFragment<FragmentTransactionHistoryBinding>(FragmentTransactionHistoryBinding::inflate) {

    override val thisPage by lazy { NavigationManager.Page.HISTORY }

    private val _tag = this::class.java.kotlin.simpleName

    override fun onResume() {
        super.onResume()
        NavigationManager.resumePage(NavigationManager.Page.HISTORY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)

        val adapter = TXPagerAdapter(this)

        adapter.addFragment(
            TransactionHistoryFragment.newInstance(),
            getString(R.string.history_title)
        )

        binding.txTabLayout.visibility = View.GONE

        val viewPager: ViewPager2 = binding.txViewPager
        viewPager.isSaveEnabled = false;
        viewPager.adapter = adapter
        viewPager.currentItem = 0

        val tabs: TabLayout = binding.txTabLayout
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of fragment TransactionHistory.
         */
        @JvmStatic
        fun newInstance() = HistoryFragment().apply { arguments = Bundle() }
    }
}
