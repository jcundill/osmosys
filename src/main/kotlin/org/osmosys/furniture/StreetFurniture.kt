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

package org.osmosys.furniture

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Way

class StreetFurniture {

    val lat: Double
    val lon: Double
    val description: String

    constructor(node: Node) {
        lat = node.position.latitude
        lon = node.position.longitude
        description = when {
            node.tags["tourism"] != null -> node.tags["tourism"]
            node.tags["highway"] != null -> node.tags["highway"]
            node.tags["amenity"] != null -> node.tags["amenity"]
            node.tags["natural"] != null -> node.tags["natural"]
            node.tags["barrier"] != null -> node.tags["barrier"]
            else -> "unknown"
        }!!
    }
    constructor(way: Way, loc: LatLon)  {
        lat = loc.latitude
        lon = loc.longitude
        description = when {
            way.tags["highway"] == "steps" -> "steps"
            way.tags["bridge"] == "yes" -> "bridge"
            way.tags["barrier"] == "hedge" -> "hedge"
            else -> "unknown"
        }

    }

}
