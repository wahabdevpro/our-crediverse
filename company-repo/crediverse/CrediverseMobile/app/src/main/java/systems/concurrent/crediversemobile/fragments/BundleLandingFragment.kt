package systems.concurrent.crediversemobile.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.databinding.FragmentBundleLandingBinding
import systems.concurrent.crediversemobile.recyclerViewAdapters.PagerAdapter
import systems.concurrent.crediversemobile.utils.NavigationManager
import systems.concurrent.crediversemobile.utils.ViewBindingFragment
import systems.concurrent.crediversemobile.view_models.TransactionHistoryViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [BundleLandingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BundleLandingFragment :
    ViewBindingFragment<FragmentBundleLandingBinding>(FragmentBundleLandingBinding::inflate) {

    override val thisPage by lazy { NavigationManager.Page.BUNDLE }

    private lateinit var viewModel: TransactionHistoryViewModel

    enum class BundleSlide(private val pageNumber: Int) {
        CHOOSE_RECIPIENT(0),
        BUNDLE_LIST(1);

        val pageId get() = pageNumber
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBundleLandingBinding.inflate(inflater, container, false)

        val adapter = PagerAdapter(this)

        adapter.addFragment(
            BundleRecipientFragment.newInstance(),
            getString(R.string.bundle_choose_recipient)
        )
        adapter.addFragment(
            BundleListFragment.newInstance(),
            getString(R.string.choose_bundle)
        )

        //adapter.addFragment(BundleListFragment.newInstance(), getString(R.string.bundle_list))

        binding.txTabLayout.visibility = View.GONE

        viewSlider = binding.bundleViewPager
        viewSlider!!.isSaveEnabled = false;
        viewSlider!!.adapter = adapter
        switchSlide(BundleSlide.CHOOSE_RECIPIENT)
        viewSlider!!.isUserInputEnabled = false

        val tabs: TabLayout = binding.txTabLayout
        TabLayoutMediator(tabs, viewSlider!!) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()

        return binding.root
    }

    companion object {
        private val _tag = this::class.java.kotlin.simpleName

        private var viewSlider: ViewPager2? = null
        private var recipient: MutableLiveData<String> = MutableLiveData("")

        fun switchSlide(slide: BundleSlide) {
            viewSlider?.currentItem = slide.pageId
        }

        fun setRecipient(recipient: String) {
            this.recipient.value = recipient
        }

        fun getRecipientLiveData(): LiveData<String> {
            return recipient
        }

        fun getRecipient(): String {
            return recipient.value ?: ""
        }

        /**
         * Use this factory method to create a new instance of this fragment.
         * @return A new instance of fragment BundleFragment.
         */
        @JvmStatic
        fun newInstance() = BundleLandingFragment().apply { arguments = Bundle() }
    }
}