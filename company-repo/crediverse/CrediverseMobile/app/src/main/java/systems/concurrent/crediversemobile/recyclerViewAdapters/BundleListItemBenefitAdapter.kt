package systems.concurrent.crediversemobile.recyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.models.Benefit
import java.math.RoundingMode

class BundleListItemBenefitAdapter(
    private val itemList: List<Benefit>,
    private val context: Context
) :
    RecyclerView.Adapter<BundleListItemBenefitAdapter.BundleListItemBenefitViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BundleListItemBenefitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bundle_list_item_benefit, parent, false)
        return BundleListItemBenefitViewHolder(view)
    }

    override fun onBindViewHolder(holder: BundleListItemBenefitViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount() = itemList.size

    inner class BundleListItemBenefitViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.benefit_name)
        private val value = itemView.findViewById<TextView>(R.id.benefit_value)
        private val icon = itemView.findViewById<TextView>(R.id.benefit_icon)

        private fun fromBytes(bytes: Long): String {
            val tb = 1024 * 1024 * 1024 * 1024L
            val gb = 1024 * 1024 * 1024L
            val mb = 1024 * 1024L
            val kb = 1024L
            // assume TB largest
            return when (bytes) {
                in gb..tb -> (bytes / gb).toBigDecimal().setScale(0, RoundingMode.UP)
                    .toString() + "GB"
                in mb..gb -> (bytes / mb).toBigDecimal().setScale(0, RoundingMode.UP)
                    .toString() + "MB"
                in kb..mb -> (bytes / kb).toBigDecimal().setScale(2, RoundingMode.UP)
                    .toString() + "KB"
                else -> bytes.toBigDecimal().setScale(2, RoundingMode.UP).toString() + "B"
            }
        }

        fun bind(item: Benefit) {
            // bind the data to the views in the child list item layout
            name.text = item.name
            value.text = item.value.toString()
            val validUnits = listOf("min", "bytes", "sms")
            if (validUnits.contains(item.units)) {
                icon.typeface = context.resources.getFont(R.font.fa_solid)
                when (item.units) {
                    "bytes" -> {
                        value.text = fromBytes(item.value)
                        if (item.code == "n_data")
                            icon.text = context.resources.getString(R.string.fa_moon)
                        else icon.text = context.resources.getString(R.string.fa_globe)
                    }
                    "min" -> icon.text = context.resources.getString(R.string.fa_phone)
                    "sms" -> icon.text = context.resources.getString(R.string.fa_message)
                    else -> icon.text = ""
                }
            }
        }
    }
}