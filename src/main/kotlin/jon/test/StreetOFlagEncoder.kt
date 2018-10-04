package jon.test

/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.graphhopper.reader.ReaderRelation
import com.graphhopper.reader.ReaderWay
import com.graphhopper.routing.util.*
import com.graphhopper.routing.util.PriorityCode.*
import com.graphhopper.routing.weighting.PriorityWeighting
import com.graphhopper.util.PMap
import java.util.*

/**
 * Defines bit layout for pedestrians (speed, access, surface, ...). Here we put a penalty on unsafe
 * roads only. If you wish to also prefer routes due to beauty like hiking routes use the
 * HikeFlagEncoder instead.
 *
 *
 *
 * @author Peter Karich
 * @author Nop
 * @author Karl Hübner
 */
class StreetOFlagEncoder @JvmOverloads constructor(speedBits: Int = 4, speedFactor: Double = 1.0) : AbstractFlagEncoder(speedBits, speedFactor, 0) {
    internal val safeHighwayTags: MutableSet<String> = HashSet()
    internal val allowedHighwayTags: MutableSet<String> = HashSet()
    internal val avoidHighwayTags: MutableSet<String> = HashSet()
    // convert network tag of hiking routes into a way route code
    internal val hikingNetworkToCode: MutableMap<String, Int> = HashMap()
    protected var sidewalkValues = HashSet<String>(5)
    protected var sidewalksNoValues = HashSet<String>(5)
    private var priorityWayEncoder: EncodedValue? = null
    private var relationCodeEncoder: EncodedValue? = null

    constructor(properties: PMap) : this(properties.getLong("speedBits", 4).toInt(),
            properties.getDouble("speedFactor", 1.0)) {
        this.properties = properties
        this.isBlockFords = properties.getBool("block_fords", true)
    }

    constructor(propertiesStr: String) : this(PMap(propertiesStr))

    init {
        restrictions.addAll(Arrays.asList("foot", "access"))
        restrictedValues.add("private")
        restrictedValues.add("no")
        restrictedValues.add("restricted")
        restrictedValues.add("military")
        restrictedValues.add("emergency")

        intendedValues.add("yes")
        intendedValues.add("designated")
        intendedValues.add("official")
        intendedValues.add("permissive")

        sidewalksNoValues.add("no")
        sidewalksNoValues.add("none")
        // see #712
        sidewalksNoValues.add("separate")

        sidewalkValues.add("yes")
        sidewalkValues.add("both")
        sidewalkValues.add("left")
        sidewalkValues.add("right")

        setBlockByDefault(false)
        potentialBarriers.add("gate")

        safeHighwayTags.add("footway")
        safeHighwayTags.add("path")
        safeHighwayTags.add("steps")
        safeHighwayTags.add("pedestrian")
        safeHighwayTags.add("living_street")
        safeHighwayTags.add("track")
        safeHighwayTags.add("residential")
        //safeHighwayTags.add("service")

        avoidHighwayTags.add("trunk")
        avoidHighwayTags.add("trunk_link")
        avoidHighwayTags.add("primary")
        avoidHighwayTags.add("primary_link")
        avoidHighwayTags.add("secondary")
        avoidHighwayTags.add("secondary_link")
        avoidHighwayTags.add("tertiary")
        avoidHighwayTags.add("tertiary_link")

        // for now no explicit avoiding #257
        allowedHighwayTags.addAll(safeHighwayTags)
        allowedHighwayTags.addAll(avoidHighwayTags)
        allowedHighwayTags.remove("trunk")
        allowedHighwayTags.remove("trunk_link")
        allowedHighwayTags.add("cycleway")
        allowedHighwayTags.add("unclassified")
        allowedHighwayTags.add("road")
        // disallowed in some countries
        allowedHighwayTags.add("bridleway")

        hikingNetworkToCode["iwn"] = UNCHANGED.value
        hikingNetworkToCode["nwn"] = UNCHANGED.value
        hikingNetworkToCode["rwn"] = UNCHANGED.value
        hikingNetworkToCode["lwn"] = UNCHANGED.value

        maxPossibleSpeed = FERRY_SPEED

        init()
    }

