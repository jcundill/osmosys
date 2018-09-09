package jon.test.gpx

import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
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

    fun writeToFile(controls: List<GHPoint>, best: PathWrapper, score: Double, controlScores: List<Double>,
                    detailedScores: List<Pair<String, List<Double>>>, filename: String) {
        val df = DecimalFormat("#")
        val gpx = GPX()
        val rte = Route()
        rte.name = "Course"
        gpx.addRoute(rte)

        fun describeFeatures(idx: Int): String {
            val descs = detailedScores.map { "${it.first} ${df.format(100.0 - it.second[idx] * 100.0)}%" }
            return descs.joinToString("\n")
        }

        controls.forEachIndexed { idx, pt ->
            rte.addRoutePoint(Waypoint().apply {
                latitude = pt.lat
                longitude = pt.lon
                name = when {
                    idx == 0 || idx == controls.size - 1 -> "Start / Finish"
                    else -> "Control: $idx"
                }
                description = when {
                    idx == 0 || idx == controls.size - 1 -> ""
                    else -> "Score: ${df.format(100.0 - controlScores[idx - 1] * 100.0)}% \n${describeFeatures(idx - 1)}"
                }
            })
        }

        gpx.addTrack((Track()).apply {
            name = "Calculated Route"
            val s = df.format((1000.0 - score) / 10.0)
            description =
                    """Length: ${df.format(best.distance)}
                  |Ascend: ${best.ascend} Descend: ${best.descend}
                  |Goodness: $s%""".trimMargin()

            //  now you can fetch the closest edge via:
            val wpts = best.instructions.createGPXList().map { gpxEntry ->
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
