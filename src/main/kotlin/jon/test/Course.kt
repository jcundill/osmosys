package jon.test

import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
import jon.test.gpx.GpxWriter
import java.io.FileInputStream
import java.util.*

fun String.toGHPoint(): GHPoint? {
    val arr = this.split(',')
    return when (arr.size) {
        2 -> GHPoint(arr[0].toDouble(), arr[1].toDouble())
        else -> null
    }
}


data class Course(
        val requestedDistance: Double = 6500.0,
        val requestedNumControls: Int = 6,
        val controls: List<GHPoint> = emptyList()) {

    lateinit var numberedControlScores: List<Double>
    lateinit var featureScores: Map<String, List<Double>>
    var energy: Double = 1000.0
    lateinit var route: PathWrapper


    companion object {

        fun buildFromProperties(filename: String): Course {
            val props = Properties()
            props.load(FileInputStream(filename))
            val waypoints = props.getProperty("controls")
            val distance = props.getProperty("distance", "6000").toDouble()
            val numControls = props.getProperty("numControls", "10").toInt()
            val initials = when (waypoints) {
                null -> emptyList()
                else -> waypoints.split("|").map {
                    it.toGHPoint()!!
                }
            }

            return Course(controls = initials, requestedDistance = distance, requestedNumControls = numControls)
        }

        fun buildFromGPX(filename: String): Course {
            val gpx = GpxWriter()
            val points = gpx.readFromFile(filename)
            val numControls = points.size - 2

            return Course(controls = points, requestedNumControls = numControls)
        }
    }
}