package jon.test

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

data class CourseParameters(
        val distance: Double? = null,
        val numControls: Int = 6,
        private val start: GHPoint,
        private val finish: GHPoint = start,
        private val initialPoints: List<GHPoint> = emptyList()) {


    val givenCourse: List<GHPoint>
        get() {
            return listOf(start) + initialPoints + finish
        }

    companion object {

        fun buildFromProperties(filename: String): CourseParameters {
            val props = Properties()
            props.load(FileInputStream(filename))
            val start = props.getProperty("start").toGHPoint()
            val finish = props.getProperty("finish", props.getProperty("start")).toGHPoint()
            val waypoints = props.getProperty("waypoints")
            val distance = props.getProperty("distance", "6000").toDouble()
            val numControls = props.getProperty("numControls", "10").toInt()
            val initials = when (waypoints) {
                null -> emptyList()
                else -> waypoints.split("|").map {
                    it.toGHPoint()!!
                }
            }

            return CourseParameters(start = start!!, finish = finish!!, initialPoints = initials, distance = distance, numControls = numControls)
        }

        fun buildFromGPX(filename: String): CourseParameters {
            val gpx = GpxWriter()
            val points = gpx.readFromFile(filename)
            val numControls = points.size - 2
            val start = points.first()
            val finish = points.last()
            val initials = points.drop(1).dropLast(1)

            return CourseParameters(start = start, finish = finish, initialPoints = initials, numControls = numControls)
        }
    }
}