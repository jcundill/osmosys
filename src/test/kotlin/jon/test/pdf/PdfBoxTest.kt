package jon.test.pdf

import com.graphhopper.util.shapes.GHPoint
import jon.test.CourseParameters
import jon.test.gpx.GpxWriter
import jon.test.mapping.MapDecorator
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.junit.jupiter.api.TestInstance
import java.awt.Color
import java.awt.Rectangle
import java.io.File
import java.io.IOException


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PdfBoxTest {

    //@Test
    fun decorator() {
        val params = CourseParameters(numControls = 15, start = GHPoint(52.988304, -1.203265))

        val points = GpxWriter().readFromFile("/Users/jcundill/Documents/Map-1528636867397.gpx")
        val original = File("/Users/jcundill/Documents/Map-1528636867397.pdf")
        val modified = "map-out.pdf"

        val d = MapDecorator(params)

        d.decorate(pdfStream = original.inputStream(), controls = points, outFile = File(modified), box = params.portrait12500)
    }

    //@Test
    fun draw() {
        val original = File("/Users/jcundill/Documents/map.pdf")
        val modified = "map-out.pdf"

        val doc = PDDocument.load(original)
        val page: PDPage = doc.documentCatalog.pages.get(0)
        val width  = page.mediaBox.width
        val height = page.mediaBox.height

        println("MediaBox: $width $height")
        println("CropBox: ${page.cropBox.width} ${page.cropBox.height}")

        val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)
        drawRect(contentStream, Color.green, Rectangle (width.toInt() / 2 - 25, height.toInt()/2 - 10, 50, 20), true)
        val centre = Pair(width / 2, height/2 )
        val end = Pair( width / 2 + width * 0.2.toFloat(), height / 2 + height * 0.2.toFloat())
        drawLine(contentStream, centre, end)
        contentStream.close()
        doc.save(File (modified))
    }

    fun drawLine(content: PDPageContentStream, start:Pair<Float, Float>, end: Pair<Float, Float>) {
        content.moveTo(start.first, start.second)
        content.setLineWidth(1.5.toFloat())
        content.setStrokingColor(Color.MAGENTA)
        content.lineTo( start.second + end.first, start.second + end.second)
        content.stroke()
    }

    @Throws(IOException::class)
    private fun drawRect(content: PDPageContentStream, color: Color, rect: Rectangle, fill: Boolean) {
        content.addRect(rect.x.toFloat(), rect.y.toFloat(), rect.width.toFloat(), rect.height.toFloat())
        if (fill) {
            content.setNonStrokingColor(color)
            content.fill()
        } else {
            content.setStrokingColor(color)
            content.stroke()
        }
    }


}