package se.ju.student.robomow

import android.app.Application
import android.bluetooth.BluetoothDevice
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@HiltAndroidApp
class RoboMowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
    val mainScope = CoroutineScope(Dispatchers.Main)

    fun connectToDevice(
        device: BluetoothDevice,
        onConnected: () -> Unit,
        onFailed: () -> Unit
    ): BluetoothClient {
        val bluetoothClient = BluetoothClient(device)
        mainScope.launch {
            if (bluetoothClient.connect()) {
                onConnected()
            } else {
                onFailed()
            }
        }
        return bluetoothClient
    }

    fun disconnectFromDevice(bluetoothClient: BluetoothClient) {
        mainScope.cancel()
        bluetoothClient.disconnect()
    }
}
