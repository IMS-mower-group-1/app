package se.ju.student.robomow
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class JoystickActivity : AppCompatActivity() {

    private lateinit var bluetoothClient: BluetoothClient
    private lateinit var readMessageJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick)

        val button = findViewById<Button>(R.id.send_button)
        val device: BluetoothDevice? = intent.getParcelableExtra("device")

        bluetoothClient = BluetoothClient(device!!)
        if (bluetoothClient.connect()) {
            Toast.makeText(this, "Connected to the device", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to connect to the device", Toast.LENGTH_SHORT).show()
            finish()
        }

        button.setOnClickListener {
            bluetoothClient.sendMessage("Button click")
        }

        readMessageJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val message = bluetoothClient.readMessage()
                if (message != null) {
                    runOnUiThread {
                        Toast.makeText(this@JoystickActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    break
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        readMessageJob.cancel()
        bluetoothClient.disconnect()
    }
}