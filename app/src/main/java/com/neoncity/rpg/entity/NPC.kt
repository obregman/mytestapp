package com.neoncity.rpg.entity

import android.graphics.Color

/**
 * Non-player character with dialogue, quests, and behavior.
 */
class NPC(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    val npcType: NPCType = NPCType.CIVILIAN
) {
    enum class NPCType {
        CIVILIAN,       // Regular city dweller
        MERCHANT,       // Can buy/sell items
        QUEST_GIVER,    // Has quests to offer
        INFORMANT,      // Provides information/tips
        GUARD,          // Security/police
        GANG_MEMBER     // Hostile or can be allied
    }

    // Appearance (pixel colors)
    var skinColor: Int = Color.rgb(220, 180, 160)
    var hairColor: Int = Color.rgb(50, 40, 30)
    var bodyColor: Int = Color.rgb(60, 60, 80)
    var accentColor: Int = Color.rgb(100, 50, 50)

    // State
    var isPlayerNear: Boolean = false
    var hasQuest: Boolean = false
    var questId: String? = null

    // Dialogue tree
    val dialogue: MutableList<DialogueNode> = mutableListOf()

    // Behavior
    var behavior: NPCBehavior = NPCBehavior.IDLE
    var patrolPoints: List<Pair<Float, Float>> = emptyList()
    private var currentPatrolIndex: Int = 0

    // Shop inventory (for merchants)
    var shopItems: MutableList<String> = mutableListOf()

    fun update() {
        when (behavior) {
            NPCBehavior.IDLE -> {
                // Just stand there
            }
            NPCBehavior.PATROL -> {
                if (patrolPoints.isNotEmpty()) {
                    val target = patrolPoints[currentPatrolIndex]
                    val dx = target.first - x
                    val dy = target.second - y
                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                    if (dist < 0.2f) {
                        currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.size
                    } else {
                        val speed = 0.02f
                        x += (dx / dist) * speed
                        y += (dy / dist) * speed
                    }
                }
            }
            NPCBehavior.WANDER -> {
                // Random movement
                if (Math.random() < 0.01) {
                    x += (Math.random() * 0.1 - 0.05).toFloat()
                    y += (Math.random() * 0.1 - 0.05).toFloat()
                }
            }
            NPCBehavior.FOLLOW_PLAYER -> {
                // Would need player reference
            }
        }
    }

    enum class NPCBehavior {
        IDLE,
        PATROL,
        WANDER,
        FOLLOW_PLAYER
    }

    companion object {
        fun createCivilian(id: String, name: String, x: Float, y: Float): NPC {
            return NPC(id, name, x, y, NPCType.CIVILIAN).apply {
                dialogue.add(DialogueNode(
                    text = "Hey there. Crazy night, huh?",
                    responses = listOf("Yeah, it's wild out here.", "Stay safe."),
                    nextNodes = listOf(-1, -1)
                ))
                // Random appearance
                bodyColor = Color.rgb(
                    (40..80).random(),
                    (40..80).random(),
                    (60..100).random()
                )
            }
        }

        fun createMerchant(id: String, name: String, x: Float, y: Float, items: List<String>): NPC {
            return NPC(id, name, x, y, NPCType.MERCHANT).apply {
                shopItems.addAll(items)
                accentColor = Color.rgb(200, 150, 50)  // Gold accent
                dialogue.add(DialogueNode(
                    text = "Welcome! Looking to buy or sell?",
                    responses = listOf("Show me what you've got.", "Maybe later."),
                    nextNodes = listOf(1, -1)
                ))
                dialogue.add(DialogueNode(
                    text = "Take a look at my wares.",
                    responses = listOf("Thanks."),
                    nextNodes = listOf(-1)
                ))
            }
        }

        fun createQuestGiver(id: String, name: String, x: Float, y: Float, questId: String, dialogueNodes: List<DialogueNode>): NPC {
            return NPC(id, name, x, y, NPCType.QUEST_GIVER).apply {
                hasQuest = true
                this.questId = questId
                accentColor = Color.rgb(255, 200, 0)  // Quest yellow
                dialogue.addAll(dialogueNodes)
            }
        }

        fun createInformant(id: String, name: String, x: Float, y: Float): NPC {
            return NPC(id, name, x, y, NPCType.INFORMANT).apply {
                accentColor = Color.rgb(0, 200, 200)  // Cyan
                dialogue.add(DialogueNode(
                    text = "You looking for information? Everything has a price in Neon City.",
                    responses = listOf("What do you know?", "Nevermind."),
                    nextNodes = listOf(1, -1)
                ))
                dialogue.add(DialogueNode(
                    text = "The corpo towers control everything. But there are ways around their surveillance... if you know where to look.",
                    responses = listOf("Tell me more.", "Thanks for the tip."),
                    nextNodes = listOf(-1, -1)
                ))
            }
        }
    }
}

data class DialogueNode(
    val text: String,
    val responses: List<String> = listOf("Goodbye"),
    val nextNodes: List<Int> = listOf(-1),  // -1 means end dialogue
    val questAction: String? = null  // Optional quest trigger
)
