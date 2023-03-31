package se.ju.student.robomow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class PreviouslyPairedDeviceListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_previously_paired_device_list)

        val pairNewDeviceButton = findViewById<Button>(R.id.pair_new_button)
        pairNewDeviceButton.setOnClickListener{
            val intent = Intent(this, PairNewDeviceActivity::class.java)
            startActivity(intent)
        }
    }
}