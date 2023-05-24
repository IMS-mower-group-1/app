package se.ju.student.robomow.model

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
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
import kotlinx.coroutines.flow.MutableStateFlow
import se.ju.student.robomow.R
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

    private val _isDiscovering = MutableLiveData(false)
    override val isDiscovering: LiveData<Boolean> get() = _isDiscovering

    init {
        getPreviouslyPairedDevices()
    }

    override fun startDiscovery() {
        if (!hasBluetoothDiscoveryPermissions()){
            Intent(context.getString(R.string.missing_permission_filter)).also {
                context.sendBroadcast(it)
            }
            return
        }
        if (bluetoothAdapter?.isDiscovering == true) {
            return
        }
        bluetoothAdapter?.startDiscovery()
    }
    private fun hasBluetoothDiscoveryPermissions(): Boolean {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            return false
        }
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            return false
        }
        return true
    }

    override fun cancelDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun unregisterReceiver() {
        context.unregisterReceiver(deviceFoundReceiver)
    }

    override fun registerReceiver() {
        context.registerReceiver(
            deviceFoundReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND).also {
                it.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                it.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
        )
    }

    private fun getPreviouslyPairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            Intent(context.getString(R.string.missing_permission_filter)).also {
                context.sendBroadcast(it)
            }
            return
        }
        _previouslyPairedDevices.value = bluetoothAdapter?.bondedDevices
    }

    private fun hasPermission(permission: String): Boolean {
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
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isDiscovering.postValue(true)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isDiscovering.postValue(false)
                }
            }
        }
    }
}