package jon.test

import com.vividsolutions.jts.geom.Envelope

class MapFitter(val possibleBoxes: List<MapBox>) {

    fun getForEnvelope(env: Envelope): MapBox? =
            possibleBoxes.find { env.width < it.maxWidth && env.height < it.maxHeight }

}