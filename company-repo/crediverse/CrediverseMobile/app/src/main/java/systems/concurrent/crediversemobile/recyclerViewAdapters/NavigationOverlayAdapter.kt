package systems.concurrent.crediversemobile.recyclerViewAdapters

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.utils.DrawableFont
import systems.concurrent.crediversemobile.utils.Icon
import systems.concurrent.crediversemobile.utils.NavigationManager

class NavigationOverlayAdapter(
    private val context: Context,
    private val overlayData: OverlayData,
    private val clickListener: (page: NavigationManager.Page) -> Unit
) : RecyclerView.Adapter<NavigationOverlayAdapter.ViewHolder>() {

    data class OverlayData(
        val navLabel: Int, val navIcon: Icon,
        val pages: List<NavigationManager.Page>,
        val specialPage: NavigationManager.Page? = null,
        val heading: String? = null
    )

    class ViewHolder(val context: Context, itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val wrapper: FlexboxLayout = itemView.findViewById(R.id.wrapper)

        private val label: TextView = itemView.findViewById(R.id.label)
        private val fab: FloatingActionButton = itemView.findViewById(R.id.fab)

        fun hideSpecial() {
            wrapper.visibility = View.GONE
        }

        fun bindSpecial(
            page: NavigationManager.Page,
            clickListener: (NavigationManager.Page) -> Unit,
        ) {
            // handled like normal pages - but only called if it exists
            bind(page, clickListener)
        }

        fun bind(
            page: NavigationManager.Page, clickListener: (NavigationManager.Page) -> Unit,
        ) {
            label.text = context.getString(page.navNameResource)
            val drawableFont = DrawableFont(context)
            fab.setImageDrawable(drawableFont.from(page.icon))
            fab.setOnClickListener {
                clickListener(page)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.template_navigation_overlay_fab, parent, false)
        return ViewHolder(context, itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            if (overlayData.specialPage == null) holder.hideSpecial()
            else holder.bindSpecial(overlayData.specialPage, clickListener)
            return
        }

        val item = overlayData.pages[position - 1]
        holder.bind(item, clickListener)
    }

    override fun getItemCount(): Int {
        // Pretend the number of pages includes the SPECIAL page ...
        //  If no special page, position 0 will be ignored
        return overlayData.pages.size + 1
    }
}
