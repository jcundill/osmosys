package jon.test

import com.graphhopper.util.shapes.GHPoint

data class MapBox(val maxWidth: Double, val maxHeight: Double)

data class CourseParameters(
        val distance: Double = 6000.0,
        private val allowedLengthDelta:Double = 0.2,
        val points: Int = 6,
        val minControlSeparation: Double = 50.0,
        val start: GHPoint) {

    val landscape = MapBox(
            maxWidth = 0.04187945854565234 * 0.99,
            maxHeight = 0.01638736589702461 * 0.99
    )

    val portrait = MapBox(
            maxWidth = 0.02955457284753238 * 0.99,
            maxHeight = 0.024208663288803223 * 0.99
    )

    val allowedBoxes = listOf(portrait, landscape)


    val minAllowedDistance = distance - distance * allowedLengthDelta

    val maxAllowedDistance = distance + distance * allowedLengthDelta
}