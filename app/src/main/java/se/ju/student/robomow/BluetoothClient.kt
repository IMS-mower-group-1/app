package se.ju.student.robomow

import android.annotation.SuppressLint
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

    @SuppressLint("MissingPermission")
    fun connect(): Boolean {
        socket = device.createRfcommSocketToServiceRecord(uuid)
        socket?.connect()
        inputStream = socket?.inputStream
        outputStream = socket?.outputStream
        return true
    }

    fun sendMessage(message: String): Boolean {
        if (outputStream == null) return false
        outputStream?.write(message.toByteArray())
        return true
    }

    fun readMessage(): String? {
        if (inputStream == null) return null
        val buffer = ByteArray(1024)
        val bytes = inputStream?.read(buffer) ?: 0
        return String(buffer, 0, bytes)
    }

    fun disconnect() {
        socket?.close()
        inputStream?.close()
        outputStream?.close()
    }
}