package jon.test

import com.graphhopper.reader.dem.SRTMProvider
import com.graphhopper.reader.osm.GraphHopperOSM
//import com.graphhopper.routing.util.DefaultEdgeFilter
import com.graphhopper.routing.util.EncodingManager

object GhWrapper {

    fun initGH(): ControlSiteFinder  {
        val osmFile = "planet_-1.273,52.977_-1.174,53.023.osm.pbf"

        val gh =  GraphHopperOSM()

        gh.osmFile = osmFile
        // where to store graphhopper files?
        gh.graphHopperLocation = "grph_testit"
        gh.isCHEnabled = false
        gh.setElevation(true)
        gh.elevationProvider = SRTMProvider()
        val em =  EncodingManager("foot")
        gh.encodingManager = em
        gh.setEnableCalcPoints(true)

        gh.importOrLoad()

       // val edgeFilter =  DefaultEdgeFilter(em.getEncoder("foot"))

        return ControlSiteFinder(gh)
    }

}