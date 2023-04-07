package se.ju.student.robomow

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class DeviceListActivity : AppCompatActivity() {

    private lateinit var deviceListViewModel: DeviceListViewModel
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDevicesListView: ListView
    private lateinit var newDevicesListView: ListView
    private lateinit var pairedDevicesArrayAdapter: ArrayAdapter<String>
    private lateinit var newDevicesArrayAdapter: ArrayAdapter<String>
    private val deviceList = mutableListOf<BluetoothDevice>()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        deviceListViewModel = ViewModelProvider(this).get(DeviceListViewModel::class.java)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        pairedDevicesListView = findViewById(R.id.paired_devices_list_view)
        newDevicesListView = findViewById(R.id.new_devices_list_view)

        pairedDevicesArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        newDevicesArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        pairedDevicesListView.adapter = pairedDevicesArrayAdapter
        newDevicesListView.adapter = newDevicesArrayAdapter

        deviceListViewModel.previouslyPairedDevices.observe(this) { devices ->
            devices.forEach { device ->
                val deviceInfo = "${device.name} - ${device.address}"
                pairedDevicesArrayAdapter.add(deviceInfo)
                deviceList.add(device)
            }
        }

        deviceListViewModel.newDevices.observe(this) { devices ->
            devices.forEach { device ->
                if (!deviceList.contains(device)) {
                    val deviceInfo = "${device.name ?: "Unknown"} - ${device.address}"
                    newDevicesArrayAdapter.add(deviceInfo)
                    deviceList.add(device)
                }
            }
        }

        deviceListViewModel.startDiscovery(this)

        pairedDevicesListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            connectToDevice(position)
        }

        newDevicesListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            connectToDevice(position)
        }
    }

    private fun connectToDevice(position: Int) {
        val device = deviceList[position]
        val intent = Intent(this, JoystickActivity::class.java)
        intent.putExtra("device", device)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        deviceListViewModel.cancelDiscovery(this)
    }
}