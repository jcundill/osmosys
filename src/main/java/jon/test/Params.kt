package jon.test

import com.graphhopper.util.shapes.GHPoint

data class Params(val distance: Double = 6000.0, val points: Int = 6, val start: GHPoint) {
    val maxWidth = 0.04187945854565234 * 0.99
    val maxHeight = 0.01638736589702461 * 0.99
}