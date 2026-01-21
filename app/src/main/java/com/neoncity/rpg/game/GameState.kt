package com.neoncity.rpg.game

import com.neoncity.rpg.entity.NPC
import com.neoncity.rpg.entity.Player
import com.neoncity.rpg.rpg.Inventory
import com.neoncity.rpg.rpg.Quest
import com.neoncity.rpg.rpg.QuestManager
import com.neoncity.rpg.world.CityMap
import com.neoncity.rpg.world.District

/**
 * Central game state manager.
 * Holds all game data and manages state transitions.
 */
class GameState {

    enum class Screen {
        TITLE,
        PLAYING,
        DIALOGUE,
        INVENTORY,
        QUEST_LOG,
        PAUSE
    }

    var currentScreen: Screen = Screen.TITLE
    var showDebug: Boolean = false

    // World
    val cityMap: CityMap = CityMap()
    var currentDistrict: District = cityMap.getStartingDistrict()

    // Player
    val player: Player = Player(
        name = "Runner",
        x = 5f,
        y = 5f
    )

    // NPCs in current district
    val npcs: MutableList<NPC> = mutableListOf()

    // RPG Systems
    val inventory: Inventory = Inventory()
    val questManager: QuestManager = QuestManager()

    // Dialogue state
    var activeDialogue: DialogueState? = null

    // Camera position (in tile coordinates)
    var cameraX: Float = 0f
    var cameraY: Float = 0f

    // Game time (in-game hours)
    var gameTime: Float = 8f  // Start at 8 AM
    var daysPassed: Int = 0

    init {
        loadDistrict(currentDistrict)
        initializeStartingQuests()
    }

    fun update() {
        when (currentScreen) {
            Screen.PLAYING -> {
                updateGameplay()
            }
            Screen.DIALOGUE -> {
                // Dialogue updates handled by input
            }
            else -> {}
        }
    }

    private fun updateGameplay() {
        // Update player
        player.update()

        // Update camera to follow player
        cameraX = player.x - 5f
        cameraY = player.y - 4f

        // Update NPCs
        npcs.forEach { it.update() }

        // Update game time (1 real second = 1 game minute)
        gameTime += 0.001f
        if (gameTime >= 24f) {
            gameTime = 0f
            daysPassed++
        }

        // Check for NPC interactions
        checkInteractions()

        // Update quests
        questManager.updateQuests(this)
    }

    private fun checkInteractions() {
        for (npc in npcs) {
            val dx = player.x - npc.x
            val dy = player.y - npc.y
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)

            npc.isPlayerNear = dist < 1.5f
        }
    }

    fun loadDistrict(district: District) {
        currentDistrict = district
        npcs.clear()
        npcs.addAll(district.npcs)

        // Set player at district entry point
        player.x = district.entryX
        player.y = district.entryY
    }

    fun changeDistrict(districtId: String) {
        val district = cityMap.getDistrict(districtId)
        if (district != null) {
            loadDistrict(district)
        }
    }

    private fun initializeStartingQuests() {
        // Add the main story quest
        questManager.addQuest(
            Quest(
                id = "main_01",
                title = "Welcome to Neon City",
                description = "Find the information broker in the Downtown district.",
                objectives = listOf(
                    Quest.Objective("Find Max at the Night Owl Bar", "find_max", false)
                )
            )
        )
    }

    fun startDialogue(npc: NPC) {
        activeDialogue = DialogueState(npc)
        currentScreen = Screen.DIALOGUE
    }

    fun endDialogue() {
        activeDialogue = null
        currentScreen = Screen.PLAYING
    }

    fun startGame() {
        currentScreen = Screen.PLAYING
    }

    fun toggleInventory() {
        currentScreen = if (currentScreen == Screen.INVENTORY) {
            Screen.PLAYING
        } else {
            Screen.INVENTORY
        }
    }

    fun toggleQuestLog() {
        currentScreen = if (currentScreen == Screen.QUEST_LOG) {
            Screen.PLAYING
        } else {
            Screen.QUEST_LOG
        }
    }
}

data class DialogueState(
    val npc: NPC,
    var currentNodeIndex: Int = 0
) {
    fun getCurrentText(): String {
        return npc.dialogue.getOrNull(currentNodeIndex)?.text ?: ""
    }

    fun getResponses(): List<String> {
        return npc.dialogue.getOrNull(currentNodeIndex)?.responses ?: listOf("Goodbye")
    }

    fun selectResponse(index: Int): Boolean {
        val node = npc.dialogue.getOrNull(currentNodeIndex)
        if (node != null && index < node.nextNodes.size) {
            val nextIndex = node.nextNodes[index]
            if (nextIndex >= 0 && nextIndex < npc.dialogue.size) {
                currentNodeIndex = nextIndex
                return true
            }
        }
        return false
    }
}