    override fun getVersion(): Int {
        return 4
    }

    override fun defineWayBits(index: Int, shift1: Int): Int {
        var shift = shift1
        // first two bits are reserved for route handling in superclass
        shift = super.defineWayBits(index, shift)
        // larger value required - ferries are faster than pedestrians
        speedEncoder = EncodedDoubleValue("Speed", shift, speedBits, speedFactor, MEAN_SPEED.toLong(), maxPossibleSpeed)
        shift += speedEncoder.bits

        priorityWayEncoder = EncodedValue("PreferWay", shift, 3, 1.0, 0, 7)
        shift += priorityWayEncoder!!.bits
        return shift
    }

    override fun defineRelationBits(index: Int, shift: Int): Int {
        relationCodeEncoder = EncodedValue("RelationCode", shift, 3, 1.0, 0, 7)
        return shift + relationCodeEncoder!!.bits
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     */
    override fun defineTurnBits(index: Int, shift: Int): Int {
        return shift
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     *
     *
     *
     * @return `false`
     */
    override fun isTurnRestricted(flag: Long): Boolean {
        return false
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     *
     *
     *
     * @return 0
     */
    override fun getTurnCost(flag: Long): Double {
        return 0.0
    }

    override fun getTurnFlags(restricted: Boolean, costs: Double): Long {
        return 0
    }

    /**
     * Some ways are okay but not separate for pedestrians.
     *
     *
     */
    override fun acceptWay(way: ReaderWay): Long {
        val highwayValue = way.getTag("highway")
        if (highwayValue == null) {
            var acceptPotentially: Long = 0

            if (way.hasTag("route", ferries)) {
                val footTag = way.getTag("foot")
                if (footTag == null || "yes" == footTag)
                    acceptPotentially = acceptBit or ferryBit
            }

            // special case not for all acceptedRailways, only platform
            if (way.hasTag("railway", "platform"))
                acceptPotentially = acceptBit

            if (way.hasTag("man_made", "pier"))
                acceptPotentially = acceptBit

            return if (acceptPotentially != 0L) {
                if (way.hasTag(restrictions, restrictedValues) && !conditionalTagInspector.isRestrictedWayConditionallyPermitted(way)) 0 else acceptPotentially
            } else 0

        }

        val sacScale = way.getTag("sac_scale")
        if (sacScale != null) {
            if ("hiking" != sacScale && "mountain_hiking" != sacScale
                    && "demanding_mountain_hiking" != sacScale && "alpine_hiking" != sacScale)
            // other scales are too dangerous, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
                return 0
        }

        // no need to evaluate ferries or fords - already included here
        if (way.hasTag("foot", intendedValues))
            return acceptBit

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !conditionalTagInspector.isRestrictedWayConditionallyPermitted(way))
            return 0

        if (way.hasTag("sidewalk", sidewalkValues))
            return acceptBit

        if (!allowedHighwayTags.contains(highwayValue))
            return 0

        if (way.hasTag("motorroad", "yes"))
            return 0

        // do not get our feet wet, "yes" is already included above
        if (isBlockFords && (way.hasTag("highway", "ford") || way.hasTag("ford")))
            return 0

        return if (conditionalTagInspector.isPermittedWayConditionallyRestricted(way)) 0 else acceptBit

    }

    override fun handleRelationTags(relation: ReaderRelation, oldRelationFlags: Long): Long {
        var code = 0
        if (relation.hasTag("route", "hiking") || relation.hasTag("route", "foot")) {
            val `val` = hikingNetworkToCode[relation.getTag("network")]
            if (`val` != null)
                code = `val`
            else
                code = hikingNetworkToCode["lwn"]!!
        } else if (relation.hasTag("route", "ferry")) {
            code = PriorityCode.AVOID_IF_POSSIBLE.value
        }

        val oldCode = relationCodeEncoder!!.getValue(oldRelationFlags).toInt()
        return if (oldCode < code) relationCodeEncoder!!.setValue(0, code.toLong()) else oldRelationFlags
    }

    override fun handleWayTags(way: ReaderWay, allowed: Long, relationFlags: Long): Long {
        if (!isAccept(allowed))
            return 0

        var flags: Long = 0
        if (!isFerry(allowed)) {
            val sacScale = way.getTag("sac_scale")
            if (sacScale != null) {
                if ("hiking" == sacScale)
                    flags = speedEncoder.setDoubleValue(flags, MEAN_SPEED.toDouble())
                else
                    flags = speedEncoder.setDoubleValue(flags, SLOW_SPEED.toDouble())
            } else {
                flags = speedEncoder.setDoubleValue(flags, MEAN_SPEED.toDouble())
            }
            flags = flags or directionBitMask

            val isRoundabout = way.hasTag("junction", "roundabout") || way.hasTag("junction", "circular")
            if (isRoundabout)
                flags = setBool(flags, FlagEncoder.K_ROUNDABOUT, true)

        } else {
            val ferrySpeed = getFerrySpeed(way)
            flags = setSpeed(flags, ferrySpeed)
            flags = flags or directionBitMask
        }

        var priorityFromRelation = 0
        if (relationFlags != 0L)
            priorityFromRelation = relationCodeEncoder!!.getValue(relationFlags).toInt()

        flags = priorityWayEncoder!!.setValue(flags, handlePriority(way, priorityFromRelation).toLong())
        return flags
    }

    override fun getDouble(flags: Long, key: Int): Double {
        when (key) {
            PriorityWeighting.KEY -> return priorityWayEncoder!!.getValue(flags).toDouble() / BEST.value
            else -> return super.getDouble(flags, key)
        }
    }

    protected fun handlePriority(way: ReaderWay, priorityFromRelation: Int): Int {
        val weightToPrioMap = TreeMap<Double, Int>()
        if (priorityFromRelation == 0)
            weightToPrioMap[0.0] = UNCHANGED.value
        else
            weightToPrioMap[110.0] = priorityFromRelation

        collect(way, weightToPrioMap)

        // pick priority with biggest order value
        return weightToPrioMap.lastEntry().value
    }

    /**
     * @param weightToPrioMap associate a weight with every priority. This sorted map allows
     * subclasses to 'insert' more important priorities as well as overwrite determined priorities.
     */
    internal fun collect(way: ReaderWay, weightToPrioMap: TreeMap<Double, Int>) {
        val highway = way.getTag("highway")
        if (way.hasTag("foot", "designated"))
            weightToPrioMap[100.0] = PREFER.value

        val maxSpeed = getMaxSpeed(way)
        if (safeHighwayTags.contains(highway) || maxSpeed > 0 && maxSpeed <= 20) {
            weightToPrioMap[40.0] = PREFER.value
            if (way.hasTag("tunnel", intendedValues)) {
                if (way.hasTag("sidewalk", sidewalksNoValues))
                    weightToPrioMap[40.0] = AVOID_IF_POSSIBLE.value
                else
                    weightToPrioMap[40.0] = UNCHANGED.value
            }
        } else if (maxSpeed > 50 || avoidHighwayTags.contains(highway)) {
            if (!way.hasTag("sidewalk", sidewalkValues))
                weightToPrioMap[45.0] = WORST.value
        }

        if (way.hasTag("bicycle", "official") || way.hasTag("bicycle", "designated"))
            weightToPrioMap[44.0] = AVOID_IF_POSSIBLE.value
    }

    override fun supports(feature: Class<*>): Boolean {
        return if (super.supports(feature)) true else PriorityWeighting::class.java.isAssignableFrom(feature)

    }

    override fun toString(): String {
        return "streeto"
    }

    /*
     * This method is a current hack, to allow ferries to be actually faster than our current storable maxSpeed.
     */
    override fun getSpeed(flags: Long): Double {
        val speed = super.getSpeed(flags)
        return if (speed == maxSpeed) {
            // We cannot be sure if it was a long or a short trip
            AbstractFlagEncoder.SHORT_TRIP_FERRY_SPEED
        } else speed
    }

    companion object {
        internal val SLOW_SPEED = 2
        internal val MEAN_SPEED = 5
        internal val FERRY_SPEED = 15
    }
}
/**
 * Should be only instantiated via EncodingManager
 */

