package jon.test.mapping

import com.graphhopper.util.shapes.GHPoint
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Envelope
import jon.test.CourseParameters
import jon.test.MapBox
import jon.test.improvers.dist2d
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import java.awt.Color
import java.io.File
import java.io.InputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MapDecorator(val params: CourseParameters) {

    private val mm2pt = { num: Float -> num / 25.4f * 72 }

    fun decorate(pdfStream: InputStream, controls: List<GHPoint>, outFile: File, box: MapBox) {

        val doc = PDDocument.load(pdfStream)
        val page: PDPage = doc.documentCatalog.pages.get(0)
        val width = page.mediaBox.width
        val height = page.mediaBox.height

        val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)
        val centre = Pair(width / 2, height / 2)

        // the centre is the thing in the middle
        val coords: List<Coordinate> = controls.map { Coordinate(it.lon, it.lat) }

        val env = Envelope()
        coords.forEach { env.expandToInclude(it.x, it.y) }


        val mapCentre = GHPoint(env.centre().y, env.centre().x) // this is what we told the tiler

        val offsetsInMetres = controls.map { control ->
            val distLat = dist2d.calcDist(control.lat, mapCentre.lon, mapCentre.lat, mapCentre.lon) * if (control.lat < mapCentre.lat) -1.0 else 1.0
            val distLon = dist2d.calcDist(mapCentre.lat, control.lon, mapCentre.lat, mapCentre.lon) * if (control.lon < mapCentre.lon) -1.0 else 1.0
            Pair(distLon.toFloat(), distLat.toFloat()) //dists in m from the centre
        }


        val offsetsInPts = offsetsInMetres.map { p ->
            val ratio = box.scale.toFloat() / 1000.0f
            val xPt = mm2pt(p.first / ratio) //lon
            val yPt = mm2pt(p.second / ratio) //lat
            Pair(centre.first + xPt + 0.2f, centre.second + yPt - 1.3f)
        }


        offsetsInPts.windowed(2).forEach { drawLine(contentStream, it) }
        drawFinish(contentStream, offsetsInPts.last())
        contentStream.close()

        return doc.save(outFile)

    }


    private fun drawFinish(content: PDPageContentStream, last: Pair<Float, Float>) {
        val cx = last.first
        val cy = last.second
        val r = 9.5f
        content.moveTo(last.first, last.second)
        content.setLineWidth(1.5.toFloat())
        content.setStrokingColor(Color.MAGENTA)
        val k = 0.552284749831f
        content.moveTo(cx - r, cy)
        content.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx, cy + r)
        content.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy)
        content.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx, cy - r)
        content.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy)
        content.stroke()
    }

    private fun drawLine(content: PDPageContentStream, line: List<Pair<Float, Float>>) {

        var angle = atan2((line[1].second - line[0].second).toDouble(), (line[1].first - line[0].first).toDouble())
        if (angle < 0) {
            angle += (2 * Math.PI)
        }

        val deltaX = 11.8f * cos(angle).toFloat()
        val deltaY = 11.8f * sin(angle).toFloat()

        content.moveTo(line[0].first + deltaX, line[0].second + deltaY)
        content.setLineWidth(1.5f)
        content.setStrokingColor(Color.MAGENTA)
        content.lineTo(line[1].first - deltaX, line[1].second - deltaY)
        content.stroke()
    }

}