package de.sia.wazynski.simon.handcontrol

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.tfe_pn_fragment_bt_devices.view.*


class BtDeviceDialog : BottomSheetDialogFragment() {

    private var callback: BtDeviceDialogCallback? = null

    private var list: RecyclerView? = null
    private var adapter: BtDeviceAdapter? = null

    private var devices = mutableListOf<BluetoothDevice>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tfe_pn_fragment_bt_devices, container, false)

        println("Dialog onCreateView")

        list = root.rvDeviceList
        list?.layoutManager = LinearLayoutManager(context)
        adapter = BtDeviceAdapter(
            layoutInflater,
            btDeviceClickListener
        )
        adapter?.updateDevices(devices)
        adapter?.notifyDataSetChanged()
        list?.adapter = adapter

        root.toolbar.setNavigationOnClickListener { dismiss() }

        return root
    }

    private val btDeviceClickListener = object :
        BtDeviceAdapter.OnClickListener {
        override fun onBtDeviceClick(position: Int) {
            callback?.onDeviceToConnect(devices[position])
            dismiss()
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = activity as BtDeviceDialogCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    fun setDevices(devices: MutableList<BluetoothDevice>) {
        this.devices = devices
        adapter?.updateDevices(devices)
    }

    interface BtDeviceDialogCallback {
        fun onDeviceToConnect(device: BluetoothDevice)
    }
}
