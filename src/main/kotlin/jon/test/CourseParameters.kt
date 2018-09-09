package jon.test

import com.graphhopper.util.DistancePlaneProjection
import com.graphhopper.util.shapes.GHPoint
import java.io.FileInputStream
import java.util.*

data class MapBox(val maxWidth: Double, val maxHeight: Double, val scale: Int, val landscape: Boolean)


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

    val dist2d = DistancePlaneProjection()

    val landscape10000 = MapBox(
            maxWidth = 0.04187945854565189 * 0.9,
            maxHeight = 0.016405336634527146 * 0.9,
            scale = 10000,
            landscape = true
    )

    val portrait10000 = MapBox(
            maxWidth = 0.02955457284753238 * 0.9,
            maxHeight = 0.024208663288803223 * 0.9,
            scale = 10000,
            landscape = false
    )

    val landscape5000 = MapBox(
            maxWidth = landscape10000.maxWidth * 0.5,
            maxHeight = landscape10000.maxHeight * 0.5,
            scale = 5000,
            landscape = true
    )

    val portrait5000 = MapBox(
            maxWidth = portrait10000.maxWidth * 0.5,
            maxHeight = portrait10000.maxHeight * 0.5,
            scale = 5000,
            landscape = false
    )
    val landscape7500 = MapBox(
            maxWidth = landscape10000.maxWidth * 0.75,
            maxHeight = landscape10000.maxHeight * 0.75,
            scale = 7500,
            landscape = true
    )

    val portrait7500 = MapBox(
            maxWidth = portrait10000.maxWidth * 0.75,
            maxHeight = portrait10000.maxHeight * 0.75,
            scale = 7500,
            landscape = false
    )
    val landscape12500 = MapBox(
            maxWidth = landscape10000.maxWidth * 1.25,
            maxHeight = landscape10000.maxHeight * 1.25,
            scale = 12500,
            landscape = true
    )

    val portrait12500 = MapBox(
            maxWidth = portrait10000.maxWidth * 1.25,
            maxHeight = portrait10000.maxHeight * 1.25,
            scale = 12500,
            landscape = false
    )

    val landscape15000 = MapBox(
            maxWidth = landscape10000.maxWidth * 1.5,
            maxHeight = landscape10000.maxHeight * 1.5,
            scale = 15000,
            landscape = true
    )

    val portrait15000 = MapBox(
            maxWidth = portrait10000.maxWidth * 1.5,
            maxHeight = portrait10000.maxHeight * 1.5,
            scale = 15000,
            landscape = false
    )

    val boxRadius = dist2d.calcDist(0.0, 0.0, landscape10000.maxHeight, 0.0) / 2.0

    val allowedBoxes = listOf(
            landscape5000, portrait5000,
            landscape7500, portrait7500,
            landscape10000, portrait10000,
            portrait12500, landscape12500,
            portrait15000, landscape15000
    )


    val minAllowedDistance = distance - distance * allowedLengthDelta
    val maxAllowedDistance = distance + distance * allowedLengthDelta

    val minLegLength = 20.0
    val maxLegLength = 2.0 * distance / numControls

}