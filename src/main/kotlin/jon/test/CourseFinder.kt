package jon.test

import com.graphhopper.GHRequest
import jon.test.annealing.Problem
import jon.test.constraints.CourseConstraint

class CourseFinder(
        private val csf: ControlSiteFinder,
        private val constraints: List<CourseConstraint>,
        private val scorer: CourseScorer,
        private val initialCourse: Course) : Problem<CourseImprover> {

    override fun initialState(): CourseImprover = CourseImprover(csf, initialCourse)

    override fun energy(searchState: CourseImprover): Double {
        with(searchState) {
            val courseRoute = csf.routeRequest(GHRequest(course.controls))
            course.route = courseRoute.best
            val score =
                    if (constraints.any { !it.valid(courseRoute) }) 10000.0
                    else scorer.score(course) * 1000

            course.energy = score
            course.route = courseRoute.best
        }
        return searchState.course.energy
    }


}
