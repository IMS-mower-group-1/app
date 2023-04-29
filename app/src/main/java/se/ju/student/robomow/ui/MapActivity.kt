package se.ju.student.robomow.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import se.ju.student.robomow.R
import se.ju.student.robomow.ui.view.MapView
import se.ju.student.robomow.model.AvoidedCollisions
import se.ju.student.robomow.model.MowSession

class MapActivity : AppCompatActivity(), MapView.CollisionAvoidanceListener {
    private lateinit var mapView: MapView
    override fun collisionAvoidancePressed(collision: AvoidedCollisions) {
        
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.map_view)
        mapView.listener = this
        val mowSession = getMowSession()
        mapView.setCoordinates(mowSession?.path, mowSession?.avoidedCollisions)
    }

    private fun getMowSession(): MowSession? {
        val mowSession = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("MOW_SESSION", MowSession::class.java)
        } else {
            intent.getParcelableExtra("MOW_SESSION")
        }
        return mowSession
    }
}