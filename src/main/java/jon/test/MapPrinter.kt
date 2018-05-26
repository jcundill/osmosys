package jon.test

import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.Geometry
import org.geotools.geometry.jts.JTS
import org.geotools.geometry.jts.JTSFactoryFinder
import org.geotools.referencing.CRS
import org.opengis.referencing.crs.CoordinateReferenceSystem
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object MapPrinter {

    //create reference system WGS84 Web Mercator
    val wgs84Web: CoordinateReferenceSystem = CRS.decode("EPSG:3857", true)
    //create reference system WGS84
    val wgs84: CoordinateReferenceSystem = CRS.decode("EPSG:4326", true)

    //Create transformation from WS84 Web Mercator to WGS84
    val wgs84WebToWgs84 = CRS.findMathTransform(wgs84Web, wgs84)

    //Create transformation from WS84 to WGS84 Web Mercator
    val wgs84ToWgs84Web = CRS.findMathTransform(wgs84, wgs84Web)


    fun delta(a: Double, b: Double, c: Double): Boolean = when {
        Math.abs(a - b) < c -> true
        else -> false
    }

    fun convert(lon: Double, lat: Double): Geometry {
        val jtsGf = JTSFactoryFinder.getGeometryFactory()
        val pointInWgs84: Geometry = jtsGf.createPoint(Coordinate(lon, lat))
        //transform point from WGS84 Web Mercator to WGS84
        return JTS.transform(pointInWgs84, wgs84WebToWgs84)

    }

    fun convertBack(lon: Double, lat: Double): Geometry {
        val jtsGf = JTSFactoryFinder.getGeometryFactory()
        val pointInWgs84Web: Geometry = jtsGf.createPoint(Coordinate(lon, lat))
        //transform point from WGS84 Web Mercator to WGS84
        return JTS.transform(pointInWgs84Web, wgs84ToWgs84Web)
    }

    //http://tiler1.oobrien.com/pdf/?style=streeto_global|paper=0.21000000000000002,0.29700000000000004|scale=10000|centre=6982438,-135151|title=OpenOrienteeringMap|club=|id=5b03178056f05|start=6980833,-133946|crosses=|cps=|controls=1,45,6981557,-134761,2,45,6982516,-133773,3,45,6983578,-134206,4,45,6983379,-134943,5,45,6982485,-134904,6,45,6981572,-133950
    fun generatePDF(filename: String = "somefile.pdf", points: List<GHPoint>) {

        val controls: List<Coordinate> = points.map { convertBack(it.lon, it.lat).coordinate }

        val env = Envelope()
        env.setToNull()
        controls.forEach { env.expandToInclude(it.x, it.y) }

        val centre = env.centre()
        val centreLat = centre.y.toInt()
        val centreLon = centre.x.toInt()

        val startLat = controls[0].y.toInt()
        val startLon = controls[0].x.toInt()

        val controlsList = formatControlsList(controls)


        val url = "http://tiler1.oobrien.com/pdf/?style=streeto|paper=0.297,0.21|scale=10000|centre=$centreLat,$centreLon|title=OpenOrienteeringMap|club=|id=5603178056f05|start=$startLat,$startLon|crosses=|cps=|controls=$controlsList"
        val obj = URL(url)

        with(obj.openConnection() as HttpURLConnection) {
            println("\nSending 'GET' request to URL : $url")
            println("Response Code : $responseCode")

            BufferedInputStream(ByteArrayInputStream(inputStream.readBytes())).use {
                val response = ByteArrayOutputStream()

                var inputLine = it.read()
                while (inputLine != -1) {
                    response.write(inputLine)
                    inputLine = it.read()

                }
                File(filename).writeBytes(response.toByteArray())
            }
        }

    }

    private fun formatControlsList(controls: List<Coordinate>): String {
        //1,45,6981557,-134761
        val strings = controls.drop(1).dropLast(1).mapIndexed { idx, coord -> "${idx + 1},45,${coord.y.toInt()},${coord.x.toInt()}" }
        return strings.joinToString(separator = ",")

    }
}