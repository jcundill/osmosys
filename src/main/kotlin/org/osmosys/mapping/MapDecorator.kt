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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import org.osmosys.ControlSite
import org.osmosys.improvers.dist2d
import org.vandeseer.easytable.TableDrawer
import org.vandeseer.easytable.settings.HorizontalAlignment
import org.vandeseer.easytable.structure.Row
import org.vandeseer.easytable.structure.Table
import org.vandeseer.easytable.structure.cell.CellText
import java.awt.Color
import java.io.File
import java.io.InputStream
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MapDecorator {

    private val mm2pt = { num: Float -> num / 25.4f * 72 }
    // Create a new font object selecting one of the PDF base fonts
    private val font: PDFont = PDType1Font.HELVETICA_BOLD


    fun decorate(pdfStream: InputStream, controls: List<ControlSite>, outFile: File, box: MapBox, centre: Coordinate) {

        val doc = PDDocument.load(pdfStream)
        val page: PDPage = doc.documentCatalog.pages.get(0)
        val width = page.mediaBox.width
        val height = page.mediaBox.height

        val centrePage = Pair(width / 2, height / 2)

        // the centre is the thing in the middle
        val mapCentre = GHPoint(centre.y, centre.x) // this is what we told the tiler

        val offsetsInPts = getControlOffsets(controls, mapCentre, box, centrePage)

        // fade the lines a bit so that you can see the map through them
        val alpha = 0.55f
        val bold = 0.9f
        val graphicsState = PDExtendedGraphicsState()
        graphicsState.strokingAlphaConstant = alpha
        graphicsState.nonStrokingAlphaConstant = bold


        val content = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)

        content.setFont(font, 20.0f)
        content.setNonStrokingColor(Color.MAGENTA)
        content.setGraphicsStateParameters(graphicsState)
        content.setLineCapStyle(0)

        content.setLineWidth(2.0f)
        content.setStrokingColor(Color.WHITE)

        drawStart(content, offsetsInPts.take(2))
        drawFinish(content, offsetsInPts.last())
        drawCourse(content, offsetsInPts)

        content.setLineWidth(1.5f)
        content.setStrokingColor(Color.MAGENTA)

        drawStart(content, offsetsInPts.take(2))
        drawFinish(content, offsetsInPts.last())
        drawCourse(content, offsetsInPts)

        content.close()

        val page2 = PDPage(PDRectangle.A4)
        doc.addPage(page2)

            val content2 = PDPageContentStream(doc, page2, PDPageContentStream.AppendMode.APPEND, true, true)
            val tableBuilder = Table.builder()
                    .addColumnsOfWidth(60.0f, 100.0f)
                    .addRow(Row.builder()
                            .add(CellText.builder().text("Control").borderWidth(1.0f).backgroundColor(Color.LIGHT_GRAY).build())
                            .add(CellText.builder().text("Description").borderWidth(1.0f).backgroundColor(Color.LIGHT_GRAY).build())
                            .build())
            controls.forEachIndexed { idx, control ->
                val ctrl = when (idx) {
                    0 -> "Start"
                    controls.size - 1 -> "Finish"
                    else -> idx.toString()
                }

                tableBuilder.addRow(Row.builder()
                        .add(CellText.builder().text(ctrl).borderWidth(1.0f).horizontalAlignment(HorizontalAlignment.RIGHT).build())
                        .add(CellText.builder().text(control.description).borderWidth(1.0f).build())
                        .build())
            }
            val table = tableBuilder.build()
            val tableDrawer = TableDrawer.builder()
                    .contentStream(content2)
                    .startX(20f)
                    .startY(page2.mediaBox.upperRightY - 20f)
                    .table(table)
                    .build();
            tableDrawer.draw()
            content2.close()

        return doc.save(outFile)

    }

    private fun drawCourse(content: PDPageContentStream, offsetsInPts: List<Pair<Float, Float>>) {
        offsetsInPts.drop(1).dropLast(1).forEachIndexed { index, pair -> drawControl(content, (index + 1).toString(), pair) }
        offsetsInPts.windowed(2).forEach { drawLine(content, it) }
    }

    private fun getControlOffsets(controls: List<ControlSite>, mapCentre: GHPoint, box: MapBox, centrePage: Pair<Float, Float>): List<Pair<Float, Float>> {
        val offsetsInMetres = controls.map { control ->
            val distLat = dist2d.calcDist(control.position.lat, mapCentre.lon, mapCentre.lat, mapCentre.lon) * if (control.position.lat < mapCentre.lat) -1.0 else 1.0
            val distLon = dist2d.calcDist(mapCentre.lat, control.position.lon, mapCentre.lat, mapCentre.lon) * if (control.position.lon < mapCentre.lon) -1.0 else 1.0
            Pair(distLon.toFloat(), distLat.toFloat()) //dists in m from the centre
        }

        return offsetsInMetres.map { p ->
            val ratio = box.scale.toFloat() / 1000.0f
            val xPt = mm2pt(p.first / ratio) //lon
            val yPt = mm2pt(p.second / ratio) //lat
            Pair(centrePage.first + xPt + 0.2f, centrePage.second + yPt - 1.3f)
        }
    }

    private fun drawStart(content: PDPageContentStream, pos: List<Pair<Float, Float>>) {
        val start = pos[0]
        val next = pos[1]
        var angle = atan2((next.second - start.second), (next.first - start.first))
        if (angle < 0) {
            angle += (2 * PI.toFloat())
        }

        drawTriangle(content, start.first, start.second, 13.0f, angle + (PI/2).toFloat())
    }

    private fun drawTriangle(content: PDPageContentStream, x: Float, y: Float, width: Float, angle: Float) {

        val degrees120 = (2 * PI / 3).toFloat()
        val x0 = x + width * sin(angle)
        val y0 = y - width * cos(angle)
        val x1 = x + (width * sin(angle + degrees120))
        val y1 = y - (width * cos(angle + degrees120))
        val x2 = x + (width * sin(angle - degrees120))
        val y2 = y - (width * cos(angle - degrees120))

        content.moveTo(x0, y0)
        content.lineTo(x1, y1)
        content.stroke()
        content.moveTo(x1, y1)
        content.lineTo(x2, y2)
        content.stroke()
        content.moveTo(x2, y2)
        content.lineTo(x0, y0)
        content.stroke()
    }


    private fun drawFinish(content: PDPageContentStream, last: Pair<Float, Float>) {
        drawCircle(content, last, 12.0f)
        drawCircle(content, last, 9.5f)
    }

    private fun drawControl(content: PDPageContentStream, number: String, position: Pair<Float, Float>) {
        drawCircle(content, position, 1.0f)
        drawCircle(content, position, 12.0f)
        drawNumber(content, number, position)
    }

    private fun drawCircle(content: PDPageContentStream, position: Pair<Float, Float>, r: Float) {
        content.moveTo(position.first, position.second)
        val k = 0.552284749831f
        content.moveTo(position.first - r, position.second)
        content.curveTo(position.first - r, position.second + k * r, position.first - k * r, position.second + r, position.first, position.second + r)
        content.curveTo(position.first + k * r, position.second + r, position.first + r, position.second + k * r, position.first + r, position.second)
        content.curveTo(position.first + r, position.second - k * r, position.first + k * r, position.second - r, position.first, position.second - r)
        content.curveTo(position.first - k * r, position.second - r, position.first - r, position.second - k * r, position.first - r, position.second)
        content.stroke()
    }

    private fun drawNumber(content: PDPageContentStream, number: String, position: Pair<Float, Float>) {
        content.beginText()
        content.newLineAtOffset(position.first + 10.0F, position.second + 10.0F)
        content.showText(number)
        content.endText()

    }

    private fun drawLine(content: PDPageContentStream, line: List<Pair<Float, Float>>) {

        var angle = atan2((line[1].second - line[0].second).toDouble(), (line[1].first - line[0].first).toDouble())
        if (angle < 0) {
            angle += (2 * PI)
        }

        val deltaX = 11.8f * cos(angle).toFloat()
        val deltaY = 11.8f * sin(angle).toFloat()

        content.moveTo(line[0].first + deltaX, line[0].second + deltaY)
        content.lineTo(line[1].first - deltaX, line[1].second - deltaY)
        content.stroke()
    }

}