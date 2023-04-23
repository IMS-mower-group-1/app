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
import se.ju.student.robomow.ui.view.JoystickView
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

class JoystickActivity : AppCompatActivity(), JoystickView.JoystickListener {

    private lateinit var joystickView: JoystickView
    private lateinit var bluetoothClient: BluetoothClient
    private lateinit var progressDialog: ProgressDialog
    private val mainScope = CoroutineScope(Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick)
        joystickView = findViewById(R.id.joystick)
        joystickView.joystickListener = this

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
    }

    override fun onJoystickMoved(angle: Double, speed: Float) {
        val roundedAngle = round(angle * 100) / 100
        val roundedSpeed = round(speed * 100) / 100
        bluetoothClient.sendMessage("${roundedAngle},${roundedSpeed}\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        bluetoothClient.disconnect()
    }
}