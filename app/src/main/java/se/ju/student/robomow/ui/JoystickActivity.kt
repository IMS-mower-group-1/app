package se.ju.student.robomow.ui
import android.app.ProgressDialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import se.ju.student.robomow.BluetoothClient
import se.ju.student.robomow.BluetoothClientHolder
import se.ju.student.robomow.R
import se.ju.student.robomow.RoboMowApplication
import se.ju.student.robomow.ui.view.JoystickView
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.round

class JoystickActivity : AppCompatActivity(), JoystickView.JoystickListener {

    private lateinit var joystickView: JoystickView
    private var bluetoothClient: BluetoothClient? = null

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