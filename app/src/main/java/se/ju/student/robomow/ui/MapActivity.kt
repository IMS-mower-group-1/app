package se.ju.student.robomow.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.ju.student.robomow.R
import se.ju.student.robomow.api.RoboMowApi
import se.ju.student.robomow.ui.view.MapView
import se.ju.student.robomow.model.AvoidedCollisions
import se.ju.student.robomow.model.MowSession
import javax.inject.Inject

@AndroidEntryPoint

class MapActivity : AppCompatActivity(), MapView.CollisionAvoidanceListener {
    @Inject
    lateinit var roboMowApi: RoboMowApi
    private lateinit var mapView: MapView

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

    override fun collisionAvoidancePressed(collision: AvoidedCollisions) {
        GlobalScope.launch(Dispatchers.IO) {
            val response = roboMowApi.getImageUrl(collision.imageLink)
            val collisionAvoidanceImage = response.body()
            val imageFragment = CollisionAvoidanceImageFragment().apply {
                arguments = Bundle().apply {
                    putString(CollisionAvoidanceImageFragment.ARG_IMAGE_URL, collisionAvoidanceImage?.imageURL)
                }
            }
            imageFragment.show(supportFragmentManager, "image_dialog")
        }
    }
}