package se.ju.student.robomow.ui

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import se.ju.student.robomow.R
import se.ju.student.robomow.ui.view.MapView
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import se.ju.student.robomow.model.MowSession

class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.map_view)

        val mowSession = getMowSession()

        mapView.setCoordinates(mowSession?.path)
    }

    private fun getMowSession(): MowSession? {
        val mowSession = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("MOW_SESSION", MowSession::class.java)
        } else {
            intent.getParcelableExtra("MOW_SESSION")
        }
        return mowSession
    }

    data class PathData(
        @SerializedName("path")
        val path: List<Coordinate>
    )

    data class Coordinate(
        @SerializedName("x")
        val x: Int,

        @SerializedName("y")
        val y: Int
    )

    private fun parseJson(json: String): PathData {
        val gson = Gson()
        return gson.fromJson(json, PathData::class.java)
    }
}