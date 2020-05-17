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

package org.osmosys.maprun

import com.graphhopper.util.shapes.GHPoint
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class KmlWriter {
    private fun getStartKML(control: GHPoint): String = getKML(control, "S1")
    private fun getFinishKML(control: GHPoint): String = getKML(control, "F1")
    private fun getControlKML(control: GHPoint, num: Int): String = getKML(control, num.toString())

    private fun getKML(control: GHPoint, name: String): String =
            """<Placemark>
            |        <name>$name</name>
            |        <styleUrl>#startfinish</styleUrl>
            |        <Point>
            |            <gx:drawOrder>1</gx:drawOrder>
            |            <coordinates>${control.lon},${control.lat},0</coordinates>
            |        </Point>
            |</Placemark>""".trimMargin()


    fun readFile(kmlFile: String): List<GHPoint>
    {
        val xmlFile = File(kmlFile)
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(xmlFile.readText()))
        val doc = dBuilder.parse(xmlInput)
        val xpFactory = XPathFactory.newInstance()
        val xPath = xpFactory.newXPath()

        //<item type="T1" count="1">Value1</item>
        val xpath = "//Placemark"

        val map = HashMap<Int, GHPoint>()
        val markers = xPath.evaluate(xpath, doc, XPathConstants.NODESET) as NodeList
        for( i in 0 until markers.length) {
            val node = markers.item(i) as Element
            val name = node.getElementsByTagName("name").item(0).textContent
            val point = node.getElementsByTagName("Point").item(0) as Element
            val coords = (point.getElementsByTagName("coordinates").item(0) as Element).textContent
            val x = coords.split(",")
            val pt = GHPoint(x[1].toDouble(), x[0].toDouble())
            val num = when (name) {
                "S1" -> 0
                "F1" -> i
                else -> name.toInt()
            }
            map[num] = pt
        }

        return map.toSortedMap().values.toList()
    }


    fun generate(controls: List<GHPoint>, mapID: String): String
    {
        var kml = ""

        var kmlintro = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n"
        kmlintro += "<Document>\n<name>oom_" + mapID + "_controls</name>\n<open>1</open>\n"
        kmlintro += "<Style id=\"startfinish\"><IconStyle><color>ffff00ff</color><scale>1.8</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/wht-stars.png</href></Icon><hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/></IconStyle><LabelStyle><color>ffff00ff</color></LabelStyle><BalloonStyle></BalloonStyle><ListStyle></ListStyle></Style>\n"
        kmlintro += "<Style id=\"control\"><IconStyle><color>ffff00ff</color><scale>1.0</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/wht-blank.png</href></Icon><hotSpot x=\"0.5\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/></IconStyle><LabelStyle><color>ffff00ff</color><scale>1.0</scale></LabelStyle><BalloonStyle></BalloonStyle><ListStyle></ListStyle></Style>\n"
        val kmlheader = "<Folder>\n<name>Controls</name>\n<open>1</open>\n\n"
        val kmlfooter = "</Folder>\n</Document>\n</kml>"

        kml += kmlintro + kmlheader

        kml += getStartKML(controls[0])
        kml += '\n'

        controls.drop(1).dropLast(1).forEachIndexed { index, control ->
            kml += getControlKML(control, index + 1)
            kml += '\n'
        }
        kml += getFinishKML(controls.last())
        kml += '\n'

        kml += kmlfooter

        return kml
    }
}