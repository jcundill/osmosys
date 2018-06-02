package jon.test

import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
import jon.test.scorers.FeatureScorer
import org.alternativevision.gpx.GPXParser
import org.alternativevision.gpx.beans.GPX
import org.alternativevision.gpx.beans.Route
import org.alternativevision.gpx.beans.Track
import org.alternativevision.gpx.beans.Waypoint
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat


/**
 * Created by jcundill on 23/02/2017.
 */
class GpxWriter {

    fun writeToFile(controls: List<GHPoint>, best: PathWrapper, score: Double, controlScores: List<Double>,
                    detailedScores: List<Pair<String, List<Double>>>, filename: String) {
        val df = DecimalFormat("#")
        val gpx = GPX()
        val rte = Route()
        rte.name = "Course"
        gpx.addRoute(rte)

        fun describeFeatures(idx:Int): String {
            val descs =  detailedScores.map {"${it.first} ${df.format(100.0 - it.second[idx] * 100.0)}%"}
            return descs.joinToString("\n")
        }

        controls.forEachIndexed { idx, pt ->
            val wpt = Waypoint()
            wpt.latitude = pt.lat
            wpt.longitude = pt.lon
            wpt.name = when {
                idx == 0 || idx == controls.size - 1 -> "Start / Finish"
                else -> "Control: $idx"
            }
            wpt.description = when {
                idx == 0 || idx == controls.size - 1 -> ""
                else -> "Score: ${df.format(100.0 - controlScores[idx - 1] * 100.0)}% \n${describeFeatures(idx - 1)}"
            }

            rte.addRoutePoint(wpt)
        }

        val track = Track()
        track.name = "Calculated Route"
        val s = df.format( (1000.0 - score) / 10.0)
        track.description =
                """Length: ${df.format(best.distance)}
                  |Ascend: ${best.ascend} Descend: ${best.descend}
                  |Goodness: $s%""".trimMargin()
        gpx.addTrack(track)

        //  now you can fetch the closest edge via:
        val ctrls = best.instructions.createGPXList()
        val wpts = ctrls.map { gpxEntry ->
            val wpt = Waypoint()
            wpt.latitude = gpxEntry.lat
            wpt.longitude = gpxEntry.lon
            wpt
        }

        track.trackPoints = ArrayList(wpts)

        val parser = GPXParser()

        val fred = File(filename)
        val fos = FileOutputStream(fred)
        parser.writeGPX(gpx, fos)
        fos.close()

    }


}
