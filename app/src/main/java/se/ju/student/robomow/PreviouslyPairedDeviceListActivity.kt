package se.ju.student.robomow

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts

class PreviouslyPairedDeviceListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_previously_paired_device_list)

        val mDeviceList = ArrayList<String>()
        val pairedDevices: Set<BluetoothDevice>?
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth:", "Not supported on device")
        }
        
        //mDeviceList.add("device.name" + "\n" + "device.address")
        try {
            pairedDevices = bluetoothAdapter?.bondedDevices
            Log.i("Bluetooth:", pairedDevices.toString())
            pairedDevices?.forEach { device ->
                mDeviceList.add(device.name + "\n" + device.address)
            }
        } catch (e: SecurityException) {
            Log.e("Bluetooth:", e.toString())
        }

        val pairedDeviceList = findViewById<ListView>(R.id.paired_device_list)
        pairedDeviceList.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            mDeviceList
        )

        val pairNewDeviceButton = findViewById<Button>(R.id.pair_new_button)
        pairNewDeviceButton.setOnClickListener {
            val intent = Intent(this, PairNewDeviceActivity::class.java)
            startActivity(intent)
        }
    }
}