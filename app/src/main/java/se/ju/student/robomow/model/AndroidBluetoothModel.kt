package se.ju.student.robomow.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.ju.student.robomow.domain.BluetoothModel

@SuppressLint("MissingPermission")
class AndroidBluetoothModel(
    private val context: Context
) : BluetoothModel {
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _newDevices = MutableLiveData<Set<BluetoothDevice>>()
    override val newDevices: LiveData<Set<BluetoothDevice>>
        get() = _newDevices

    private val _previouslyPairedDevices = MutableLiveData<Set<BluetoothDevice>>()
    override val previouslyPairedDevices: LiveData<Set<BluetoothDevice>>
        get() = _previouslyPairedDevices

    init {
        getPreviouslyPairedDevices()
    }

    override fun startDiscovery() {
        context.registerReceiver(
            deviceFoundReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        bluetoothAdapter?.startDiscovery()
    }

    override fun cancelDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun unregisterReceiver() {
        context.unregisterReceiver(deviceFoundReceiver)
    }

    private fun getPreviouslyPairedDevices() {
        try {
            _previouslyPairedDevices.value = bluetoothAdapter?.bondedDevices
        } catch (e: SecurityException) {
            // TODO: Handle Exception
        }
    }

    private fun hasRequiredPermissions(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private val deviceFoundReceiver = object : BroadcastReceiver() {
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
}