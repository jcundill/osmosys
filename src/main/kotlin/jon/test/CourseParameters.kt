package jon.test

import com.graphhopper.util.DistancePlaneProjection
import com.graphhopper.util.shapes.GHPoint

data class MapBox(val maxWidth: Double, val maxHeight: Double, val scale: Int) {
    val dist2d = DistancePlaneProjection()
    val widthInMetres = dist2d.calcDist(0.0, 0.0, 0.0, this.maxWidth)
    val heightInMetres = dist2d.calcDist(0.0, 0.0, this.maxHeight, 0.0)
}


data class CourseParameters(
        val distance: Double = 6000.0,
        private val allowedLengthDelta:Double = 0.2,
        val numControls: Int = 6,
        val minControlSeparation: Double = 50.0,
        val start: GHPoint,
        val finish: GHPoint = start) {

    val dist2d = DistancePlaneProjection()

    val landscape = MapBox(
            maxWidth = 0.04187945854565234 * 0.99,
            maxHeight = 0.01638736589702461 * 0.99,
            scale = 10000
    )

    val portrait = MapBox(
            maxWidth = 0.02955457284753238 * 0.99,
            maxHeight = 0.024208663288803223 * 0.99,
            scale = 10000
    )

    val landscape125 = MapBox(
            maxWidth = landscape.maxWidth * 1.25,
            maxHeight = landscape.maxHeight * 1.25,
            scale = 12500
    )

    val portrait125 = MapBox(
            maxWidth = portrait.maxWidth * 1.25,
            maxHeight = portrait.maxHeight * 1.25,
            scale = 12500
    )

    val landscape150 = MapBox(
            maxWidth = landscape.maxWidth * 1.5,
            maxHeight = landscape.maxHeight * 1.5,
            scale = 15000
    )

    val portrait150 = MapBox(
            maxWidth = portrait.maxWidth * 1.5,
            maxHeight = portrait.maxHeight * 1.5,
            scale = 15000
    )

    val boxRadius = dist2d.calcDist(0.0, 0.0, landscape.maxHeight, 0.0) / 2.0

    val allowedBoxes = listOf(portrait, landscape, portrait125, landscape125, portrait150, landscape150)


    val minAllowedDistance = distance - distance * allowedLengthDelta
    val maxAllowedDistance = distance + distance * allowedLengthDelta

    val minLegLength = 20.0
    val maxLegLength = distance / numControls * 2.0

}