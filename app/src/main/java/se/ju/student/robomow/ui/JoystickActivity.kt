package se.ju.student.robomow.ui
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import se.ju.student.robomow.data.BluetoothClient
import se.ju.student.robomow.data.BluetoothClientHolder
import se.ju.student.robomow.R
import se.ju.student.robomow.ui.view.JoystickView
import kotlin.math.round

class JoystickActivity : AppCompatActivity(), JoystickView.JoystickListener {

    private lateinit var joystickView: JoystickView
    private var bluetoothClient: BluetoothClient? = null

    override fun onResume() {
        super.onResume()
        BluetoothClientHolder.connectionStatus?.onEach { connected ->
            if (connected == false) {
                Toast.makeText(this, "Connect to a mower to control it", Toast.LENGTH_SHORT).show()
                finish()
            }
        }?.launchIn(lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick)
        joystickView = findViewById(R.id.joystick)
        joystickView.joystickListener = this

        bluetoothClient = BluetoothClientHolder.bluetoothClient
        if (bluetoothClient == null) {
            Toast.makeText(this, "Bluetooth connection lost. Please try again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val toAutoButton = findViewById<Button>(R.id.auto_button)
        toAutoButton.setOnClickListener {
            bluetoothClient?.sendMessage("AUTO")
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    override fun onJoystickMoved(angle: Double, speed: Float) {
        val roundedAngle = round(angle * 100) / 100
        val roundedSpeed = round(speed * 100) / 100
        bluetoothClient?.sendMessage("${roundedAngle},${roundedSpeed}\n")
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}