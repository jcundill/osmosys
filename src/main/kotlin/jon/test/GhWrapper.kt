package jon.test

import com.graphhopper.reader.dem.SRTMProvider
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.util.EncodingManager


object GHWrapper {

    fun initGH(name: String): ControlSiteFinder {

        val gh = GraphHopperOSM().apply {
            osmFile = "extracts/$name.osm.pbf"
            graphHopperLocation = "osm_data/grph_$name"
            isCHEnabled = false
            setElevation(true)
            elevationProvider = SRTMProvider()
            encodingManager = EncodingManager(StreetOFlagEncoder())
            setEnableCalcPoints(true)
        }

        gh.importOrLoad()
        return ControlSiteFinder(gh)
    }

}