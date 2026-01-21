package com.neoncity.rpg.world

import android.graphics.Color
import com.neoncity.rpg.entity.DialogueNode
import com.neoncity.rpg.entity.NPC

/**
 * A district is a distinct area of the city with its own tiles, NPCs, and atmosphere.
 */
class District(
    val id: String,
    val name: String,
    val width: Int = 30,
    val height: Int = 30,
    val entryX: Float = 15f,
    val entryY: Float = 15f
) {
    private val tiles: Array<Array<Tile?>> = Array(height) { arrayOfNulls<Tile>(width) }
    val npcs: MutableList<NPC> = mutableListOf()

    var isNight: Boolean = true
    var ambientColor: Int = Color.rgb(20, 20, 40)

    fun getTile(x: Int, y: Int): Tile? {
        if (x < 0 || x >= width || y < 0 || y >= height) return null
        return tiles[y][x]
    }

    fun setTile(x: Int, y: Int, tile: Tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[y][x] = tile
        }
    }

    fun isWalkable(x: Int, y: Int): Boolean {
        val tile = getTile(x, y)
        return tile?.isWalkable ?: false
    }

    fun addNPC(npc: NPC) {
        npcs.add(npc)
    }

    companion object {
        /**
         * Create the Downtown district - the starting area with shops and NPCs.
         */
        fun createDowntown(): District {
            val district = District(
                id = "downtown",
                name = "Downtown",
                width = 40,
                height = 40,
                entryX = 20f,
                entryY = 35f
            )

            // Fill with base tiles
            for (y in 0 until district.height) {
                for (x in 0 until district.width) {
                    district.setTile(x, y, Tile.ground())
                }
            }

            // Main road (horizontal)
            for (x in 0 until district.width) {
                district.setTile(x, 20, Tile.road(if (x % 4 == 0) 1 else 0))
                district.setTile(x, 21, Tile.road())
                district.setTile(x, 19, Tile.sidewalk())
                district.setTile(x, 22, Tile.sidewalk())
            }

            // Side road (vertical)
            for (y in 0 until district.height) {
                district.setTile(20, y, Tile.road(if (y % 4 == 0) 1 else 0))
                district.setTile(21, y, Tile.road())
                district.setTile(19, y, Tile.sidewalk())
                district.setTile(22, y, Tile.sidewalk())
            }

            // Buildings - North side
            createBuildingBlock(district, 2, 2, 8, 6, Color.rgb(60, 50, 70), 4, true, Color.CYAN)
            createBuildingBlock(district, 12, 2, 6, 5, Color.rgb(70, 60, 60), 3, true, Color.MAGENTA)
            createBuildingBlock(district, 24, 2, 10, 7, Color.rgb(50, 60, 70), 5, true, Color.YELLOW)

            // Buildings - South side
            createBuildingBlock(district, 2, 25, 7, 5, Color.rgb(65, 55, 65), 3, false, Color.CYAN)
            createBuildingBlock(district, 11, 25, 6, 6, Color.rgb(55, 65, 75), 4, true, Color.rgb(255, 100, 0))
            createBuildingBlock(district, 24, 25, 8, 5, Color.rgb(60, 60, 80), 3, true, Color.GREEN)

            // Buildings - East side
            createBuildingBlock(district, 30, 8, 8, 4, Color.rgb(70, 50, 60), 3, true, Color.MAGENTA)
            createBuildingBlock(district, 32, 26, 6, 6, Color.rgb(55, 55, 70), 4, false, Color.CYAN)

            // Buildings - West side
            createBuildingBlock(district, 2, 10, 5, 4, Color.rgb(60, 60, 70), 2, false, Color.CYAN)

            // Night Owl Bar (quest location)
            district.setTile(15, 24, Tile.door("bar_interior", Color.rgb(80, 40, 40), Color.rgb(255, 100, 0)))

            // Add park area
            for (y in 32 until 38) {
                for (x in 5 until 15) {
                    district.setTile(x, y, Tile.park(if ((x + y) % 3 == 0) 1 else 0))
                }
            }

            // Zone transitions
            district.setTile(0, 20, Tile.transition("industrial"))
            district.setTile(0, 21, Tile.transition("industrial"))
            district.setTile(39, 20, Tile.transition("corporate"))
            district.setTile(39, 21, Tile.transition("corporate"))
            district.setTile(20, 0, Tile.transition("residential"))
            district.setTile(21, 0, Tile.transition("residential"))
            district.setTile(20, 39, Tile.transition("docks"))
            district.setTile(21, 39, Tile.transition("docks"))

            // Add NPCs
            district.addNPC(NPC.createQuestGiver(
                "max",
                "Max",
                14f,
                23f,
                "main_01",
                listOf(
                    DialogueNode(
                        text = "You must be the new runner. I've been expecting you. The name's Max.",
                        responses = listOf("I'm looking for work.", "What is this place?"),
                        nextNodes = listOf(1, 2)
                    ),
                    DialogueNode(
                        text = "Good. I have a job that needs doing. A data chip went missing from a corpo facility. I need someone to retrieve it.",
                        responses = listOf("I'm interested.", "Sounds dangerous."),
                        nextNodes = listOf(3, 3)
                    ),
                    DialogueNode(
                        text = "This is the Night Owl. A place for people who don't want to be found. We deal in information here.",
                        responses = listOf("I see. About that work...", "Interesting."),
                        nextNodes = listOf(1, -1)
                    ),
                    DialogueNode(
                        text = "It pays well. Head to the Corporate district. The chip is in Nexus Tower, floor 15. Come back when you have it.",
                        responses = listOf("I'll get it done."),
                        nextNodes = listOf(-1),
                        questAction = "accept_main_02"
                    )
                )
            ))

            district.addNPC(NPC.createMerchant(
                "vendor_tech",
                "Rico",
                8f,
                18f,
                listOf("medkit", "stim_pack", "hack_chip")
            ))

            district.addNPC(NPC.createInformant("info_01", "Whisper", 28f, 18f))

            district.addNPC(NPC.createCivilian("civ_01", "Street Kid", 25f, 30f).apply {
                behavior = NPC.NPCBehavior.WANDER
            })

            district.addNPC(NPC.createCivilian("civ_02", "Worker", 10f, 33f))

            return district
        }

        /**
         * Create the Corporate district - high-security area with corpo buildings.
         */
        fun createCorporate(): District {
            val district = District(
                id = "corporate",
                name = "Corporate District",
                width = 40,
                height = 40,
                entryX = 2f,
                entryY = 20f
            )

            // Fill with clean sidewalks
            for (y in 0 until district.height) {
                for (x in 0 until district.width) {
                    district.setTile(x, y, Tile.sidewalk())
                }
            }

            // Clean roads
            for (x in 0 until district.width) {
                district.setTile(x, 15, Tile.road())
                district.setTile(x, 16, Tile.road())
                district.setTile(x, 25, Tile.road())
                district.setTile(x, 26, Tile.road())
            }

            // Massive corporate towers
            createBuildingBlock(district, 8, 2, 12, 10, Color.rgb(40, 50, 70), 8, true, Color.rgb(0, 150, 255))
            createBuildingBlock(district, 25, 2, 10, 8, Color.rgb(50, 50, 60), 7, true, Color.WHITE)
            createBuildingBlock(district, 10, 20, 8, 6, Color.rgb(45, 55, 65), 6, true, Color.rgb(100, 200, 255))
            createBuildingBlock(district, 25, 30, 12, 8, Color.rgb(35, 45, 55), 9, true, Color.rgb(0, 200, 200))

            // Nexus Tower (mission target)
            createBuildingBlock(district, 30, 18, 8, 8, Color.rgb(30, 40, 60), 10, true, Color.rgb(255, 0, 100))

            // Entry door to Nexus Tower
            district.setTile(30, 22, Tile.door("nexus_interior", Color.rgb(50, 50, 70), Color.rgb(255, 0, 100)))

            // Transition back to downtown
            district.setTile(0, 15, Tile.transition("downtown"))
            district.setTile(0, 16, Tile.transition("downtown"))

            // Add guards
            district.addNPC(NPC(
                "guard_01",
                "Security Guard",
                20f,
                20f,
                NPC.NPCType.GUARD
            ).apply {
                accentColor = Color.rgb(50, 50, 150)
                dialogue.add(DialogueNode(
                    text = "Move along, citizen. This area is monitored.",
                    responses = listOf("Sure thing."),
                    nextNodes = listOf(-1)
                ))
                behavior = NPC.NPCBehavior.PATROL
                patrolPoints = listOf(
                    Pair(15f, 18f),
                    Pair(25f, 18f),
                    Pair(25f, 28f),
                    Pair(15f, 28f)
                )
            })

            return district
        }

        /**
         * Create the Industrial district - factories, warehouses, gang territory.
         */
        fun createIndustrial(): District {
            val district = District(
                id = "industrial",
                name = "Industrial Zone",
                width = 40,
                height = 40,
                entryX = 38f,
                entryY = 20f
            )

            // Dirty ground
            for (y in 0 until district.height) {
                for (x in 0 until district.width) {
                    district.setTile(x, y, Tile.ground().copy(color = Color.rgb(35, 35, 40)))
                }
            }

            // Broken roads
            for (x in 0 until district.width) {
                district.setTile(x, 20, Tile.road())
                district.setTile(x, 21, Tile.road())
            }

            // Warehouses
            createBuildingBlock(district, 5, 5, 10, 8, Color.rgb(50, 45, 40), 2, false, Color.CYAN)
            createBuildingBlock(district, 20, 5, 12, 6, Color.rgb(45, 40, 35), 2, true, Color.rgb(255, 100, 0))
            createBuildingBlock(district, 5, 25, 8, 6, Color.rgb(55, 50, 45), 2, false, Color.CYAN)
            createBuildingBlock(district, 18, 28, 10, 8, Color.rgb(40, 35, 30), 3, true, Color.rgb(200, 50, 50))

            // Polluted water canal
            for (y in 0 until district.height) {
                district.setTile(35, y, Tile.water())
                district.setTile(36, y, Tile.water())
            }

            // Transition to downtown
            district.setTile(39, 20, Tile.transition("downtown"))
            district.setTile(39, 21, Tile.transition("downtown"))

            // Gang members
            district.addNPC(NPC(
                "gang_01",
                "Rust Devil",
                15f,
                15f,
                NPC.NPCType.GANG_MEMBER
            ).apply {
                accentColor = Color.rgb(200, 50, 50)
                hairColor = Color.rgb(200, 50, 50)
                dialogue.add(DialogueNode(
                    text = "You're in Rust Devil territory now. Best watch your step.",
                    responses = listOf("I'm just passing through.", "You don't scare me."),
                    nextNodes = listOf(-1, 1)
                ))
                dialogue.add(DialogueNode(
                    text = "Heh. Brave words. But brave doesn't stop a bullet. Get lost.",
                    responses = listOf("Fine."),
                    nextNodes = listOf(-1)
                ))
            })

            return district
        }

        /**
         * Create the Residential district - apartment blocks, lived-in feel.
         */
        fun createResidential(): District {
            val district = District(
                id = "residential",
                name = "The Blocks",
                width = 40,
                height = 40,
                entryX = 20f,
                entryY = 38f
            )

            // Fill with ground
            for (y in 0 until district.height) {
                for (x in 0 until district.width) {
                    district.setTile(x, y, Tile.sidewalk())
                }
            }

            // Streets
            for (y in 0 until district.height) {
                district.setTile(12, y, Tile.road())
                district.setTile(13, y, Tile.road())
                district.setTile(27, y, Tile.road())
                district.setTile(28, y, Tile.road())
            }
            for (x in 0 until district.width) {
                district.setTile(x, 20, Tile.road())
                district.setTile(x, 21, Tile.road())
            }

            // Apartment buildings
            createBuildingBlock(district, 2, 2, 8, 8, Color.rgb(65, 60, 55), 5, true, Color.rgb(255, 200, 100))
            createBuildingBlock(district, 16, 2, 10, 6, Color.rgb(60, 55, 60), 4, true, Color.rgb(200, 150, 255))
            createBuildingBlock(district, 30, 5, 7, 7, Color.rgb(55, 55, 65), 5, true, Color.rgb(100, 255, 200))
            createBuildingBlock(district, 2, 25, 8, 6, Color.rgb(60, 60, 60), 4, true, Color.rgb(255, 150, 100))
            createBuildingBlock(district, 16, 28, 9, 8, Color.rgb(55, 50, 55), 5, true, Color.rgb(255, 255, 100))
            createBuildingBlock(district, 30, 25, 8, 8, Color.rgb(65, 55, 60), 4, false, Color.CYAN)

            // Small park
            for (y in 10 until 15) {
                for (x in 2 until 8) {
                    district.setTile(x, y, Tile.park())
                }
            }

            // Transition to downtown
            district.setTile(20, 39, Tile.transition("downtown"))
            district.setTile(21, 39, Tile.transition("downtown"))

            // Residents
            district.addNPC(NPC.createCivilian("resident_01", "Old Timer", 5f, 12f).apply {
                dialogue.clear()
                dialogue.add(DialogueNode(
                    text = "I've lived in these blocks for 40 years. Seen the city change... and not for the better.",
                    responses = listOf("What was it like before?", "Times change."),
                    nextNodes = listOf(1, -1)
                ))
                dialogue.add(DialogueNode(
                    text = "People used to know their neighbors. Now everyone's plugged into their screens, afraid of the corps, afraid of each other.",
                    responses = listOf("Sounds rough.", "Thanks for sharing."),
                    nextNodes = listOf(-1, -1)
                ))
            })

            return district
        }

        /**
         * Create the Docks district - port area, smuggling, black market.
         */
        fun createDocks(): District {
            val district = District(
                id = "docks",
                name = "Harbor District",
                width = 40,
                height = 40,
                entryX = 20f,
                entryY = 2f
            )

            // Water on most of the south
            for (y in 0 until district.height) {
                for (x in 0 until district.width) {
                    if (y > 25) {
                        district.setTile(x, y, Tile.water())
                    } else {
                        district.setTile(x, y, Tile.ground().copy(color = Color.rgb(40, 40, 45)))
                    }
                }
            }

            // Dock platforms
            for (x in 5 until 35) {
                district.setTile(x, 24, Tile.sidewalk())
                district.setTile(x, 25, Tile.sidewalk())
            }

            // Pier extending into water
            for (y in 25 until 35) {
                for (x in 18 until 23) {
                    district.setTile(x, y, Tile.sidewalk().copy(color = Color.rgb(60, 50, 40)))
                }
            }

            // Warehouses
            createBuildingBlock(district, 5, 5, 10, 6, Color.rgb(50, 45, 40), 2, false, Color.CYAN)
            createBuildingBlock(district, 25, 8, 8, 5, Color.rgb(45, 40, 35), 2, true, Color.rgb(100, 200, 100))

            // Road
            for (x in 0 until district.width) {
                district.setTile(x, 2, Tile.road())
                district.setTile(x, 3, Tile.road())
            }

            // Transition to downtown
            district.setTile(20, 0, Tile.transition("downtown"))
            district.setTile(21, 0, Tile.transition("downtown"))

            // Black market dealer
            district.addNPC(NPC.createMerchant(
                "black_market",
                "Shadow",
                20f,
                30f,
                listOf("military_stim", "stealth_chip", "emp_grenade")
            ).apply {
                dialogue.clear()
                dialogue.add(DialogueNode(
                    text = "Looking for something... special? I've got gear you won't find in any store.",
                    responses = listOf("Show me.", "Maybe later."),
                    nextNodes = listOf(1, -1)
                ))
                dialogue.add(DialogueNode(
                    text = "Keep it quiet. The corps don't like competition.",
                    responses = listOf("Understood."),
                    nextNodes = listOf(-1)
                ))
                accentColor = Color.rgb(80, 80, 80)
                bodyColor = Color.rgb(30, 30, 35)
            })

            return district
        }

        private fun createBuildingBlock(
            district: District,
            startX: Int,
            startY: Int,
            width: Int,
            height: Int,
            color: Int,
            buildingHeight: Int,
            hasNeon: Boolean,
            neonColor: Int
        ) {
            for (y in startY until startY + height) {
                for (x in startX until startX + width) {
                    if (x < district.width && y < district.height) {
                        // Edge tiles get neon, inner tiles don't
                        val isEdge = x == startX || x == startX + width - 1 || y == startY || y == startY + height - 1
                        district.setTile(x, y, Tile.building(
                            height = buildingHeight,
                            color = color,
                            hasNeon = hasNeon && isEdge && (x + y) % 3 == 0,
                            neonColor = neonColor
                        ))
                    }
                }
            }
        }
    }
}
