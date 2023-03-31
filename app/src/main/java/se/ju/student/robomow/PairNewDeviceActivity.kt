package se.ju.student.robomow

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.core.app.ActivityCompat

class PairNewDeviceActivity : AppCompatActivity() {
    private var adapter: ArrayAdapter<String>? = null
    private var newDevices = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_new_device)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            newDevices
        )
        val newDeviceList = findViewById<ListView>(R.id.new_device_list)
        newDeviceList.adapter = adapter

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions, 0)
        val bluetoothAdapter: BluetoothAdapter? =
            getSystemService(BluetoothManager::class.java).adapter
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
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    try {
                        newDevices.add(device?.name + "\n" + device?.address) //Name sometimes null
                        adapter?.notifyDataSetChanged()
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
