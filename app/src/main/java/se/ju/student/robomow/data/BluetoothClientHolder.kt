package se.ju.student.robomow.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BluetoothClientHolder {
    var bluetoothClient: BluetoothClient? = null
    private val _connectionStatus = MutableStateFlow<Boolean?>(null)
    val connectionStatus: StateFlow<Boolean?> = _connectionStatus

    fun updateConnectionStatus(status: Boolean?) {
        _connectionStatus.value = status
    }

    fun disconnect() {
        bluetoothClient?.disconnect()
        bluetoothClient = null
        updateConnectionStatus(false)
    }
}