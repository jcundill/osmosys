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
import java.awt.Rectangle
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class MapDecorator(val params: CourseParameters) {

    fun decorate(pdfStream: InputStream, controls: List<GHPoint>, orientation: MapBox, outFile: File) {

        val mm2pt = { num: Float -> num / 25.4f * 72}


        // the centre is the thing in the middle
        val coords: List<Coordinate> = controls.map { Coordinate(it.lon, it.lat) }

        val env = Envelope()
        coords.forEach { env.expandToInclude(it.x, it.y) }


        val mapCentre = GHPoint(env.centre().y, env.centre().x) // this is what we told the tiler

        val offsetsInMetres = controls.map { control ->
            val distLat = dist2d.calcDist(control.lat, mapCentre.lon, mapCentre.lat, mapCentre.lon) * if( control.lat < mapCentre.lat) -1.0 else 1.0
            val distLon = dist2d.calcDist(mapCentre.lat, control.lon, mapCentre.lat, mapCentre.lon) * if(control.lon < mapCentre.lon) -1.0 else 1.0
            Pair( distLon.toFloat(), distLat.toFloat() ) //dists in m from the centre
        }

       val doc = PDDocument.load(pdfStream)
        val page: PDPage = doc.documentCatalog.pages.get(0)
        val width  = page.mediaBox.width
        val height = page.mediaBox.height

        val marginTop = 0.014
        val marginLeft = 0.008

        val mapWidth = orientation.widthInMetres


        val portraitPaperSize = Pair(0.21000000000000002 - 2 * marginLeft,0.29700000000000004 - 2 * marginTop)
        val landscapePaperSize = Pair(0.29700000000000004 - 2 * marginLeft, 0.21000000000000002 - 2 * marginTop)

        val paperSize = if( orientation == params.landscape) landscapePaperSize else portraitPaperSize

        println("MediaBox: $width $height")
        println("CropBox: ${page.cropBox.width} ${page.cropBox.height}")

        val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)
        val centre = Pair(width / 2, height/2 )

        val offsetsInPts = offsetsInMetres.map {p ->
            val xPt = mm2pt(p.first / 10.toFloat()) //lon
            val yPt = mm2pt(p.second / 10.toFloat()) //lat
            Pair(xPt + 0.5f, yPt - 1.0f)
        }


        offsetsInPts.windowed(2).forEach { drawLine(contentStream, centre, it) }
        drawFinish(contentStream, centre, offsetsInPts.last())
        contentStream.close()

        return doc.save(outFile)

    }

    private fun drawFinish(content: PDPageContentStream, centre: Pair<Float, Float>, last: Pair<Float, Float>) {
        val cx = centre.first + last.first
        val cy = centre.second + last.second
        val r = 9.5f
        content.moveTo(centre.first + last.first, centre.second + last.second)
        content.setLineWidth(1.5.toFloat())
        content.setStrokingColor(Color.MAGENTA)
        val k = 0.552284749831f;
        content.moveTo(cx - r, cy);
        content.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx, cy + r);
        content.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy);
        content.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx, cy - r);
        content.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy);
        content.stroke();
    }

    fun drawLine(content: PDPageContentStream, centre:Pair<Float, Float>, line: List<Pair<Float, Float>>) {
        content.moveTo(centre.first + line[0].first, centre.second + line[0].second)
        content.setLineWidth(1.5.toFloat())
        content.setStrokingColor(Color.MAGENTA)
        content.lineTo( centre.first + line[1].first, centre.second + line[1].second)
        content.stroke()
    }

}