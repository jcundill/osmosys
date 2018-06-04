package jon.test.constraints

import com.graphhopper.GHResponse
import com.graphhopper.util.PointList
import com.vividsolutions.jts.geom.Envelope
import jon.test.CourseParameters
import jon.test.MapBox

class PrintableOnMapConstraint(val params: CourseParameters) : CourseConstraint{
    val env = Envelope()

    override fun valid(routedCourse: GHResponse): Boolean {
        return routeFitsBox(routedCourse.best.points, params.allowedBoxes)
    }

    fun routeFitsBox(points: PointList, possibleBoxes: List<MapBox>): Boolean {
        env.setToNull()
        points.forEach { env.expandToInclude(it.lon, it.lat) }
        return possibleBoxes.any {env.width < it.maxWidth && env.height < it.maxHeight}
    }


}