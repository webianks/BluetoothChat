package com.webianks.bluechat

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by R Ankit on 18-07-2017.
 */

class DevicesRecyclerViewAdapter(val mList: List<String>, val context: Context) :
        RecyclerView.Adapter<DevicesRecyclerViewAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_single_item, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder?.label?.text = mList[position]
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class VH(itemView: View?) : RecyclerView.ViewHolder(itemView){
        val label = itemView?.findViewById<TextView>(R.id.largeLabel)
     }
}