package jon.test.gpx

import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
import jon.test.Course
import org.alternativevision.gpx.GPXParser
import org.alternativevision.gpx.beans.GPX
import org.alternativevision.gpx.beans.Route
import org.alternativevision.gpx.beans.Track
import org.alternativevision.gpx.beans.Waypoint
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat


/**
 * Created by jcundill on 23/02/2017.
 */
class GpxWriter {

    fun readFromFile(filename: String): List<GHPoint> {

        val fis = FileInputStream(filename)
        val parser = GPXParser()
        val gpx = parser.parseGPX(fis)

        fis.close()
        val route = gpx.routes.toList()[0]
        return route.routePoints.map { wpt -> GHPoint(wpt.latitude, wpt.longitude) }
    }

    fun writeToFile(course: Course, filename: String) {
        val df = DecimalFormat("#")
        val gpx = GPX()
        val rte = Route()
        rte.name = "Course"
        gpx.addRoute(rte)

        fun describeFeatures(idx: Int): String {
            val descs = course.featureScores.map { "${it.key} ${df.format(100.0 - it.value[idx] * 100.0)}%" }
            return descs.joinToString("\n")
        }

        course.controls.forEachIndexed { idx, pt ->
            rte.addRoutePoint(Waypoint().apply {
                latitude = pt.lat
                longitude = pt.lon
                name = when (idx) {
                    0, course.controls.size - 1 -> "Start / Finish"
                    else -> "Control: $idx"
                }
                description = when (idx) {
                    0, course.controls.size - 1 -> ""
                    else -> "Score: ${df.format(100.0 - course.numberedControlScores[idx - 1] * 100.0)}% \n${describeFeatures(idx - 1)}"
                }
            })
        }


        gpx.addTrack((Track()).apply {
            name = "Calculated Route"
            val s = df.format((1000.0 - course.energy) / 10.0)
            description =
                    """Length: ${df.format(course.route.distance)}
                  |Ascend: ${course.route.ascend} Descend: ${course.route.descend}
                  |Goodness: $s%""".trimMargin()

            //  now you can fetch the closest edge via:
            val wpts = course.route.instructions.createGPXList().map { gpxEntry ->
                Waypoint().apply {
                    latitude = gpxEntry.lat
                    longitude = gpxEntry.lon
                }
            }
            trackPoints = ArrayList(wpts)
        })

        val parser = GPXParser()

        val fred = File(filename)
        val fos = FileOutputStream(fred)
        parser.writeGPX(gpx, fos)
        fos.close()

    }


}
