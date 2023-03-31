package se.ju.student.robomow

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat

class PairNewDeviceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_new_device)

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)

        val bluetoothAdapter: BluetoothAdapter? = getSystemService(BluetoothManager::class.java).adapter
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        try {
            bluetoothAdapter?.startDiscovery()
        } catch (e: SecurityException) {
            Log.e("Bluetooth:", e.toString())
        }
        registerReceiver(receiver, filter)
        
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    try {
                        Log.i("Bluetooth:", device.name + device.address)
                        val deviceName = device.name
                        val deviceHardwareAddress = device.address // MAC address
                    } catch (e: SecurityException) {
                        Log.e("Bluetooth:", e.toString())
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}
