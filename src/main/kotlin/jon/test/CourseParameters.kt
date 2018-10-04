package jon.test

import com.graphhopper.util.shapes.GHPoint
import java.io.FileInputStream
import java.util.*


data class CourseParameters(
        val distance: Double = 6000.0,
        val numControls: Int = 6,
        val start: GHPoint,
        val finish: GHPoint = start) {

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
            return CourseParameters(start = start!!, finish = finish!!, distance = distance, numControls = numControls)
        }
    }
}