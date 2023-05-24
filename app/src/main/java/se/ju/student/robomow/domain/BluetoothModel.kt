package se.ju.student.robomow.domain

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.MutableStateFlow

interface BluetoothModel {
    val newDevices: LiveData<Set<BluetoothDevice>>
    val previouslyPairedDevices: LiveData<Set<BluetoothDevice>>
    val isDiscovering: LiveData<Boolean>

    fun startDiscovery()
    fun cancelDiscovery()
    fun unregisterReceiver()
    fun registerReceiver()
}
