package se.ju.student.robomow

object BluetoothClientHolder {
    var bluetoothClient: BluetoothClient? = null

    fun disconnect() {
        bluetoothClient?.disconnect()
        bluetoothClient = null
    }
}