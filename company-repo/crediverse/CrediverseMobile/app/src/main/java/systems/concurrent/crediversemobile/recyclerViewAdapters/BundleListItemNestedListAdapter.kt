package systems.concurrent.crediversemobile.recyclerViewAdapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.activities.NavigationActivity
import systems.concurrent.crediversemobile.models.SmartshopBundlesListGet
import systems.concurrent.crediversemobile.recyclerViewAdapters.BundleListItemControlAdaptor.BundleMenuItem
import systems.concurrent.crediversemobile.recyclerViewAdapters.BundleListItemControlAdaptor.Companion.bundleSaleClickListener
import systems.concurrent.crediversemobile.recyclerViewAdapters.BundleListItemControlAdaptor.NestedItem
import systems.concurrent.crediversemobile.utils.Formatter

class BundleListItemNestedListAdapter(
    private val categorisedBundles: SmartshopBundlesListGet,
    nestedMenu: BundleMenuItem, // One of BundleCategory || BundleModel
    private val fragment: Fragment,
    private val activity: NavigationActivity,
) : RecyclerView.Adapter<BundleListItemNestedListAdapter.BundleListItemNestViewHolder>() {

    private val _tag = this::class.java.kotlin.simpleName

    private val bundleListNestedListViewHolders =
        mutableListOf<BundleListItemNestedListAdapter.BundleListItemNestViewHolder>()

    private var nestedBundleMenuItems = mutableListOf<BundleMenuItem>()

    init {
        nestedMenu.categories?.forEach { category ->
            if (!category.disallowed) {
                BundleListItemControlAdaptor.mutateNestedMenu(
                    NestedItem.BundleCategory(category), nestedBundleMenuItems
                )
            }
        }
        nestedMenu.bundleCodes?.forEach { bundleCode ->
            BundleListItemControlAdaptor.mutateNestedMenu(
                NestedItem.String(bundleCode), nestedBundleMenuItems
            )
        }
        Log.e(_tag, "not working")
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BundleListItemNestViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.bundle_list_item, parent, false)
        return BundleListItemNestViewHolder(view)
    }

    private fun buildNestedMenu(view: View, menuItem: BundleMenuItem) {
        val nestedListAdapter = BundleListItemNestedListAdapter(
            categorisedBundles, menuItem, fragment, activity
        )

        val nestedRecyclerView = view.findViewById<RecyclerView>(R.id.nested_items)

        nestedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = nestedListAdapter
        }
    }

    override fun onBindViewHolder(holder: BundleListItemNestViewHolder, position: Int) {
        bundleListNestedListViewHolders.add(position, holder)

        if (nestedBundleMenuItems.size <= position) return

        val bundleMenuItem = nestedBundleMenuItems[position]

        holder.bind(categorisedBundles, nestedBundleMenuItems[position], position)

        holder.itemView.setOnClickListener { view ->
            val nestedMenuWrapper = view.findViewById<FlexboxLayout>(R.id.nested_menu)
            if (nestedMenuWrapper.visibility == View.GONE) {
                closeAllNestedMenus()
                nestedMenuWrapper.visibility = View.VISIBLE
            } else nestedMenuWrapper.visibility = View.GONE

            buildNestedMenu(view, bundleMenuItem)

            Log.e(_tag, "Clicked on ")
        }
    }

    private fun closeAllNestedMenus() {
        bundleListNestedListViewHolders.forEach {
            it.itemView.findViewById<FlexboxLayout>(R.id.nested_menu).visibility = View.GONE
        }
    }

    override fun getItemCount() = nestedBundleMenuItems.size

    inner class BundleListItemNestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val methodButton = itemView.findViewById<MaterialButton>(R.id.method_button)

        private val bundleWrapper = itemView.findViewById<ConstraintLayout>(R.id.bundle_wrapper)
        private val categoryWrapper = itemView.findViewById<FlexboxLayout>(R.id.category_wrapper)
        private val bundleName = itemView.findViewById<TextView>(R.id.bundle_name)
        private val bundleCost = itemView.findViewById<TextView>(R.id.bundle_cost)
        private val categoryName = itemView.findViewById<TextView>(R.id.category_name)

        // `item` is One of BundleCategory || BundleModel
        fun bind(
            categorisedBundles: SmartshopBundlesListGet,
            item: BundleMenuItem,
            bundlePosition: Int
        ) {
            // bind the data to the views in the child list item layout
            if (item.hasChildren()) {
                categoryName.text = item.menuName
                categoryWrapper.visibility = View.VISIBLE
                return
            }

            val bundleCode = item.menuName

            val bundleModel =
                categorisedBundles.bundles.find { it.code == bundleCode } ?: return
            bundleWrapper.visibility = View.VISIBLE
            bundleName.text = bundleModel.name

            @SuppressLint("SetTextI18n")
            bundleCost.text = Formatter.formatCustomCurrency(
                /**
                 * TODO --- We do not not sort/filter `methods`, there could be more than one... but we use the MAX to charge
                 *          This may come back to bite us, `methods` is an ARRAY. We must be aware of this!!!
                 */
                bundleModel.methods.maxOf { it.charge.amount }
            )

            bundleSaleClickListener(
                activity, fragment, activity, methodButton, bundleModel, bundlePosition
            )
        }
    }
}
