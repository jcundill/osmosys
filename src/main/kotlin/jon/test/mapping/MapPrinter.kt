package jon.test.mapping

import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import org.geotools.geometry.jts.JTS
import org.geotools.geometry.jts.JTSFactoryFinder
import org.geotools.referencing.CRS
import org.opengis.referencing.crs.CoordinateReferenceSystem
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class MapPrinter {

    //create reference system WGS84 Web Mercator
    private val wgs84Web: CoordinateReferenceSystem = CRS.decode("EPSG:3857", true)
    //create reference system WGS84
    private val wgs84: CoordinateReferenceSystem = CRS.decode("EPSG:4326", true)
    //Create transformation from WS84 to WGS84 Web Mercator
    private val wgs84ToWgs84Web = CRS.findMathTransform(wgs84, wgs84Web)
    private val decorator = MapDecorator()


    private fun convertBack(lon: Double, lat: Double): Geometry {
        val jtsGf = JTSFactoryFinder.getGeometryFactory()
        val pointInWgs84Web: Geometry = jtsGf.createPoint(Coordinate(lon, lat))
        //transform point from WGS84 Web Mercator to WGS84
        return JTS.transform(pointInWgs84Web, wgs84ToWgs84Web)
    }

    fun generatePDF(filename: String, title: String, controls: List<GHPoint>, centre: Coordinate, box: MapBox) {
        val orientationString = if (box.landscape) "0.297,0.21" else "0.21,0.297"
        val mapCentre = convertBack(centre.x, centre.y).coordinate

        val centreLat = mapCentre.y.toInt()
        val centreLon = mapCentre.x.toInt()

        val url = "http://tiler1.oobrien.com/pdf/?style=streeto|" +
                "paper=$orientationString" +
                "|scale=${box.scale}" +
                "|centre=$centreLat,$centreLon" +
                "|title=$title" +
                "|club=StreetO" +
                "|id=${requestKey()}" +
                "|start=" +
                "|crosses=" +
                "|cps=" +
                "|controls="

        with(URL(url).openConnection() as HttpURLConnection) {
            val bis = BufferedInputStream(ByteArrayInputStream(inputStream.readBytes()))
            decorator.decorate(bis, controls, File(filename), box, centre)
        }

    }

    private fun requestKey(): String {
        val dummyString = "data%5Baction%5D=savemap&data%5Btitle%5D=OpenOrienteeringMap&data%5Brace_instructions%5D" +
                "=Race+instructions&data%5Beventdate%5D=&data%5Bclub%5D=&data%5Bstyle%5D=streeto_global&data%5Bscale%5D" +
                "=s10000&data%5Bpapersize%5D=p2970-2100&data%5Bpaperorientation%5D=portrait&data%5Bcentre_lat%5" +
                "D=6980976.811425837&data%5Bcentre_lon%5D=-134828.74097699366&data%5Bcentre_wgs84lat%5D=52.989072105477476&data%5B" +
                "centre_wgs84lon%5D=-1.2111871875822542&data%5Bcontrols%5D%5B0%5D%5Bid%5D=1&data%5Bcontrols%5D%5B0%5D%5Bnumber%5D" +
                "=1&data%5Bcontrols%5D%5B0%5D%5Bangle%5D=45&data%5Bcontrols%5D%5B0%5D%5Bscore%5D=10&data%5Bcontrols%5D%5B0%5D%5Btype%5" +
                "D=c_regular&data%5Bcontrols%5D%5B0%5D%5Bdescription%5D=&data%5Bcontrols%5D%5B0%5D%5Blat%5D=6980995.808714605&data%5B" +
                "controls%5D%5B0%5D%5Blon%5D=-134369.39101331212&data%5Bcontrols%5D%5B0%5D%5Bwgs84lat%5D=52.989174834420936&data%5Bcontrols" +
                "%5D%5B0%5D%5Bwgs84lon%5D=-1.2070607766509056&data%5Bcontrols%5D%5B1%5D%5Bid%5D=2&data%5Bcontrols%5D%5B1%5D%5Bnumber%5D" +
                "=2&data%5Bcontrols%5D%5B1%5D%5Bangle%5D=45&data%5Bcontrols%5D%5B1%5D%5Bscore%5D=10&data%5Bcontrols%5D%5B1%5D%5Btype%5D" +
                "=c_regular&data%5Bcontrols%5D%5B1%5D%5Bdescription%5D=&data%5Bcontrols%5D%5B1%5D%5Blat%5D=6981589.203398543&data%5B" +
                "controls%5D%5B1%5D%5Blon%5D=-134322.5136170591&data%5Bcontrols%5D%5B1%5D%5Bwgs84lat%5D=52.99238352766508&data%5Bcontrols" +
                "%5D%5B1%5D%5Bwgs84lon%5D=-1.2066396698355673&data%5Bcontrols%5D%5B2%5D%5Bid%5D=0&data%5Bcontrols%5D%5B2%5D%5Bnumber%5D=" +
                "&data%5Bcontrols%5D%5B2%5D%5Bangle%5D=45&data%5Bcontrols%5D%5B2%5D%5Bscore%5D=0&data%5Bcontrols%5D%5B2%5D%5Btype%5D=" +
                "c_startfinish&data%5Bcontrols%5D%5B2%5D%5Bdescription%5D=&data%5Bcontrols%5D%5B2%5D%5Blat%5D=6981216.554224269&data%5B" +
                "controls%5D%5B2%5D%5Blon%5D=-135425.9052604716&data%5Bcontrols%5D%5B2%5D%5Bwgs84lat%5D=52.990368510683766&data%5Bcontrol" +
                "s%5D%5B2%5D%5Bwgs84lon%5D=-1.2165516056120393"

        val dummyParameters = dummyString.toByteArray()

        val url = URL("http://oomap.co.uk/save.php")

        return with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Content-Length", dummyParameters.size.toString())
            setRequestProperty("Content-Language", "en-US")

            useCaches = false
            doInput = true
            doOutput = true

            outputStream.write(dummyParameters)
            outputStream.flush()
            outputStream.close()

            val rd = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuffer()
            rd.lines().forEach {
                response.append(it)
            }
            rd.close()
            response.toString().split(",").find { it.startsWith("\"message\"") }!!.substringAfter("\"message\":\"").dropLast(1)
        }
    }

    private fun formatControlsList(controls: List<Coordinate>): String {
        //1,45,6981557,-134761
        val strings = controls.drop(1).dropLast(1).mapIndexed { idx, coord -> "${idx + 1},45,${coord.y.toInt()},${coord.x.toInt()}" }
        val finishString = when {
            controls.first() == controls.last() -> ""
            else -> {
                val coord = controls.last()
                ",F,45,${coord.y.toInt()},${coord.x.toInt()}"
            }
        }
        return strings.joinToString(separator = ",") + finishString

    }
}