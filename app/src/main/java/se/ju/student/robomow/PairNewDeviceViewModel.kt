package se.ju.student.robomow

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PairNewDeviceViewModel: ViewModel() {
    private val _newDevices = MutableLiveData<List<BluetoothDevice>>()
    val newDevices: LiveData<List<BluetoothDevice>>
        get() = _newDevices

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)

    fun getReceiver(): BroadcastReceiver = receiver
    fun getIntentFilter(): IntentFilter = filter

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    try {
                        if (device is BluetoothDevice)
                            _newDevices.value = _newDevices.value.orEmpty().plus(device)
                    } catch (e: SecurityException) {
                        Log.e("Bluetooth:", e.toString())
                    }
                }
            }
        }
    }

    fun startDiscovery(context: Context) {
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(context as Activity, permissions, 0)
        bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter
        try {
            bluetoothAdapter?.startDiscovery()

        } catch (e: SecurityException){

        }
    }
}