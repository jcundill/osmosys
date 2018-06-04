package jon.test

//import com.graphhopper.routing.util.DefaultEdgeFilter
import com.graphhopper.reader.dem.SRTMProvider
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.util.EncodingManager


object GhWrapper {

    fun initGH(name: String): ControlSiteFinder {
        val osmFile = "extracts/$name.osm.pbf"

        val gh = GraphHopperOSM()

        gh.osmFile = osmFile
        // where to store graphhopper files?
        gh.graphHopperLocation = "osm_data/grph_$name"
        gh.isCHEnabled = false
        gh.setElevation(true)
        gh.elevationProvider = SRTMProvider()
        val em = EncodingManager(StreetOFlagEncoder())
        gh.encodingManager = em
        gh.setEnableCalcPoints(true)

        gh.importOrLoad()

        // val edgeFilter =  DefaultEdgeFilter(em.getEncoder("foot"))

        return ControlSiteFinder(gh)
    }

}