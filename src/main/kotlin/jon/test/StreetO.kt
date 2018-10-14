package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import jon.test.annealing.InfeasibleProblemException
import jon.test.annealing.LinearDecayScheduler
import jon.test.annealing.Solver
import jon.test.constraints.CourseLengthConstraint
import jon.test.constraints.IsRouteableConstraint
import jon.test.constraints.PrintableOnMapConstraint
import jon.test.mapping.MapFitter
import jon.test.scorers.*


class StreetO(db: String) {
    private val featureScorers = listOf(
            LegLengthScorer(),
            LegRouteChoiceScorer(),
            LegComplexityScorer(),
            BeenThisWayBeforeScorer(),
            ComingBackHereLaterScorer(),
            DidntMoveScorer(),
            LastControlNearTheFinishScorer(),
            DogLegScorer()
    )

    private val csf = GHWrapper.initGH(db)
    private val scorer = CourseScorer(featureScorers, csf::findRoutes)
    private val seeder = CourseSeeder(csf)
    private val fitter = MapFitter()

    fun makeProblem(params: CourseParameters): CourseFinder {
        val seedCourse = seeder.chooseInitialPoints(params.givenCourse, params.numControls, params.distance)

        val distance = when {
            params.distance == null -> findBestRoute(seedCourse).distance * 0.8 // no desired distance given, make it about as long as it is now
            else -> params.distance
        }
        val constraints = listOf(
                IsRouteableConstraint(),
                CourseLengthConstraint(distance),
                PrintableOnMapConstraint(fitter)
        )

        return CourseFinder(csf, constraints, scorer, seedCourse)
    }

    fun findCourse(problem: CourseFinder, iterations: Int = 1000): CourseImprover? {

        val solver = Solver(problem, LinearDecayScheduler(1000.0, iterations))
        return try {
            solver.solve()
        } catch (e: InfeasibleProblemException) {
            println(e.message ?: "All gone badly wrong")
            null
        }
    }

    fun findBestRoute(controls: List<GHPoint>): PathWrapper = csf.routeRequest(GHRequest(controls)).best


    fun getEnvelopeForProbableRoutes(controls: List<GHPoint>): Envelope {
        val routes = controls.windowed(2).flatMap {
            val req = GHRequest(it.first(), it.last())
            csf.routeRequest(req, 3).all
        }

        val env = Envelope()
        routes.forEach { it.points.forEach { p -> env.expandToInclude(p.lon, p.lat) } }
        return env
    }

    fun getDetailedScores(featureScores: List<List<Double>>): List<Pair<String, List<Double>>> {
        return featureScorers.map {
            it::class.java.simpleName
        }.zip(featureScores)
    }
}