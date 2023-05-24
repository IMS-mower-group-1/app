package se.ju.student.robomow.ui.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import se.ju.student.robomow.domain.BluetoothModel
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val bluetoothModel: BluetoothModel
) : ViewModel() {

    val newDevices: LiveData<Set<BluetoothDevice>> get() = bluetoothModel.newDevices
    val previouslyPairedDevices: LiveData<Set<BluetoothDevice>> get() = bluetoothModel.previouslyPairedDevices
    fun startDiscovery() {
        bluetoothModel.startDiscovery()
    }

    fun cancelDiscovery() {
        bluetoothModel.cancelDiscovery()
    }

    fun unregisterReceiver() {
        try {
            bluetoothModel.unregisterReceiver()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun registerReceiver() {
        bluetoothModel.registerReceiver()
    }
}