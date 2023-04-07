package se.ju.student.robomow

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.ProgressDialog
import android.os.Handler
import android.os.Looper

class DeviceListActivity : AppCompatActivity() {

    private lateinit var deviceListViewModel: DeviceListViewModel
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDevicesListView: ListView
    private lateinit var newDevicesListView: ListView
    private lateinit var pairedDevicesArrayAdapter: ArrayAdapter<String>
    private lateinit var newDevicesArrayAdapter: ArrayAdapter<String>
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private val pairedDeviceList = mutableListOf<BluetoothDevice>()
    private val newDeviceList = mutableListOf<BluetoothDevice>()

    // Add a progress dialog to show during the pairing process
    private lateinit var progressDialog: ProgressDialog
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        deviceListViewModel = ViewModelProvider(this).get(DeviceListViewModel::class.java)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        deviceListViewModel.getPreviouslyPairedDevices(this)

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
                pairedDeviceList.add(device)
            }
        }

        deviceListViewModel.newDevices.observe(this) { devices ->
            devices.forEach { device ->
                if (!newDeviceList.contains(device)) {
                    val deviceInfo = "${device.name ?: "Unknown"} - ${device.address}"
                    newDevicesArrayAdapter.add(deviceInfo)
                    newDeviceList.add(device)
                }
            }
        }

        // Initialize the progress dialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Pairing...")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

        deviceListViewModel.startDiscovery(this)

        pairedDevicesListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            connectToDevice(position, true)
        }

        newDevicesListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            connectToDevice(position, false)
        }
    }
    @SuppressLint("MissingPermission")
    private fun connectToDevice(position: Int, isPaired: Boolean) {
        val device = if (isPaired) {
            pairedDeviceList[position]
        } else {
            newDeviceList[position]
        }

        // Show the progress dialog
        progressDialog.show()

        // Register the BroadcastReceiver to listen for the bonding process to complete
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondingBroadcastReceiver, intentFilter)

        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            // Initiate a pairing request if the device is not paired
            mainScope.launch {
                createBond(device)
            }
        } else {
            // If the device is already paired, connect to it
            Handler(Looper.getMainLooper()).postDelayed({
                progressDialog.dismiss()
                connectToPairedDevice(device)
            }, 1000) // Show the ProgressDialog for a while before connecting
        }
    }

    private fun connectToPairedDevice(device: BluetoothDevice) {
        val intent = Intent(this, JoystickActivity::class.java)
        intent.putExtra("device", device)
        startActivity(intent)
    }
    @SuppressLint("MissingPermission")
    suspend fun createBond(device: BluetoothDevice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            device.createBond()
        } else {
            val createBondMethod = device.javaClass.getMethod("createBond")
            createBondMethod.invoke(device)
        }
    }

    @SuppressLint("MissingPermission")
    private val bondingBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    when (device.bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            // Pairing is successful, proceed with the connection
                            Toast.makeText(
                                this@DeviceListActivity,
                                "Successfully paired",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressDialog.dismiss()
                            connectToPairedDevice(device)
                            context?.unregisterReceiver(this) // Unregister BroadcastReceiver
                        }
                        BluetoothDevice.BOND_NONE -> {
                            // Pairing failed, show an error message
                            Toast.makeText(
                                this@DeviceListActivity,
                                "Failed to pair with the device",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressDialog.dismiss()
                            context?.unregisterReceiver(this) // Unregister BroadcastReceiver
                        }
                        BluetoothDevice.BOND_BONDING -> {
                            // Pairing is in progress
                        }
                        else -> {
                            progressDialog.dismiss()
                            context?.unregisterReceiver(this) // Unregister BroadcastReceiver
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    context?.unregisterReceiver(this) // Unregister BroadcastReceiver
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deviceListViewModel.cancelDiscovery(this)
    }
}