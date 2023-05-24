package se.ju.student.robomow.domain

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData

interface BluetoothModel {
    val newDevices: LiveData<Set<BluetoothDevice>>
    val previouslyPairedDevices: LiveData<Set<BluetoothDevice>>

    fun startDiscovery()
    fun cancelDiscovery()
    fun unregisterReceiver()
    fun registerReceiver()
}
