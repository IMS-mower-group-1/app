package se.ju.student.robomow

object BluetoothClientHolder {
    var bluetoothClient: BluetoothClient? = null

    fun isConnected(): Boolean {
        return bluetoothClient?.isConnected() ?: false
    }

    fun disconnect() {
        bluetoothClient?.disconnect()
        bluetoothClient = null
    }
}