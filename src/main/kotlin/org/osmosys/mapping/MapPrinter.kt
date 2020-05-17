/*
 *
 *     Copyright (c) 2017-2020 Jon Cundill.
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package org.osmosys.mapping

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

    fun generateMapFiles(filename: String, title: String, controls: List<GHPoint>, centre: Coordinate, box: MapBox, descriptions: List<String> = emptyList()) {
        val orientationString = if (box.landscape) "0.297,0.21" else "0.21,0.297"
        val mapCentre = convertBack(centre.x, centre.y).coordinate

        val centreLat = mapCentre.y.toInt()
        val centreLon = mapCentre.x.toInt()
        val id = requestKey()

        // https://tiler4.oobrien.com/pdf/?style=streeto|paper=0.29700000000000004,0.21000000000000002|scale=10000|centre=6981466,-133774|title=OpenOrienteeringMap|club=|id=5dcfc371bcc02|start=|crosses=|cps=|controls=
        generateArtifact("pdf", orientationString, box, centreLat, centreLon, title, id, controls, filename, centre, descriptions)
        generateArtifact("kmz", orientationString, box, centreLat, centreLon, title, id, controls, filename, centre, descriptions)
        //generateArtifact("jgw", orientationString, box, centreLat, centreLon, title, id, controls, filename, centre, descriptions)

    }

    private fun generateArtifact(mapType: String, orientationString: String, box: MapBox, centreLat: Int, centreLon: Int, title: String, id: String, controls: List<GHPoint>, filename: String, centre: Coordinate, descriptions:List<String>) {
        val url = "https://tiler4.oobrien.com/$mapType/?style=streeto|" +
                "paper=$orientationString" +
                "|scale=${box.scale}" +
                "|centre=$centreLat,$centreLon" +
                "|title=$title" +
                "|club=Osmosys" +
                "|id=${id}" +
                "|start=" +
                "|crosses=" +
                "|cps=" +
                "|controls="

        with(URL(url).openConnection() as HttpURLConnection) {
            val bis = BufferedInputStream(ByteArrayInputStream(inputStream.readBytes()))
            if ( mapType == "pdf")
                decorator.decorate(bis, controls, File("$filename.$mapType"), box, centre)
            else {
                val f = File("$filename.$mapType")
                f.writeBytes(bis.readAllBytes())
            }
        }
    }

    private fun requestKey(): String {
        val dummyString = "data%5Baction%5D=savemap&data%5Btitle%5D=OpenOrienteeringMap&data%5Brace_instructions%5D" +
                "=Race+instructions&data%5Beventdate%5D=&data%5Bclub%5D=&data%5Bstyle%5D=streeto&data%5Bscale%5D" +
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

        val url = URL("https://oomap.co.uk/save.php")

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

            val response = StringBuffer()
            //val rd = BufferedReader(InputStreamReader(inputStream))
            BufferedReader(InputStreamReader(inputStream)).use { rd ->
                rd.lines().forEach {
                    response.append(it)
                }
            }
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