package jon.test

import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.Geometry
import org.geotools.geometry.jts.JTS
import org.geotools.geometry.jts.JTSFactoryFinder
import org.geotools.referencing.CRS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opengis.referencing.crs.CoordinateReferenceSystem
import org.opengis.referencing.operation.MathTransform
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.assertTrue

class CRSTest {

    lateinit var wgs84WebToWgs84: MathTransform
    lateinit var wgs84ToWgs84Web: MathTransform


    @BeforeEach
    fun before() {
        //create reference system WGS84 Web Mercator
        val wgs84Web: CoordinateReferenceSystem = CRS.decode("EPSG:3857", true)
        //create reference system WGS84
        val wgs84: CoordinateReferenceSystem = CRS.decode("EPSG:4326", true)

        //Create transformation from WS84 Web Mercator to WGS84
        wgs84WebToWgs84 = CRS.findMathTransform( wgs84Web, wgs84  )

        //Create transformation from WS84 to WGS84 Web Mercator
        wgs84ToWgs84Web = CRS.findMathTransform( wgs84, wgs84Web  )

    }

    fun delta(a:Double, b:Double, c:Double):Boolean = when {
        Math.abs(a - b) < c -> true
        else -> false
    }

    //paper=0.29700000000000004,0.21000000000000002|scale=10000|
    // centre=6982422,-134764
    //controls=
    // 6983933,-137095,
    // 6980908,-137095,
    // 6983936,-132439,
    // 6980905,-132433

    @Test
    fun box() {


        val a = Coordinate(-1.2315453387636581, 53.00505492801988)
        val b = convert(-137095.0,6980908.0).coordinate
        val c = convert(-132439.0,6983936.0).coordinate
        val d = convert(-132433.0,6980905.0).coordinate

        val centre = convert(-134764.0,6982422.0).coordinate

        println(a)
        println(b)
        println(c)
        println(d)

        val env = Envelope(a)
        env.expandToInclude(b)
        env.expandToInclude(c)
        env.expandToInclude(d)

        assertTrue { env.contains( centre )}
        println ("${env.width}  ${env.height}")

    }

    @Test
    fun mapFits() {
        val centre = Coordinate(-1.18794, 52.99247)
        val c = convertBack(-1.18794, 52.99247).coordinate
        fuelTest(filename = "jon.pdf", centre = c)
    }


     //http://tiler1.oobrien.com/pdf/?style=streeto_global|paper=0.21000000000000002,0.29700000000000004|scale=10000|centre=6982438,-135151|title=OpenOrienteeringMap|club=|id=5b03178056f05|start=6980833,-133946|crosses=|cps=|controls=1,45,6981557,-134761,2,45,6982516,-133773,3,45,6983578,-134206,4,45,6983379,-134943,5,45,6982485,-134904,6,45,6981572,-133950
    fun fuelTest(filename: String = "somefile.pdf", centre:Coordinate) {
        val url = "http://tiler1.oobrien.com/pdf/?style=streeto|paper=0.3,0.2|scale=12500|centre=${centre.y.toInt()},${centre.x.toInt()}|title=OpenOrienteeringMap|club=|id=5603178056f05|start=6980833,-133946|finish=6980833,-133946|crosses=|cps=|controls="
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
        }    }

    @Test
    fun convert() {

        //create a point
        val lon = -133973.0605429214
        val lat = 6981971.873735948
        val pointInWgs84 = convert(lon, lat)

        val outLon = -1.2035004794597628
        val outLat = 52.994452634005086

        val calcLon = pointInWgs84.coordinate.x
        val calcLat = pointInWgs84.coordinate.y

        assertTrue(delta(calcLon, outLon, 0.0000001))
        assertTrue(delta(calcLat, outLat, 0.0000001))

        println( pointInWgs84)

        val back = convertBack(calcLon, calcLat)
        assertTrue(delta(back.coordinate.x, lon, 0.0000001))
        assertTrue(delta(back.coordinate.y, lat, 0.0000001))

        println( back )

    }

    private fun convert(lon: Double, lat: Double):Geometry {
        val jtsGf = JTSFactoryFinder.getGeometryFactory()
        val pointInWgs84: Geometry = jtsGf.createPoint(Coordinate(lon, lat))
        //transform point from WGS84 Web Mercator to WGS84
        return JTS.transform(pointInWgs84, wgs84WebToWgs84)

    }

    private fun convertBack(lon: Double, lat: Double):Geometry {
        val jtsGf = JTSFactoryFinder.getGeometryFactory()
        val pointInWgs84Web: Geometry = jtsGf.createPoint(Coordinate(lon, lat))
        //transform point from WGS84 Web Mercator to WGS84
        return  JTS.transform(pointInWgs84Web, wgs84ToWgs84Web)
    }
}