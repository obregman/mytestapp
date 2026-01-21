package com.neoncity.rpg.world

import android.graphics.Color

/**
 * Represents a single tile in the isometric world.
 */
data class Tile(
    val type: TileType,
    var variant: Int = 0,
    var height: Int = 1,        // For buildings/walls (in tile units)
    var color: Int = Color.GRAY,
    var isWalkable: Boolean = true,
    var isInteractable: Boolean = false,
    var hasNeonLight: Boolean = false,
    var neonColor: Int = Color.CYAN,
    var linkedDistrictId: String? = null  // For transition tiles
) {
    companion object {
        fun ground(): Tile = Tile(
            type = TileType.GROUND,
            color = Color.rgb(40, 40, 50),
            isWalkable = true
        )

        fun road(variant: Int = 0): Tile = Tile(
            type = TileType.ROAD,
            variant = variant,
            color = Color.rgb(35, 35, 45),
            isWalkable = true
        )

        fun sidewalk(): Tile = Tile(
            type = TileType.SIDEWALK,
            color = Color.rgb(55, 55, 65),
            isWalkable = true
        )

        fun building(height: Int, color: Int, hasNeon: Boolean = false, neonColor: Int = Color.CYAN): Tile = Tile(
            type = TileType.BUILDING,
            height = height,
            color = color,
            isWalkable = false,
            hasNeonLight = hasNeon,
            neonColor = neonColor
        )

        fun wall(height: Int = 2): Tile = Tile(
            type = TileType.WALL,
            height = height,
            color = Color.rgb(50, 50, 60),
            isWalkable = false
        )

        fun water(): Tile = Tile(
            type = TileType.WATER,
            color = Color.rgb(20, 40, 80),
            isWalkable = false
        )

        fun park(variant: Int = 0): Tile = Tile(
            type = TileType.PARK,
            variant = variant,
            color = Color.rgb(30, 60, 35),
            isWalkable = true
        )

        fun door(linkedDistrict: String? = null, color: Int = Color.rgb(80, 60, 40), neonColor: Int = Color.CYAN): Tile = Tile(
            type = TileType.DOOR,
            color = color,
            isWalkable = true,
            isInteractable = true,
            hasNeonLight = true,
            neonColor = neonColor,
            linkedDistrictId = linkedDistrict
        )

        fun transition(linkedDistrict: String): Tile = Tile(
            type = TileType.TRANSITION,
            color = Color.rgb(60, 60, 80),
            isWalkable = true,
            linkedDistrictId = linkedDistrict
        )
    }
}

enum class TileType {
    GROUND,
    ROAD,
    SIDEWALK,
    BUILDING,
    WALL,
    WATER,
    PARK,
    DOOR,
    TRANSITION
}
