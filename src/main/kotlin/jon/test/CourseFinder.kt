package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.shapes.GHPoint
import jon.test.annealing.Problem
import jon.test.constraints.CourseConstraint

class CourseFinder(
        private val csf: ControlSiteFinder,
        private val constraints: List<CourseConstraint>,
        private val scorer: CourseScorer,
        private val initialCourse: List<GHPoint>) : Problem<CourseImprover> {

    var bad = 0

    override fun initialState(): CourseImprover = CourseImprover(csf, initialCourse)

    override fun energy(searchState: CourseImprover): Double {
        val courseRoute = csf.routeRequest(GHRequest(searchState.controls))
        val score =
                if (constraints.any { !it.valid(courseRoute) }) 10000.0
                else scorer.score(searchState, courseRoute) * 1000

        if (score > 1000.0) bad++
        return score
    }


}
