package systems.concurrent.crediversemobile.recyclerViewAdapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter<T : Fragment>(activity: T?) : FragmentStateAdapter(activity!!) {
    private var fragments: MutableList<Fragment> = ArrayList()
    private var fragmentTitles: MutableList<String> = ArrayList()

    fun getTabTitle(position: Int): String {
        return fragmentTitles[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        fragmentTitles.add(title)
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
