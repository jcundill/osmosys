


The GPX library used in not in mvn

mvn install:install-file -Dfile=lib/gpxparser-20130603.jar -DgroupId=jon.gpx -DartifactId=jon.gpx.gpxparser -Dversion=1.0 -Dpackaging=jar

need to download the openstreetmap protocol buffer file england-latest.osm.pbf - or choose a smaller one

https://download.geofabrik.de/europe/great-britain/england.html

This needs to go into a folder called extracts - see initGH in GhWrapper for details

    fun initGH(name: String): ControlSiteFinder {
        val osmFile = "extracts/$name.osm.pbf"

        val gh = GraphHopperOSM()

        gh.osmFile = osmFile
        // where to store graphhopper files?
        gh.graphHopperLocation = "osm_data/grph_$name"
        gh.isCHEnabled = false // Contraction Hierarchies - if we enable this its faster but we don't get alt routes
        gh.setElevation(true) // use evelation data
        gh.elevationProvider = SRTMProvider()
        val em = EncodingManager(StreetOFlagEncoder()) //StreetO Flag Encoder is ours - minor changes on FootFlagEncoder
        gh.encodingManager = em
        gh.setEnableCalcPoints(true) // calculate GPS points as well as dist - not sure if we actually need this

        gh.importOrLoad() // for england latest takes about 10mins to initial import

        // val edgeFilter =  DefaultEdgeFilter(em.getEncoder("foot"))

        return ControlSiteFinder(gh)
    }

 mvn clean package

