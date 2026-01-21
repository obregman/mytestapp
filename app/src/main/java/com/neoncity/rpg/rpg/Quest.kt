package com.neoncity.rpg.rpg

/**
 * Quest data structure for the game's quest system.
 */
data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val objectives: List<Objective>,
    var status: QuestStatus = QuestStatus.ACTIVE,
    val rewards: QuestRewards = QuestRewards()
) {
    enum class QuestStatus {
        AVAILABLE,   // Can be accepted
        ACTIVE,      // Currently in progress
        COMPLETED,   // All objectives done
        TURNED_IN    // Rewards claimed
    }

    data class Objective(
        val description: String,
        val id: String,
        var completed: Boolean = false,
        var current: Int = 0,
        val target: Int = 1
    ) {
        fun isComplete(): Boolean = completed || current >= target
    }

    fun isComplete(): Boolean = objectives.all { it.isComplete() }

    fun completeObjective(objectiveId: String): Boolean {
        val objective = objectives.find { it.id == objectiveId }
        if (objective != null && !objective.completed) {
            objective.completed = true
            if (isComplete()) {
                status = QuestStatus.COMPLETED
            }
            return true
        }
        return false
    }

    fun incrementObjective(objectiveId: String, amount: Int = 1): Boolean {
        val objective = objectives.find { it.id == objectiveId }
        if (objective != null && !objective.completed) {
            objective.current += amount
            if (objective.current >= objective.target) {
                objective.completed = true
            }
            if (isComplete()) {
                status = QuestStatus.COMPLETED
            }
            return true
        }
        return false
    }
}

data class QuestRewards(
    val experience: Int = 100,
    val credits: Int = 200,
    val items: List<String> = emptyList()
)
