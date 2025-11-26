package systems.concurrent.crediversemobile.recyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import systems.concurrent.crediversemobile.R
import systems.concurrent.crediversemobile.models.InfoItem

class InfoItemAdapter(context: Context, private val data: List<InfoItem>) :
    ArrayAdapter<InfoItem>(context, R.layout.info_item, data) {

    private val _tag = this::class.java.kotlin.simpleName

    private var height = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =
            convertView ?: LayoutInflater.from(context).inflate(R.layout.info_item, parent, false)
        val nameView = view.findViewById<TextView>(R.id.info_item_name)
        val valueView = view.findViewById<TextView>(R.id.info_item_value)

        val item = getItem(position)
        nameView.text = item?.name ?: ""
        valueView.text = item?.value ?: ""

        val nameViewHeightIsGreater = nameView.measuredHeight > valueView.measuredHeight

        height += if (nameViewHeightIsGreater) nameView.measuredHeight else valueView.measuredHeight

        return view
    }

    fun getHeight() = height
}
