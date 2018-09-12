package jon.test.mapping

import com.vividsolutions.jts.geom.Envelope

data class MapBox(val maxWidth: Double, val maxHeight: Double, val scale: Int, val landscape: Boolean)

/**
 * finds, from the available possibilities, the best map scale and orientation that would allow
 * the current course to be printed on a sheet of paper
 */
class MapFitter {

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

    private val possibleBoxes = listOf(
            landscape5000, portrait5000,
            landscape7500, portrait7500,
            landscape10000, portrait10000,
            portrait12500, landscape12500,
            portrait15000, landscape15000
    )

    /**
     * return the MapBox that would best enclose the points in the passed in envelope
     * or null if we don't have one
     */
    fun getForEnvelope(env: Envelope): MapBox? =
            possibleBoxes.find { env.width < it.maxWidth && env.height < it.maxHeight }

}