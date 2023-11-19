package com.example.kian_hosseinkhani_myruns2.activitiesAndFragments.details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.kian_hosseinkhani_myruns2.R

class DetailsListAdapterManualEntry(private val context: Context, private val dataSource: List<Pair<String, String?>>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Pair<String, String?> = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.item_detail, parent, false)

        val primaryTextView = rowView.findViewById<TextView>(R.id.primaryText)
        val secondaryTextView = rowView.findViewById<TextView>(R.id.secondaryText)

        val pair = getItem(position)

        primaryTextView.text = pair.first
        secondaryTextView.text = pair.second

        if(pair.second.isNullOrEmpty()) {
            secondaryTextView.visibility = View.GONE
        }

        return rowView
    }
}
