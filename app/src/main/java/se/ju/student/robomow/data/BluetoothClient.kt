package se.ju.student.robomow.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothClient(private val device: BluetoothDevice) {

    private val uuid: UUID = UUID.fromString("30097c35-95b9-4f92-9d2e-e3e06aa3b07f")
    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    // Shared data buffer
    private val sharedBuffer = PublishSubject.create<String>()

    private lateinit var heartbeatJob: Job


    @SuppressLint("MissingPermission")
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream

            heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    val timeoutOccurred = withTimeoutOrNull(6000) {
                        while (true) {
                            val message = readMessage()
                            message?.let { sharedBuffer.onNext(it) }
                            Log.d("pulse", message.toString())
                            if (message == "1") {
                                break
                            }
                        }
                        false
                    } ?: true

                    if (timeoutOccurred) {
                        BluetoothClientHolder.disconnect()
                        break
                    }
                }
            }

        } catch (e: IOException) {
            Log.e("BluetoothClient", "Error connecting to socket", e)
            return@withContext false
        }
        return@withContext true
    }


    fun getSharedBuffer(): PublishSubject<String> {
        return sharedBuffer
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

    suspend fun readMessage(): String? = withContext(Dispatchers.IO) {
        if (inputStream == null) return@withContext null
        val buffer = ByteArray(1024)
        try {
            val bytes = inputStream?.read(buffer) ?: 0
            String(buffer, 0, bytes)
        } catch (e: IOException) {
            Log.e("BluetoothClient", "Error reading message", e)
            null
        } catch (e: Exception) {
            Log.e("BluetoothClient", "Unexpected error while reading message", e)
            null
        }
    }

    fun disconnect() {
        try {
            Log.d("pulse", "DISCONNECTING")
            heartbeatJob.cancel()
            socket?.close()
            inputStream?.close()
            outputStream?.close()

            sharedBuffer.onComplete()
        } catch (e: IOException) {
            Log.e("BluetoothClient", "Error disconnecting from socket", e)
        }
    }
}