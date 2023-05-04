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

    val mainScope = CoroutineScope(Dispatchers.Main)

    // Create and return bluetoothClient and try to connect to socket
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

}
