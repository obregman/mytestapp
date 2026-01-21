package com.neoncity.rpg.rpg

import com.neoncity.rpg.game.GameState

/**
 * Manages all quests in the game - tracking, updating, and completing.
 */
class QuestManager {

    private val quests: MutableMap<String, Quest> = mutableMapOf()

    fun addQuest(quest: Quest) {
        quests[quest.id] = quest
    }

    fun getQuest(questId: String): Quest? {
        return quests[questId]
    }

    fun getActiveQuests(): List<Quest> {
        return quests.values.filter { it.status == Quest.QuestStatus.ACTIVE }
    }

    fun getCompletedQuests(): List<Quest> {
        return quests.values.filter { it.status == Quest.QuestStatus.COMPLETED || it.status == Quest.QuestStatus.TURNED_IN }
    }

    fun completeObjective(questId: String, objectiveId: String): Boolean {
        val quest = quests[questId] ?: return false
        return quest.completeObjective(objectiveId)
    }

    fun isQuestComplete(questId: String): Boolean {
        val quest = quests[questId] ?: return false
        return quest.isComplete()
    }

    /**
     * Update quest objectives based on game state.
     * Called each frame to check for objective completion.
     */
    fun updateQuests(gameState: GameState) {
        // Check location-based objectives
        for (quest in getActiveQuests()) {
            for (objective in quest.objectives) {
                if (objective.completed) continue

                when (objective.id) {
                    // Main quest: Find Max
                    "find_max" -> {
                        val maxNpc = gameState.npcs.find { it.id == "max" }
                        if (maxNpc != null && maxNpc.isPlayerNear) {
                            objective.completed = true
                        }
                    }

                    // Example: Reach a location
                    "reach_corporate" -> {
                        if (gameState.currentDistrict.id == "corporate") {
                            objective.completed = true
                        }
                    }

                    // Example: Collect items
                    "collect_data_chip" -> {
                        if (gameState.inventory.hasItem("data_chip")) {
                            objective.completed = true
                        }
                    }
                }
            }

            // Update quest status if all objectives complete
            if (quest.isComplete() && quest.status == Quest.QuestStatus.ACTIVE) {
                quest.status = Quest.QuestStatus.COMPLETED
            }
        }
    }

    /**
     * Claim rewards for a completed quest.
     */
    fun claimRewards(questId: String, gameState: GameState): Boolean {
        val quest = quests[questId] ?: return false

        if (quest.status != Quest.QuestStatus.COMPLETED) {
            return false
        }

        // Grant rewards
        gameState.player.addExperience(quest.rewards.experience)
        gameState.inventory.addCredits(quest.rewards.credits)

        for (itemId in quest.rewards.items) {
            val item = createItemFromId(itemId)
            if (item != null) {
                gameState.inventory.addItem(item)
            }
        }

        quest.status = Quest.QuestStatus.TURNED_IN

        // Potentially trigger follow-up quests
        triggerFollowUpQuest(questId)

        return true
    }

    private fun createItemFromId(itemId: String): Item? {
        return when (itemId) {
            "medkit" -> Item.createMedkit()
            "stim_pack" -> Item.createStimPack()
            "hack_chip" -> Item.createHackChip()
            "data_chip" -> Item.createDataChip()
            "military_stim" -> Item.createMilitaryStim()
            "stealth_chip" -> Item.createStealthChip()
            "emp_grenade" -> Item.createEMPGrenade()
            else -> null
        }
    }

    private fun triggerFollowUpQuest(completedQuestId: String) {
        when (completedQuestId) {
            "main_01" -> {
                // After finding Max, add the next main quest
                addQuest(Quest(
                    id = "main_02",
                    title = "Corporate Heist",
                    description = "Infiltrate Nexus Tower and retrieve the stolen data chip.",
                    objectives = listOf(
                        Quest.Objective("Reach the Corporate District", "reach_corporate", false),
                        Quest.Objective("Enter Nexus Tower", "enter_nexus", false),
                        Quest.Objective("Retrieve the data chip", "collect_data_chip", false),
                        Quest.Objective("Return to Max", "return_to_max", false)
                    ),
                    rewards = QuestRewards(
                        experience = 500,
                        credits = 1000,
                        items = listOf("military_stim", "hack_chip")
                    )
                ))
            }

            "main_02" -> {
                // After the heist, continue the story
                addQuest(Quest(
                    id = "main_03",
                    title = "The Truth",
                    description = "Decrypt the data chip and discover what the corporations are hiding.",
                    objectives = listOf(
                        Quest.Objective("Find a hacker in the Industrial Zone", "find_hacker", false),
                        Quest.Objective("Decrypt the data chip", "decrypt_chip", false),
                        Quest.Objective("Decide what to do with the information", "make_choice", false)
                    ),
                    rewards = QuestRewards(
                        experience = 1000,
                        credits = 2000
                    )
                ))
            }
        }
    }

    /**
     * Handle quest-related actions from dialogue choices.
     */
    fun handleQuestAction(action: String, gameState: GameState) {
        when (action) {
            "accept_main_02" -> {
                // Accept the heist quest from Max
                completeObjective("main_01", "find_max")
                claimRewards("main_01", gameState)
            }
        }
    }
}
