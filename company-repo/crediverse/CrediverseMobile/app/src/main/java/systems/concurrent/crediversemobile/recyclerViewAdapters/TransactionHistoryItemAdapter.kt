package systems.concurrent.crediversemobile.recyclerViewAdapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.models.*
import systems.concurrent.crediversemobile.services.MasService
import systems.concurrent.crediversemobile.utils.FeatureToggle
import systems.concurrent.crediversemobile.utils.Formatter
import systems.concurrent.crediversemobile.utils.NavigationManager
import systems.concurrent.crediversemobile.view_models.FromHistoryViewModel
import kotlin.math.roundToInt

class TransactionHistoryItemAdapter(
    private val transactions: MutableList<TransactionModel>, private val context: Context
) : RecyclerView.Adapter<TransactionHistoryItemAdapter.TransactionHistoryItemViewHolder>() {

    private val _tag = this::class.java.kotlin.simpleName
    private val transactionItemViewHolders = mutableListOf<TransactionHistoryItemViewHolder>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TransactionHistoryItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.sale_history_item, parent, false)

        return TransactionHistoryItemViewHolder(view, context)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: TransactionHistoryItemViewHolder, position: Int) {
        transactionItemViewHolders.add(position, holder)

        val transaction = transactions[position]

        // sets the image to the imageview from our itemHolder class
        holder.bind(transaction)
        holder.itemView.setOnLongClickListener {
            Log.e(_tag, "Long Clicked!")
            true
        }

        val itemContainer = holder.itemView.findViewById<FlexboxLayout>(R.id.extra_tx_info)
        holder.itemView.setOnClickListener {
            if (itemContainer.visibility == View.GONE) {
                closeAllItemContainers()
                itemContainer.visibility = View.VISIBLE
            } else itemContainer.visibility = View.GONE

            Log.i(_tag, "Clicked on Transaction for Recipient: " + transaction.recipientMsisdn)
        }

        val repeatSaleButton = holder.itemView.findViewById<MaterialButton>(R.id.repeat_sale_button)
        repeatSaleButton.setOnClickListener {
            val transactionToRepeat = transactions[position]

            val amount = transactionToRepeat.amount.toDoubleOrNull()?.roundToInt()?.toString() ?: ""

            FromHistoryViewModel.setCachedActionData(transactionToRepeat.recipientMsisdn, amount)

            Log.e(_tag, "Clicked Button for position: $position")
            when (transactionToRepeat.type) {
                TXType.SELF_TOPUP -> NavigationManager.setPage(NavigationManager.Page.AIRTIME)
                TXType.SELL -> NavigationManager.setPage(NavigationManager.Page.AIRTIME)
                TXType.TRANSFER -> NavigationManager.setPage(NavigationManager.Page.TRANSFER)
                TXType.NON_AIRTIME_DEBIT -> NavigationManager.setPage(NavigationManager.Page.BUNDLE)
                else -> {}
            }
        }

    }

    private fun closeAllItemContainers() {
        transactionItemViewHolders.forEach {
            val itemContainer = it.itemView.findViewById<FlexboxLayout>(R.id.extra_tx_info)
            itemContainer.visibility = View.GONE
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int = transactions.size

    // append list to the existing one
    fun addPage(newList: List<TransactionModel>) {
        val position = transactions.size
        transactions.addAll(newList)
        notifyItemInserted(position)
    }
    // Holds the views for adding it to image and text

    class TransactionHistoryItemViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        // Fields
        private val affectedBParty = itemView.findViewById<TextView>(R.id.tx_b_party)
        private val amount = itemView.findViewById<TextView>(R.id.tx_amount)
        private val txType = itemView.findViewById<TextView>(R.id.tx_type)
        private val status = itemView.findViewById<TextView>(R.id.status_value)
        private val txReference = itemView.findViewById<TextView>(R.id.tx_reference)
        private val txStarted = itemView.findViewById<TextView>(R.id.tx_datestamp)

        private val boundIcon = itemView.findViewById<TextView>(R.id.tx_bound_icon)
        private val icon = itemView.findViewById<TextView>(R.id.tx_history_icon)
        private val repeatSaleButtonWrapper =
            itemView.findViewById<ConstraintLayout>(R.id.repeat_sale_button_wrapper)

        private val selfTopupIcon = context.getString(R.string.fa_wallet)
        private val adjustIcon = context.getString(R.string.fa_sliders)
        private val transferIcon = context.getString(R.string.fa_arrow_right_arrow_left)
        private val adjudicateIcon = context.getString(R.string.fa_scale_unbalanced)
        private val reversalIcon = context.getString(R.string.fa_backward)
        private val rewardIcon = context.getString(R.string.fa_hand_holding_dollar)
        private val airtimeSaleIcon = context.getString(R.string.fa_retro_mobile)
        private val bundleSaleIcon = context.getString(R.string.fa_gift)

        private val arrowRightIcon = context.getString(R.string.fa_arrow_right)
        private val arrowLeftIcon = context.getString(R.string.fa_arrow_left)

        // Key/Value holders
        private val itemDescriptionLineItem =
            itemView.findViewById<View>(R.id.item_description_line_item)
        private val profitLineItem = itemView.findViewById<View>(R.id.profit_line_item)
        private val tradeBonusLineItem = itemView.findViewById<View>(R.id.trade_bonus_line_item)
        private val commissionAmountLineItem =
            itemView.findViewById<View>(R.id.commission_line_item)
        private val myBalanceAfterLineItem =
            itemView.findViewById<ConstraintLayout>(R.id.my_balance_after_line_item)
        private val followUpLineItem =
            itemView.findViewById<ConstraintLayout>(R.id.follow_up_line_item)
        private val rolledBackLineItem =
            itemView.findViewById<ConstraintLayout>(R.id.rolled_back_line_item)

        fun bind(item: TransactionModel) {
            val myMsisdn = MasService.getMyMsisdn()

            val profitValue =
                item.costOfGoodsSold?.toDoubleOrNull()?.let { item.amount.toDouble() - it }

            if (!FeatureToggle.Stats.showSalesProfit) profitLineItem.visibility = View.GONE

            if (profitValue == null) {
                profitLineItem.findViewById<TextView>(R.id.value).text =
                    Formatter.italic("(" + context.getString(R.string.unknown).lowercase() + ")")
            } else {
                profitLineItem.findViewById<TextView>(R.id.value).text =
                    Formatter.formatCustomCurrency(profitValue)
            }

            /**
             * Setup the labels
             */
            val itemDescriptionLabel = itemDescriptionLineItem.findViewById<TextView>(R.id.label)
            val profitLabel = profitLineItem.findViewById<TextView>(R.id.label)
            val tradeBonusLabel = tradeBonusLineItem.findViewById<TextView>(R.id.label)
            val commissionAmountLabel = commissionAmountLineItem.findViewById<TextView>(R.id.label)
            val myBalanceAfterLabel = myBalanceAfterLineItem.findViewById<TextView>(R.id.label)
            val followUpLabel = followUpLineItem.findViewById<TextView>(R.id.label)
            val rolledBackLabel = rolledBackLineItem.findViewById<TextView>(R.id.label)

            itemDescriptionLabel.text = context.getString(R.string.item_description)
            profitLabel.text = context.getString(R.string.sale_history_profit)
            tradeBonusLabel.text = context.getString(R.string.sale_history_trade_bonus)
            commissionAmountLabel.text = context.getString(R.string.sale_history_commission_amount)
            myBalanceAfterLabel.text = context.getString(R.string.sale_history_balance_after)
            followUpLabel.text = context.getString(R.string.sale_history_follow_up)
            rolledBackLabel.text = context.getString(R.string.sale_history_rolled_back)

            // Disable visibility ahead of time unless specific criteria is met (lower down)
            tradeBonusLineItem.visibility = View.GONE
            itemDescriptionLineItem.visibility = View.GONE
            commissionAmountLineItem.visibility = View.GONE
            /** **/

            if (item.type == TXType.TRANSFER) {
                if (item.recipientMsisdn == myMsisdn) {
                    tradeBonusLineItem.visibility = View.VISIBLE
                    if (item.bonus.isNotEmpty() && item.amount.isNotEmpty()) {
                        val tradeBonusPercentage =
                            (item.bonus.toDouble() / item.amount.toDouble() * 100)

                        if (tradeBonusPercentage.equals(0.0)) {
                            tradeBonusLineItem.findViewById<TextView>(R.id.value).text = "-"
                        } else {
                            tradeBonusLineItem.findViewById<TextView>(R.id.value).text =
                                context.getString(
                                    R.string.sale_history_trade_bonus_value,
                                    Formatter.formatCustomCurrency(item.bonus.toDouble()),
                                    String.format("%.1f", tradeBonusPercentage)
                                )
                        }
                    } else {
                        tradeBonusLineItem.findViewById<TextView>(R.id.value).text = "-"
                    }
                }
            } else if (item.type in listOf(TXType.NON_AIRTIME_DEBIT, TXType.NON_AIRTIME_REFUND)) {
                /**
                 * Description line item
                 */
                if (item.itemDescription != null && item.itemDescription.isNotEmpty()) {
                    itemDescriptionLineItem.visibility = View.VISIBLE
                    itemDescriptionLineItem.findViewById<TextView>(R.id.value).text =
                        item.itemDescription
                }

                if (item.type == TXType.NON_AIRTIME_DEBIT) {
                    /**
                     * Commission line item
                     */
                    commissionAmountLineItem.visibility = View.VISIBLE
                    val commission = item.commissionAmount?.toDoubleOrNull()
                    val commissionAmountTextView =
                        commissionAmountLineItem.findViewById<TextView>(R.id.value)
                    commissionAmountTextView.text = when (commission) {
                        null -> Formatter.italic(
                            "(${context.getString(R.string.unknown).lowercase()})"
                        )
                        0.0 -> "-"
                        else -> Formatter.formatCustomCurrency(commission)
                    }
                }
            }

            when (item.type) {
                TXType.SELL -> icon.text = airtimeSaleIcon
                TXType.TRANSFER -> icon.text = transferIcon
                TXType.ADJUDICATE -> icon.text = adjudicateIcon
                TXType.ADJUST -> icon.text = adjustIcon
                TXType.SELF_TOPUP -> icon.text = selfTopupIcon
                TXType.PROMOTION_REWARD -> icon.text = rewardIcon
                in listOf(TXType.REVERSE, TXType.REVERSE_PARTIALLY) -> icon.text = reversalIcon
                in listOf(TXType.NON_AIRTIME_DEBIT, TXType.NON_AIRTIME_REFUND) -> icon.text = bundleSaleIcon
                else -> {}
            }

            var isOutbound =
                (item.type == TXType.SELL || item.type == TXType.TRANSFER || item.type == TXType.NON_AIRTIME_DEBIT) && item.recipientMsisdn != myMsisdn

            // special case - when selling airtime to myself, we "deduct" from the crediverse account, so it counts as "outbound"
            if (item.recipientMsisdn == myMsisdn && item.type == TXType.SELF_TOPUP) {
                isOutbound = true
            }

            repeatSaleButtonWrapper.visibility = View.GONE

            if (isOutbound) {
                // OUTBOUND transaction
                if (item.amount.toDouble() >= 0) {
                    /**
                     * Only show "Repeat Sale" buttons for each TX type if nav is enabled
                     */
                    if (
                        (FeatureToggle.Nav.hasTransferPage && item.type == TXType.TRANSFER) ||
                        (FeatureToggle.Nav.hasBundlePage && item.type == TXType.NON_AIRTIME_DEBIT) ||
                        item.type == TXType.SELL
                    )
                        repeatSaleButtonWrapper.visibility = View.VISIBLE
                }
                boundIcon.text = arrowRightIcon
                boundIcon.setTextColor(context.getColor(R.color.danger))
            } else {
                // INBOUND transaction
                boundIcon.text = arrowLeftIcon
                boundIcon.setTextColor(context.getColor(R.color.second_success))
            }

            txType.text = context.getString(item.type.resource)

            if (item.recipientMsisdn == myMsisdn) {
                affectedBParty.text =
                    context.getString(R.string.from_prefix, Formatter.bold(item.sourceMsisdn))
            } else {
                affectedBParty.text =
                    context.getString(R.string.to_prefix, Formatter.bold(item.recipientMsisdn))
            }

            amount.text = Formatter.formatCustomCurrency(item.amount.toDouble())
            status.text = context.getString(TXStatus.getResourceOrOtherError(item.status))
            if (item.status == "SUCCESS") {
                if (item.followUpRequired) {
                    // Success is not known if follow up is required...
                    status.text = context.getString(R.string.unknown_status)
                    status.setTextColor(context.getColor(R.color.warn))
                } else {
                    status.setTextColor(context.getColor(R.color.green))
                }
            } else {
                status.setTextColor(context.getColor(R.color.red))
            }
            txReference.text = item.transactionNo
            txStarted.text =
                Formatter.dateStringFromEpoch(item.transactionStarted, "EEE, MMM d,\nh:mm a")
            //balanceAfter.text =
            myBalanceAfterLineItem.findViewById<TextView>(R.id.value).text =
                if (item.balanceAfter == "") "" else Formatter.formatCustomCurrency(item.balanceAfter.toDouble())

            val followUpRequired = followUpLineItem.findViewById<TextView>(R.id.value)
            followUpRequired.text = if (item.followUpRequired) {
                followUpRequired.setTextColor(context.getColor(R.color.warn))
                context.getString(R.string.yes)
            } else context.getString(R.string.no)

            val rolledBack = rolledBackLineItem.findViewById<TextView>(R.id.value)
            rolledBack.text = if (item.rolledBack) {
                rolledBack.setTextColor(context.getColor(R.color.warn))
                context.getString(R.string.yes)
            } else context.getString(R.string.no)
        }
    }
}
