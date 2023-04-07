package se.ju.student.robomow

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
@SuppressLint("MissingPermission")
class DeviceListViewModel : ViewModel() {

    private val _newDevices = MutableLiveData<Set<BluetoothDevice>>()
    val newDevices: LiveData<Set<BluetoothDevice>> get() = _newDevices

    private val _previouslyPairedDevices = MutableLiveData<Set<BluetoothDevice>>()
    val previouslyPairedDevices: LiveData<Set<BluetoothDevice>> get() = _previouslyPairedDevices


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                        )
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device is BluetoothDevice) {
                        _newDevices.value = _newDevices.value.orEmpty().plus(device)
                    }
                }
            }
        }
    }

    fun getPreviouslyPairedDevices(context: Context) {
        val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter
        try {
            _previouslyPairedDevices.value = bluetoothAdapter.bondedDevices
        } catch (e: SecurityException) {
            // TODO: Handle Exception
        }
    }

    fun startDiscovery(context: Context) {
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(context as Activity, permissions, 0)
        val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    fun cancelDiscovery(context: Context) {
        val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter
        bluetoothAdapter.cancelDiscovery()
    }
}