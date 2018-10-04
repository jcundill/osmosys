package jon.test.constraints

import com.graphhopper.GHResponse
import com.graphhopper.PathWrapper
import com.vividsolutions.jts.geom.Envelope
import jon.test.mapping.MapFitter

class PrintableOnMapConstraint(val fitter: MapFitter) : CourseConstraint {
    val env = Envelope()

    override fun valid(routedCourse: GHResponse): Boolean {
        return routeFitsBox(routedCourse.all)
    }

    private fun routeFitsBox(routes: List<PathWrapper>): Boolean {
        env.setToNull()
        routes.forEach { pw -> pw.points.forEach { env.expandToInclude(it.lon, it.lat) } }
        return fitter.getForEnvelope(env) != null
    }


}