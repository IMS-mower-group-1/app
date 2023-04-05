package se.ju.student.robomow

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothClient(private val device: BluetoothDevice) {
    private val uuid: UUID = UUID.fromString("30097c35-95b9-4f92-9d2e-e3e06aa3b07f")
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    fun sendMessage(message: String): Boolean {
        return true
    }

    fun readMessage(): String? {
        return "message"
    }

    fun disconnect() {

    }
}