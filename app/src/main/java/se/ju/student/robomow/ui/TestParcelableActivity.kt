package se.ju.student.robomow.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import se.ju.student.robomow.R
import se.ju.student.robomow.model.MowSession

class TestParcelableActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_parcelable)
        val mowSession = intent.getParcelableExtra<MowSession>("MOW_SESSION")
        for (position in mowSession!!.path){
            println(position)
        }
    }
}