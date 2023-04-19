package se.ju.student.robomow.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import se.ju.student.robomow.R
import se.ju.student.robomow.ui.view.MapView
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.map_view)

        val jsonString = """{
            "path": [
                {
                    "y": 0,
                    "x": 0
                },
                {
                    "y": 1,
                    "x": 0
                },
                {
                    "y": 2,
                    "x": 0
                },
                {
                    "x": 1,
                    "y": 2
                },
                {
                    "x": 2,
                    "y": 2
                },
                {
                    "y": 3,
                    "x": 2
                },
                {
                    "y": 4,
                    "x": 2
                },
                {
                    "x": 2,
                    "y": 5
                },
                {
                    "x": 2,
                    "y": 6
                }
            ]
        }"""
        val pathData = parseJson(jsonString)
        mapView.setCoordinates(pathData.path)
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