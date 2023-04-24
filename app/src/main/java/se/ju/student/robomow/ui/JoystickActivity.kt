package se.ju.student.robomow.ui
import android.app.ProgressDialog
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import se.ju.student.robomow.BluetoothClient
import se.ju.student.robomow.R

class JoystickActivity : AppCompatActivity() {

    private lateinit var bluetoothClient: BluetoothClient
    private lateinit var readMessageJob: Job
    private lateinit var progressDialog: ProgressDialog
    private val mainScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick)

        val button = findViewById<Button>(R.id.send_button)
        val device: BluetoothDevice? = intent.getParcelableExtra("device")
        bluetoothClient = BluetoothClient(device!!)
        mainScope.launch {
            progressDialog.show()
            if (bluetoothClient.connect()) {
                progressDialog.dismiss()
                Toast.makeText(this@JoystickActivity, "Connected to the device", Toast.LENGTH_SHORT).show()
            } else {
                progressDialog.dismiss()
                Toast.makeText(this@JoystickActivity, "Failed to connect to the device", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Initialize the progress dialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Connecting...")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }


        button.setOnClickListener {
            bluetoothClient.sendMessage("10")
        }

        // (TEMPORARY) Coroutine Polling for received messages from the socket server
        readMessageJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (isActive) {
                    val message = bluetoothClient.readMessage()
                    if (message != null) {
                        runOnUiThread {
                            Toast.makeText(this@JoystickActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    delay(500)
                }
            } catch (e: Exception) {
                Log.e("JoystickActivity", "Error in readMessageJob", e)
                runOnUiThread {
                    Toast.makeText(this@JoystickActivity, "Error reading message: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        readMessageJob.cancel()
        mainScope.cancel()
        bluetoothClient.disconnect()
    }
}