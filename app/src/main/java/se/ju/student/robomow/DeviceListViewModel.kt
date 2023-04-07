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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DeviceListViewModel : ViewModel() {

    private val _previouslyPairedDevices = MutableLiveData<Set<BluetoothDevice>>()
    private val _newDevices = MutableLiveData<MutableSet<BluetoothDevice>>()
    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    val previouslyPairedDevices: MutableLiveData<Set<BluetoothDevice>>
        get() = _previouslyPairedDevices
    val newDevices: MutableLiveData<MutableSet<BluetoothDevice>>
        get() = _newDevices

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        _newDevices.value?.add(device)
                        _newDevices.postValue(_newDevices.value)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    context?.unregisterReceiver(this)
                }
            }
        }
    }

    init {
        @SuppressLint("MissingPermission")
        _previouslyPairedDevices.value = bluetoothAdapter.bondedDevices
        _newDevices.value = mutableSetOf()
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(context: Context) {
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(context as Activity, permissions, 0)
        bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun cancelDiscovery(context: Context) {
        bluetoothAdapter.cancelDiscovery()
        context.unregisterReceiver(receiver)
    }
}