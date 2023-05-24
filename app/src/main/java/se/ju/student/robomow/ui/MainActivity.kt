package se.ju.student.robomow.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private lateinit var connectionStatusImage: ImageView
    private lateinit var connectionStatusText: TextView
    private lateinit var connectButton: Button

    private fun handleBluetoothConnectionLost() {
        connectButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_dark))
        connectButton.text = "Connect"

        connectionStatusImage.setImageAlpha(51) // Set opacity to 20%
        connectionStatusText.text = "Disconnected"
        connectionStatusText.setTextColor(ContextCompat.getColor(this, R.color.warning_red))
    }

    private fun handleBluetoothConnectionEstablished() {
        connectButton.setBackgroundColor(ContextCompat.getColor(this, R.color.warning_red))
        connectButton.text = "Disconnect"

        connectionStatusImage.setImageAlpha(255) // Set opacity to 100%
        connectionStatusText.text = "Connected"
        connectionStatusText.setTextColor(ContextCompat.getColor(this, R.color.success_green))
    }

    override fun onResume() {
        super.onResume()
        BluetoothClientHolder.connectionStatus?.onEach { connected ->
            if (connected == true) {
                handleBluetoothConnectionEstablished()
            } else {
                handleBluetoothConnectionLost()
            }
        }?.launchIn(lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectionStatusImage = findViewById(R.id.connection_status_image)
        connectionStatusText = findViewById(R.id.connection_status_text)
        connectButton = findViewById(R.id.connect_button)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth:", "Not supported on device")
        }
        requestPermission()

        connectButton.setOnClickListener {
            if(connectButton.text == "Connect"){
                Intent(this, DeviceListActivity::class.java).also {
                    startActivity(it)
                }
            } else {
                BluetoothClientHolder.disconnect()
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

        val startSessionButton = findViewById<Button>(R.id.start_session_button)
        startSessionButton.setOnClickListener {
            if (bluetoothClient == null) {
                Toast.makeText(this, "Connect to a mower to start its session", Toast.LENGTH_SHORT).show()
            } else {
                bluetoothClient!!.sendMessage("START_SESSION")
            }
        }

        val endSessionButton = findViewById<Button>(R.id.end_session_button)
        endSessionButton.setOnClickListener {
            if (bluetoothClient == null) {
                Toast.makeText(this, "Connect to a mower to end its session", Toast.LENGTH_SHORT).show()
            } else {
                bluetoothClient!!.sendMessage("END_SESSION")
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
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