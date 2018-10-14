package jon.test

import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import jon.test.annealing.InfeasibleProblemException
import jon.test.improvers.dist2d
import jon.test.mapping.MapFitter

class CourseSeeder(private val csf: ControlSiteFinder) {

    private val fitter = MapFitter()

    fun chooseInitialPoints(initialPoints: List<GHPoint>, requestedNumControls: Int, requestedCourseLength: Double?): List<GHPoint> {

        val env = Envelope()

        // check that everything we have been given is mappable
        val startPoint = csf.findNearestControlSiteTo(initialPoints.first())
                ?: throw InfeasibleProblemException("no control point near the start")

        val finishPoint = csf.findNearestControlSiteTo(initialPoints.last())
                ?: throw InfeasibleProblemException("no control point near the finish")

        val chosenControls = initialPoints.drop(1).dropLast(1).map {
            csf.findNearestControlSiteTo(it)
                    ?: throw InfeasibleProblemException("no control point near the waypoint $it")
        }

        with(env) {
            expandToInclude(startPoint.lon, startPoint.lat)
            expandToInclude(finishPoint.lon, finishPoint.lat)
            chosenControls.forEach {
                expandToInclude(it.lon, it.lat)
            }
        }

        if (!canBeMapped(env)) {
            throw InfeasibleProblemException("initial course cannot be mapped")
        }

        // ok, so everything we have been given can be mapped, so add in a number of generated controls and return that
        val numToGenerate = requestedNumControls - chosenControls.size
        return listOf(startPoint) + when (numToGenerate) {
            0 -> emptyList()
            else -> generateControls(numToGenerate, requestedCourseLength!!, env)
        } + chosenControls + finishPoint


    }

    private fun canBeMapped(env: Envelope) =
            fitter.getForEnvelope(env) != null


    private fun generateControls(numControls: Int, distance: Double, env: Envelope): List<GHPoint> {

        val bearing = csf.randomBearing
        val angle = (2 * Math.PI) / numControls

        val envCentre = GHPoint(env.centre().y, env.centre().x)

        // if the env is really small (only a start perhaps)
        // treat it as being on the radius of the circle
        // otherwise build an initial circle around its centre
        val w = dist2d.calcDist(env.minY, env.minX, env.maxY, env.maxX)

        val fudgeFactor = 5.0 / numControls
        val radius = fudgeFactor * distance / (2 * Math.PI)
        val circleCentre = when {
            w < 1000 -> csf.getCoords(envCentre, Math.PI + bearing, radius)
            else -> envCentre
        }

        val positions = (1..numControls).map { num ->
            csf.getCoords(circleCentre, (num * angle) + bearing, radius)
        }
        return positions.map { csf.findControlSiteNear(it, radius / 5.0) }
    }

}