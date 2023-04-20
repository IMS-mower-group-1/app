package se.ju.student.robomow.adapter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import se.ju.student.robomow.R

@SuppressLint("MissingPermission")
class BluetoothDeviceListAdapter(context: Context) :
    ArrayAdapter<BluetoothDevice>(context, R.layout.list_item_device) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_device, parent, false)

        val device = getItem(position)

        val deviceName = view.findViewById<TextView>(R.id.device_name)
        deviceName.text = device?.name ?: "Unknown device name"

        val deviceAddress = view.findViewById<TextView>(R.id.device_address)
        deviceAddress.text = device?.address ?: "Unknown address"

        return view
    }
}