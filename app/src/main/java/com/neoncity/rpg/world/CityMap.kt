package com.neoncity.rpg.world

/**
 * Manages all districts in the city and handles zone transitions.
 */
class CityMap {

    private val districts: MutableMap<String, District> = mutableMapOf()

    init {
        // Create all city districts
        districts["downtown"] = District.createDowntown()
        districts["corporate"] = District.createCorporate()
        districts["industrial"] = District.createIndustrial()
        districts["residential"] = District.createResidential()
        districts["docks"] = District.createDocks()
    }

    fun getDistrict(id: String): District? {
        return districts[id]
    }

    fun getStartingDistrict(): District {
        return districts["downtown"]!!
    }

    fun getAllDistricts(): List<District> {
        return districts.values.toList()
    }

    /**
     * Get the district connected to the current one via a transition tile.
     */
    fun getConnectedDistrict(currentDistrictId: String, transitionTarget: String): District? {
        return districts[transitionTarget]
    }

    /**
     * City layout (for reference):
     *
     *                 [Residential]
     *                      |
     *     [Industrial] - [Downtown] - [Corporate]
     *                      |
     *                   [Docks]
     */
    companion object {
        val DISTRICT_CONNECTIONS = mapOf(
            "downtown" to listOf("corporate", "industrial", "residential", "docks"),
            "corporate" to listOf("downtown"),
            "industrial" to listOf("downtown"),
            "residential" to listOf("downtown"),
            "docks" to listOf("downtown")
        )
    }
}
