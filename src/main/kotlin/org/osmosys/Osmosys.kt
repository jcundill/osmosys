/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.osmosys

import com.graphhopper.GHRequest
import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.BBox
import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Envelope
import org.osmosys.annealing.ExponentialDecayScheduler
import org.osmosys.annealing.InfeasibleProblemException
import org.osmosys.annealing.Solver
import org.osmosys.constraints.CourseLengthConstraint
import org.osmosys.constraints.IsRouteableConstraint
import org.osmosys.constraints.PrintableOnMapConstraint
import org.osmosys.furniture.StreetFurnitureFinder
import org.osmosys.mapping.MapFitter
import org.osmosys.scorers.*


class Osmosys(db: String) {
    private val featureScorers = listOf(
            LegLengthScorer(),
            LegRouteChoiceScorer(),
            LegComplexityScorer(),
            BeenThisWayBeforeScorer(),
            ComingBackHereLaterScorer(),
            OnlyGoToTheFinishAtTheEndScorer(),
            DidntMoveScorer(),
            LastControlNearTheFinishScorer(),
            DogLegScorer()
    )

    val csf = GHWrapper.initGH(db)
    private val scorer = CourseScorer(featureScorers, csf::findRoutes)
    private val seeder = CourseSeeder(csf)
    private val fitter = MapFitter()
    private val finder = StreetFurnitureFinder()

    fun makeProblem(initialCourse: Course): CourseFinder {
        findFurniture(initialCourse.controls[0])

        val seededCourse = initialCourse.copy(controls = seeder.chooseInitialPoints(initialCourse.controls, initialCourse.requestedNumControls, initialCourse.distance()))
        seededCourse.legScores = List(seededCourse.controls.size - 1) { 0.5 }

        val constraints = listOf(
                IsRouteableConstraint(),
                CourseLengthConstraint(initialCourse.distance()),
                PrintableOnMapConstraint(fitter)
        )
        return CourseFinder(csf, constraints, scorer, seededCourse)
    }

    fun findCourse(problem: CourseFinder, iterations: Int = 1000): Course? {
        val solver = Solver(problem, ExponentialDecayScheduler(1000.0, iterations))
        return try {
            solver.solve().course
        } catch (e: InfeasibleProblemException) {
            println(e.message ?: "All gone badly wrong")
            null
        }
    }

    fun findBestRoute(controls: List<ControlSite>): PathWrapper = csf.routeRequest(controls).best


    fun getEnvelopeForProbableRoutes(controls: List<ControlSite>): Envelope {
        val routes = controls.windowed(2).flatMap {
            csf.routeRequest(it, 3).all
        }

        val env = Envelope()
        routes.forEach { it.points.forEach { p -> env.expandToInclude(p.lon, p.lat) } }
        return env
    }

    fun score(course: Course): Course {
        val route = csf.routeRequest(course.controls)
        course.route = route.best
        scorer.score(course)
        return course

    }

    fun evaluateLeg(from: GHPoint, to: GHPoint): List<Pair<Double, List<GHPoint>>> {
        val leg = csf.routeRequest(GHRequest(from, to), 3)
        return leg.all.map{  Pair(it.distance, it.points.toList()) }
    }

    fun findFurniture(start: ControlSite) {
        val scaleFactor = 5000.0
        val max = csf.getCoords(start.position, Math.PI * 0.25, scaleFactor)
        val min = csf.getCoords(start.position, Math.PI * 1.25, scaleFactor)
        val bbox = BBox(min.lon, max.lon, min.lat, max.lat)
        csf.furniture = finder.findForBoundingBox(bbox)
    }
}