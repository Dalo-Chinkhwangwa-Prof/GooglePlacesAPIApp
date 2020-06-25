package com.bigbang.myplacecompass.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bigbang.myplacecompass.R
import com.bigbang.myplacecompass.model.data.Result
import kotlinx.android.synthetic.main.location_item_layout.view.*

class PlaceAdapter(var placeList: List<Result>) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.location_item_layout, parent, false)
        return PlaceViewHolder(itemView)
    }

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.itemView.place_name_textview.text = placeList[position].name
    }
}