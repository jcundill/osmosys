package jon.test.constraints

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper
import com.vividsolutions.jts.geom.Envelope
import jon.test.CourseParameters
import jon.test.MapFitter

class PrintableOnMapConstraint(val params: CourseParameters) : CourseConstraint {
    val env = Envelope()
    val fitter = MapFitter(params.allowedBoxes)

    override fun valid(routedCourse: GHResponse): Boolean {
        return routeFitsBox(routedCourse.all)
    }

    fun routeFitsBox(routes: List<PathWrapper>): Boolean {
        env.setToNull()
        routes.forEach { pw -> pw.points.forEach { env.expandToInclude(it.lon, it.lat) } }
        return fitter.getForEnvelope(env) != null
    }


}