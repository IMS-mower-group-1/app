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
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
        } catch (e: IOException) {
            Log.e("BluetoothClient", "Error connecting to socket", e)
            return false
        }
        return true
    }

    fun sendMessage(message: String): Boolean {
        if (outputStream == null) return false
        return try {
            outputStream?.write(message.toByteArray())
            true
        } catch (e: IOException) {
            Log.e("BluetoothClient", "Error sending message", e)
            false
        }
    }

    fun readMessage(): String? {
        if (inputStream == null) return null
        val buffer = ByteArray(1024)
        return try {
            val bytes = inputStream?.read(buffer) ?: 0
            String(buffer, 0, bytes)
        } catch (e: IOException) {
            Log.e("BluetoothClient", "Error reading message", e)
            null
        }
    }

    fun disconnect() {
        try {
            socket?.close()
            inputStream?.close()
            outputStream?.close()
        } catch (e: IOException) {
            Log.e("BluetoothClient", "Error disconnecting from socket", e)
        }
    }
}