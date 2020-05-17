
## Osmosys - OpenStreetMap Orienteering system

Use goal based searching to generate passable Urban Orienteering Courses.

### Overview 
Works as follows:

Uses the Graphhopper routing engine - https://github.com/graphhopper/graphhopper/ - to calculate runnable routes between arbitrary locations. 
Where permitted routes are limited to those you could legally and safely run along. So no motorways, private service roads etc.

System is given a starting location, an approximate length to make the
generated course and the number of controls to place on the course.

Then an initial course is seeded from these inputs, and the algorithm iteratively tries
to improve the placement of the controls on the course to make the legs between them
more interesting as an orienteering course.

This is done 1000 times, which takes around 20secs on my Macbook, then the following artifacts are created:
+ gpx file of the course and optimal path around it
+ Open Orienteering Map showing the course
+ KMZ and KML files of the course for uploading to MapRun

As we are routing legs along the street, road, footpath and track data in OSM, the quality of the generated course is largely 
constrained by the number, structure and layout of these features in the area surrounding the start location. So you tend to get a better result in areas like
town centres and housing estates than you do out in more rural locations with limited available route choices.

### Course Improving

System uses a Simulated Annealing approach - https://en.wikipedia.org/wiki/Simulated_annealing - to improving the control locations.

At each iteration, the legs between the controls are scored using the following set of factors:
+ Leg Route Choice - did the routing engine suggest alternative routes for this leg, and if so how dissimilar are they
+ Leg Complexity - how many decisions about turn left, turn right, etc are there on this leg
+ Leg Length - is this leg just too short or too long
+ Dog Leg - is this just coming back the same way we just went
+ Been This Way Before - how much of the leg to this numbered control has been travelled along already
+ Coming Back Here Later - do we run close to a future control on this leg
+ Only Go To The Finish At The End - primarily for MapRun, don't accidentally trigger the finish part way through the course
+ Last Control Is Near The Finish - don't make the run in annoyingly long
+ Didn't Move - this leg is so short the controls are basically in the same place

The system then selects a number of the worst scoring legs and randomly picks an alternative location
for the control at the end of that leg. For the last leg, the last control rather than the finish location is replaced.

These replacements are fed into the Annealing solver which re-scores the resulting course and either accepts
or rejects this new course depending on both the overall scores for both the old and the
new courses and the amount of energy still remaining in the Solver.

If rejected new alternatives for the worst scoring legs are re-evaluated.
If accepted the Solver moves on to look at the worst scoring legs in this new course.

Annealing continues for 1000 iterations.

Each time the course is improved we defensively run a Travelling Salesman solver over it to makes sure that
control ordering remains fairly sensible.

In addition to the improvers, there are an number of hard constraints that the course must always satisfy:
+ Course Length - must be reasonably similar to the requested course length
+ Is Routeable - there must exist at least one safely runnable route around the whole course
+ Printable On Map - Is it possible to fit the course onto either a Landscape or Portrait A4 map at 5000, 7500, 10000, 12500 or 15000 scale


### Running it locally

It's a java based piece of Software, primarily because the Graphhopper routing engine that does a lot of the heavy lifting is written in Java.

The codebase itself is Kotlin - https://kotlinlang.org/ - sorry about that I mostly
wrote this code as an excuse to learn Kotlin properly. Still could be worse I started off writing it in Scala.

The GPX library used in not in mvn

mvn install:install-file -Dfile=lib/gpxparser-20130603.jar -DgroupId=jon.gpx -DartifactId=jon.gpx.gpxparser -Dversion=1.0 -Dpackaging=jar

need to download the openstreetmap protocol buffer file england-latest.osm.pbf - or choose a smaller one

https://download.geofabrik.de/europe/great-britain/england.html

This needs to go into a folder called extracts - see initGH in GhWrapper for details

    fun initGH(name: String): ControlSiteFinder {

        val gh = GraphHopperOSM().apply {
            forServer()
            osmFile = "extracts/$name.osm.pbf"
            graphHopperLocation = "osm_data/grph_$name"
            isCHEnabled = false
            setElevation(true)
            elevationProvider = SRTMProvider()
            encodingManager = EncodingManager.create(oFlagEncoder)
            setEnableCalcPoints(true)
        }

        gh.importOrLoad()
        return ControlSiteFinder(gh)
    }


 mvn clean package

