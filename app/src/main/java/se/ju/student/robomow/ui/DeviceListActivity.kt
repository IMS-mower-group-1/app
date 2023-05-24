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
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.Provides
import dagger.hilt.android.AndroidEntryPoint
import se.ju.student.robomow.BluetoothClient
import se.ju.student.robomow.BluetoothClientHolder
import se.ju.student.robomow.adapter.BluetoothDeviceListAdapter
import se.ju.student.robomow.R
import se.ju.student.robomow.RoboMowApplication
import se.ju.student.robomow.ui.viewmodel.DeviceListViewModel

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class DeviceListActivity : AppCompatActivity(), PermissionCallback {

    private lateinit var deviceListViewModel: DeviceListViewModel
    private lateinit var permissionReceiver: PermissionBroadcastReceiver
    private val mainScope = CoroutineScope(Dispatchers.Main)

    // Add a progress dialog to show during the pairing process
    private lateinit var progressBar: ProgressBar
    private lateinit var discoveryProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        deviceListViewModel = ViewModelProvider(this)[DeviceListViewModel::class.java]

        registerPermissionReceiver()

        val pairedDevicesListView = findViewById<ListView>(R.id.paired_devices_list_view)
        val newDevicesListView = findViewById<ListView>(R.id.new_devices_list_view)

        val pairedDevicesArrayAdapter = BluetoothDeviceListAdapter(this)
        val newDevicesArrayAdapter = BluetoothDeviceListAdapter(this)
        deviceListViewModel.registerReceiver()
        val scanButton = findViewById<Button>(R.id.scan_button)
        scanButton.setOnClickListener {
            deviceListViewModel.startDiscovery()
        }
        discoveryProgressBar = findViewById(R.id.discovery_progress_bar)
        deviceListViewModel.isDiscovering.observe(this) {
            if (it) {
                discoveryProgressBar.visibility = View.VISIBLE
            } else {
                discoveryProgressBar.visibility = View.GONE
            }
        }

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

        progressBar = findViewById(R.id.progress_indicator)

        pairedDevicesListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device = pairedDevicesListView.getItemAtPosition(position)
                if (device is BluetoothDevice) {
                    connectToDevice(device)
                }
            }

        newDevicesListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device = newDevicesListView.getItemAtPosition(position)
                if (device is BluetoothDevice) {
                    connectToDevice(device)
                }
            }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        // Show the progress dialog
        progressBar.visibility = View.VISIBLE

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
                connectToPairedDevice(device)
            }, 1000) // Show the ProgressDialog for a while before connecting
        }
    }

    private fun connectToPairedDevice(device: BluetoothDevice) {
        connectToSocket(device) {}
    }

    private fun connectToSocket(device: BluetoothDevice, onConnectionSuccess: () -> Unit) {
        val bluetoothClient = (application as RoboMowApplication).connectToDevice(
            device,
            onConnected = {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@DeviceListActivity,
                    "Connected to the device",
                    Toast.LENGTH_SHORT
                ).show()
                onConnectionSuccess()
                BluetoothClientHolder.updateConnectionStatus(true)
            },
            onFailed = {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@DeviceListActivity,
                    "Failed to connect to the device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        // Assign the new bluetoothClient to the singleton
        BluetoothClientHolder.bluetoothClient = bluetoothClient
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
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    when (device.bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            // Pairing is successful, proceed with the connection
                            Toast.makeText(
                                this@DeviceListActivity,
                                "Successfully paired",
                                Toast.LENGTH_SHORT
                            ).show()
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
                            progressBar.visibility = View.GONE
                            context?.unregisterReceiver(this) // Unregister BroadcastReceiver
                        }
                        BluetoothDevice.BOND_BONDING -> {
                            // Pairing is in progress
                        }
                        else -> {
                            progressBar.visibility = View.GONE
                            context?.unregisterReceiver(this) // Unregister BroadcastReceiver
                        }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    context?.unregisterReceiver(this) // Unregister BroadcastReceiver
                }
            }
        }
    }

    private fun registerPermissionReceiver() {
        permissionReceiver = PermissionBroadcastReceiver(this)
        registerReceiver(
            permissionReceiver,
            IntentFilter(getString(R.string.missing_permission_filter))
        )
    }

    override fun onPermissionMissing() {
        val positiveButtonLabel =
            getString(R.string.alert_dialog_bluetooth_permissions_positive_button)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.alert_dialog_bluetooth_permissions_title)
            .setMessage(
                getString(
                    R.string.alert_dialog_bluetooth_permissions_message,
                    positiveButtonLabel
                )
            )
            .setPositiveButton(positiveButtonLabel) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNeutralButton(R.string.close) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(permissionReceiver)
        deviceListViewModel.unregisterReceiver()
    }
}