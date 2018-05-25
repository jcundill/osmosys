package jon.test

import com.graphhopper.PathWrapper
import com.graphhopper.util.shapes.GHPoint
import org.alternativevision.gpx.GPXParser
import org.alternativevision.gpx.beans.GPX
import org.alternativevision.gpx.beans.Route
import org.alternativevision.gpx.beans.Track
import org.alternativevision.gpx.beans.Waypoint
import java.io.File
import java.io.FileOutputStream


/**
 * Created by jcundill on 23/02/2017.
 */
class GpxWriter {

    fun writeToFile(controls: List<GHPoint>, best: PathWrapper, filename: String)  {
        val gpx =  GPX()
        val rte = Route()
        rte.name = "Course"
        gpx.addRoute(rte)

        controls.forEachIndexed { idx, pt ->
            val wpt =  Waypoint()
            wpt.latitude = pt.lat
            wpt.longitude = pt.lon
            wpt.name = when {
                idx == 0  || idx == controls.size - 1  -> "Start / Finish"
                else -> "Control: $idx"
            }

            rte.addRoutePoint(wpt)
        }

        val track = Track()
        track.name = "Calculated Route"
        track.description = "Length: ${best.distance} Ascend: ${best.ascend} Descend: ${best.descend}"
        gpx.addTrack(track)

        //  now you can fetch the closest edge via:
        val ctrls = best.instructions.createGPXList()
        val wpts = ctrls.map {
            gpxEntry ->
            val wpt = Waypoint()
            wpt.latitude = gpxEntry.lat
            wpt.longitude = gpxEntry.lon
            wpt
        }
        track.trackPoints = ArrayList(wpts)

        val parser = GPXParser()

        val fred = File(filename)
        val fos = FileOutputStream(fred)
        parser.writeGPX(gpx, fos )
        fos.close()

    }



}
