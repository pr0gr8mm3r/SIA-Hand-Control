package de.sia.wazynski.simon.handcontrol

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rv_item_bt_device.view.*

class BtDeviceAdapter(private val inflater: LayoutInflater, val clickListener: OnClickListener): RecyclerView.Adapter<BtDeviceAdapter.ViewHolder>() {

    private var devices = mutableListOf<BluetoothDevice>()

    class ViewHolder(v: View, private val clickListener: OnClickListener): RecyclerView.ViewHolder(v) {
        init { v.setOnClickListener { clickListener.onBtDeviceClick(adapterPosition) } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            inflater.inflate(
                R.layout.rv_item_bt_device,
                parent,
                false
            ),
            clickListener
        )
    }

    override fun getItemCount(): Int = devices.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tvName.text = devices[position].name
        holder.itemView.tvAddress.text = devices[position].address
    }

    fun updateDevices(devices: MutableList<BluetoothDevice>) {
        this.devices = devices
        notifyDataSetChanged()
    }

    interface OnClickListener {
        fun onBtDeviceClick(position: Int)
    }
}