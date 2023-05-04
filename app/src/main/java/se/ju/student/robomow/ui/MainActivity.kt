package se.ju.student.robomow.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import se.ju.student.robomow.BluetoothClient
import se.ju.student.robomow.BluetoothClientHolder
import se.ju.student.robomow.R
import se.ju.student.robomow.api.RoboMowApi
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var roboMowApi: RoboMowApi
    private val bluetoothClient: BluetoothClient?
        get() = BluetoothClientHolder.bluetoothClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth:", "Not supported on device")
        }
        requestPermission()

        val connectButton = findViewById<Button>(R.id.connect_button)
        connectButton.setOnClickListener {
            Intent(this, DeviceListActivity::class.java).also {
                startActivity(it)
            }
        }

        val controlButton = findViewById<Button>(R.id.control_button)
        controlButton.setOnClickListener {
            if (bluetoothClient == null) {
                Toast.makeText(this, "Connect to a mower to control it", Toast.LENGTH_SHORT).show()
            } else {
                bluetoothClient!!.sendMessage("TAKE_CONTROL")
                Intent(this, JoystickActivity::class.java).also {
                    startActivity(it)
                }
            }
        }


        val routesButton = findViewById<Button>(R.id.route_button)
        routesButton.setOnClickListener {
            Intent(this, MowSessionsActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
            } else {
                //deny
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }
}