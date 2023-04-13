package se.ju.student.robomow.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.ProgressDialog
import android.os.Handler
import android.os.Looper
import android.widget.*
import dagger.hilt.android.AndroidEntryPoint
import se.ju.student.robomow.BluetoothDeviceListAdapter
import se.ju.student.robomow.R
import se.ju.student.robomow.ui.viewmodel.DeviceListViewModel

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class DeviceListActivity : AppCompatActivity() {

    private lateinit var deviceListViewModel: DeviceListViewModel
    private val mainScope = CoroutineScope(Dispatchers.Main)
    // Add a progress dialog to show during the pairing process
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        deviceListViewModel = ViewModelProvider(this)[DeviceListViewModel::class.java]

        val pairedDevicesListView = findViewById<ListView>(R.id.paired_devices_list_view)
        val newDevicesListView = findViewById<ListView>(R.id.new_devices_list_view)

        val pairedDevicesArrayAdapter = BluetoothDeviceListAdapter(this)
        val newDevicesArrayAdapter = BluetoothDeviceListAdapter(this)

        pairedDevicesListView.adapter = pairedDevicesArrayAdapter
        newDevicesListView.adapter = newDevicesArrayAdapter

        deviceListViewModel.previouslyPairedDevices.observe(this) { devices ->
            pairedDevicesArrayAdapter.clear()
            pairedDevicesArrayAdapter.addAll(devices)

        }

        deviceListViewModel.newDevices.observe(this) { devices ->
            newDevicesArrayAdapter.clear()
            newDevicesArrayAdapter.addAll(devices)

        }

        // Initialize the progress dialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Pairing...")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

        pairedDevicesListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device = pairedDevicesListView.getItemAtPosition(position)
            if (device is BluetoothDevice){
                connectToDevice(device)
            }
        }

        newDevicesListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device = newDevicesListView.getItemAtPosition(position)
            if (device is BluetoothDevice){
                connectToDevice(device)
            }
        }
    }
    private fun connectToDevice(device: BluetoothDevice) {
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
    private suspend fun createBond(device: BluetoothDevice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            device.createBond()
        } else {
            val createBondMethod = device.javaClass.getMethod("createBond")
            createBondMethod.invoke(device)
        }
    }

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

    override fun onStart(){
        super.onStart()
        deviceListViewModel.startDiscovery()
    }
    override fun onDestroy() {
        super.onDestroy()
        deviceListViewModel.unregisterReceiver()
    }
}