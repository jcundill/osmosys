package jon.test

import com.graphhopper.GHRequest
import com.graphhopper.util.PointList
import com.graphhopper.util.shapes.GHPoint
import xyz.thepathfinder.simulatedannealing.Problem

class CourseFinder(private val csf: ControlSiteFinder, val scorer:CourseScorer, private val params: CourseParameters) : Problem<CourseImprover> {

    var bad = 0

    override fun initialState(): CourseImprover = CourseImprover(csf, chooseInitialPoints(params.start))

    override fun energy(step: CourseImprover?): Double {
        val score = scoreStep(step)
        if (score > 1000.0) bad++
        return score
    }

    private fun scoreStep(step: CourseImprover?): Double {
        return when {
            step === null -> 10000.0
            else -> {
                val response = csf.routeRequest(GHRequest(step.controls))
                when {
                    response.hasErrors() -> 10000.0
                    !csf.routeFitsBox(response.best.points, params.allowedBoxes) -> 10000.0
                    else -> {
                        val ans = scorer.score(step) * 1000
                        println(ans)
                        ans
                    }
                }
            }
        }
    }


    private tailrec fun findMappableControlSite(seed: List<GHPoint> ): GHPoint{
        val possible = csf.findControlSiteNear(seed.last(), 500.0)
        val pl = seed.fold(PointList(), {acc, pt -> acc.add(pt); acc})
        pl.add(possible)
        return if (csf.routeFitsBox(pl, params.allowedBoxes)) possible
        else findMappableControlSite(seed)
    }

    private fun chooseInitialPoints(near: GHPoint): List<GHPoint> {

        val seq = generateSequence(listOf(near)) { it + findMappableControlSite(it) }
        return seq.take(params.points - 1).last() + near

    }


}
