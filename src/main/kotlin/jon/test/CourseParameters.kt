package jon.test

import com.graphhopper.util.shapes.GHPoint
import java.io.FileInputStream
import java.util.*


data class CourseParameters(
        val distance: Double = 6000.0,
        private val allowedLengthDelta: Double = 0.2,
        val numControls: Int = 6,
        val minControlSeparation: Double = 50.0,
        val start: GHPoint,
        val finish: GHPoint = start,
        val map: String = "england-latest") {

    companion object {
        private fun String.toGHPoint(): GHPoint? {
            val arr = this.split(',')
            return when (arr.size) {
                2 -> GHPoint(arr[0].toDouble(), arr[1].toDouble())
                else -> null
            }
        }

        fun buildFromProperties(filename: String): CourseParameters {
            val props = Properties()
            props.load(FileInputStream(filename))
            val start = props.getProperty("start").toGHPoint()
            val finish = props.getProperty("finish", props.getProperty("start")).toGHPoint()
            val distance = props.getProperty("distance", "6000").toDouble()
            val numControls = props.getProperty("numControls", "10").toInt()
            val map = props.getProperty("map", "england-latest")
            return CourseParameters(start = start!!, finish = finish!!, distance = distance, numControls = numControls, map = map)
        }
    }


    val minAllowedDistance = distance - distance * allowedLengthDelta
    val maxAllowedDistance = distance + distance * allowedLengthDelta

    val minLegLength = 20.0
    val maxLegLength = 2.0 * distance / numControls

}