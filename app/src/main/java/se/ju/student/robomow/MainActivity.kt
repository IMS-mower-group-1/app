package se.ju.student.robomow

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import se.ju.student.robomow.model.QuoteList
import se.ju.student.robomow.service.RoboMowApiService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Example code to test async API call
        val roboMowApi = RoboMowApiService()
        GlobalScope.launch(Dispatchers.IO) {
            val response = roboMowApi.getMowSessions()
            if (response.isSuccessful){
                Log.d("Response body:", response.body().toString())
                val body: QuoteList? = response.body()
                if (body is QuoteList){
                    val results: List<se.ju.student.robomow.model.Result> = body.results
                    results.forEach { result ->
                        Log.d("Q", result.content)
                    }
                }
            }

        }

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth:", "Not supported on device")
        }
        requestPermission()

        val connectButton = findViewById<Button>(R.id.connect_button)
        connectButton.setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivity(intent)
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