package se.ju.student.robomow

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class PairNewDeviceActivity : AppCompatActivity() {
    private lateinit var viewModel: PairNewDeviceViewModel
    private lateinit var adapter: ArrayAdapter<BluetoothDevice>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_new_device)

        viewModel = ViewModelProvider(this)[PairNewDeviceViewModel::class.java]
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            mutableListOf<BluetoothDevice>()
        )
        val newDeviceList = findViewById<ListView>(R.id.new_device_list)
        newDeviceList.adapter = adapter

        registerReceiver(
            viewModel.getReceiver(),
            viewModel.getIntentFilter()
        )

        viewModel.newDevices.observe(this) {
            adapter.clear()
            adapter.addAll(it)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startDiscovery(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(viewModel.getReceiver())
    }
}
