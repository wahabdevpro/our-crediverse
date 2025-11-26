package systems.concurrent.crediversemobile.recyclerViewAdapters

import android.annotation.SuppressLint
import systems.concurrent.crediversemobile.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import systems.concurrent.crediversemobile.models.CurrencyFormatParams
import systems.concurrent.crediversemobile.models.TeamMemberModel
import systems.concurrent.crediversemobile.utils.AppFlag
import systems.concurrent.crediversemobile.utils.Formatter

class TeamMemberAdapter(
    private val context: android.content.Context,
    private val data: List<TeamMemberModel>,
    private val onClickListener: ((TeamMemberModel) -> Unit)? = null
) :
    RecyclerView.Adapter<TeamMemberAdapter.ViewHolder>() {

    private val currencyFormatParams = CurrencyFormatParams(
        AppFlag.Currency.localeCode,
        AppFlag.Currency.decimalSeparator, AppFlag.Currency.groupSeparator,
        AppFlag.Currency.symbol, AppFlag.Currency.patternWithoutSymbol
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.team_member_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon = itemView.findViewById<TextView>(R.id.team_member_icon)
        private val description = itemView.findViewById<TextView>(R.id.team_member_description)
        private val msisdn = itemView.findViewById<TextView>(R.id.team_member_msisdn)
        private val amount = itemView.findViewById<TextView>(R.id.team_member_amount)

        @SuppressLint("SetTextI18n")
        fun bind(item: TeamMemberModel) {
            icon.visibility = View.GONE
            description.text = "${item.firstName} ${item.surname}"
            msisdn.text = item.msisdn

            val stockAmount = item.stockBalance.balance.toDoubleOrNull()

            amount.text =
                if (stockAmount == null) context.getString(R.string.error_balance)
                else Formatter.formatCustomCurrency(stockAmount, currencyFormatParams)

            itemView.setOnClickListener { onClickListener?.invoke(item) }
        }
    }
}
