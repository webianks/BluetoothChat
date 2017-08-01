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

class DevicesRecyclerViewAdapter(val mDeviceList: List<DeviceData>, val context: Context) :
        RecyclerView.Adapter<DevicesRecyclerViewAdapter.VH>() {


    private var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_single_item, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder?.label?.text = mDeviceList[position].deviceName ?: mDeviceList[position].deviceHardwareAddress
    }

    override fun getItemCount(): Int {
        return mDeviceList.size
    }

    inner class VH(itemView: View?) : RecyclerView.ViewHolder(itemView){

        var label: TextView? = itemView?.findViewById(R.id.largeLabel)

        init {
            itemView?.setOnClickListener{
                listener?.itemClicked(mDeviceList[adapterPosition])
            }
        }
    }

    fun setItemClickListener(listener: ItemClickListener){
        this.listener = listener
    }

    interface ItemClickListener{
        fun itemClicked(deviceData: DeviceData)
    }
}