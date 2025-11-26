package systems.concurrent.crediversemobile.recyclerViewAdapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import systems.concurrent.crediversemobile.recyclerViewAdapters.BundleListItemControlAdaptor.BundleMenuItem
import systems.concurrent.crediversemobile.recyclerViewAdapters.BundleListItemControlAdaptor.NestedItem
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.models.*

class BundleListItemAdapter(
    private val categorisedBundles: SmartshopBundlesListGet,
    private val fragment: Fragment,
    private val activity: NavigationActivity,
) : RecyclerView.Adapter<BundleListItemAdapter.BundleListItemViewHolder>() {

    private val _tag = this::class.java.kotlin.simpleName
    private val bundleListItemViewHolder = mutableListOf<BundleListItemViewHolder>()

    private var bundleMenuItems = mutableListOf<BundleMenuItem>()

    init {
        categorisedBundles.categories.forEach { bundleCategory ->
            if (!bundleCategory.disallowed) {
                BundleListItemControlAdaptor.mutateNestedMenu(
                    NestedItem.BundleCategory(bundleCategory), bundleMenuItems
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BundleListItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.bundle_list_item, parent, false)

        return BundleListItemViewHolder(view, activity)
    }

    private fun buildNestedMenu(view: View, menuItem: BundleMenuItem) {
        val nestedListAdapter =
            BundleListItemNestedListAdapter(categorisedBundles, menuItem, fragment, activity)

        val nestedRecyclerView = view.findViewById<RecyclerView>(R.id.nested_items)

        nestedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = nestedListAdapter
        }
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: BundleListItemViewHolder, position: Int) {

        bundleListItemViewHolder.add(position, holder)

        val bundleMenuItem = bundleMenuItems[position]

        holder.bind(bundleMenuItem)

        holder.itemView.setOnLongClickListener {
            Log.e(_tag, "Long Clicked!")
            true
        }

        holder.itemView.setOnClickListener { view ->
            val nestedMenuWrapper = view.findViewById<FlexboxLayout>(R.id.nested_menu)
            if (nestedMenuWrapper.visibility == View.GONE) {
                closeAllNestedMenus()
                nestedMenuWrapper.visibility = View.VISIBLE
            } else nestedMenuWrapper.visibility = View.GONE

            buildNestedMenu(view, bundleMenuItem)

            Log.i(_tag, "Clicked on Bundle named: " + bundleMenuItem.menuName)
        }
    }

    private fun closeAllNestedMenus() {
        bundleListItemViewHolder.forEach {
            it.itemView.findViewById<FlexboxLayout>(R.id.nested_menu).visibility = View.GONE
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int = bundleMenuItems.size

    // Holds the views for adding it to image and text

    inner class BundleListItemViewHolder(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val categoryWrapper = itemView.findViewById<FlexboxLayout>(R.id.category_wrapper)
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)

        fun bind(item: BundleMenuItem) {
            // not containing child elements .... exit
            if (!item.hasChildren()) return

            categoryName.text = item.menuName
            categoryWrapper.visibility = View.VISIBLE
        }
    }
}
