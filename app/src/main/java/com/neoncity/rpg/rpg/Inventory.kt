package com.neoncity.rpg.rpg

import android.graphics.Color

/**
 * Player inventory system for items and credits.
 */
class Inventory(
    var credits: Int = 500,
    val maxSlots: Int = 32
) {
    val items: MutableList<Item> = mutableListOf()

    init {
        // Starting items
        addItem(Item.createMedkit())
        addItem(Item.createStimPack())
    }

    fun addItem(item: Item): Boolean {
        if (items.size >= maxSlots) {
            return false
        }

        // Check for stackable items
        val existing = items.find { it.id == item.id && it.stackable }
        if (existing != null) {
            existing.quantity += item.quantity
            return true
        }

        items.add(item)
        return true
    }

    fun removeItem(itemId: String, quantity: Int = 1): Boolean {
        val item = items.find { it.id == itemId } ?: return false

        if (item.quantity > quantity) {
            item.quantity -= quantity
            return true
        } else if (item.quantity == quantity) {
            items.remove(item)
            return true
        }

        return false
    }

    fun hasItem(itemId: String, quantity: Int = 1): Boolean {
        val item = items.find { it.id == itemId } ?: return false
        return item.quantity >= quantity
    }

    fun getItem(itemId: String): Item? {
        return items.find { it.id == itemId }
    }

    fun addCredits(amount: Int) {
        credits += amount
    }

    fun spendCredits(amount: Int): Boolean {
        if (credits >= amount) {
            credits -= amount
            return true
        }
        return false
    }
}

data class Item(
    val id: String,
    val name: String,
    val description: String,
    val type: ItemType,
    val symbol: String,      // Single character for pixel display
    val color: Int,
    var quantity: Int = 1,
    val stackable: Boolean = true,
    val value: Int = 10,
    val effect: ItemEffect? = null
) {
    enum class ItemType {
        CONSUMABLE,
        WEAPON,
        ARMOR,
        KEY_ITEM,
        CYBERWARE,
        CHIP
    }

    companion object {
        fun createMedkit(): Item = Item(
            id = "medkit",
            name = "Medkit",
            description = "Restores 50 health.",
            type = ItemType.CONSUMABLE,
            symbol = "+",
            color = Color.rgb(255, 100, 100),
            value = 50,
            effect = ItemEffect(ItemEffect.EffectType.HEAL, 50)
        )

        fun createStimPack(): Item = Item(
            id = "stim_pack",
            name = "Stim Pack",
            description = "Restores 25 energy.",
            type = ItemType.CONSUMABLE,
            symbol = "S",
            color = Color.rgb(100, 200, 255),
            value = 30,
            effect = ItemEffect(ItemEffect.EffectType.RESTORE_ENERGY, 25)
        )

        fun createHackChip(): Item = Item(
            id = "hack_chip",
            name = "Hack Chip",
            description = "Bypass basic security.",
            type = ItemType.CHIP,
            symbol = "H",
            color = Color.rgb(0, 255, 200),
            value = 100
        )

        fun createDataChip(): Item = Item(
            id = "data_chip",
            name = "Data Chip",
            description = "Contains encrypted corporate data.",
            type = ItemType.KEY_ITEM,
            symbol = "D",
            color = Color.rgb(255, 200, 0),
            stackable = false,
            value = 500
        )

        fun createMilitaryStim(): Item = Item(
            id = "military_stim",
            name = "Military Stim",
            description = "Powerful combat stimulant. +50 energy, temporary stat boost.",
            type = ItemType.CONSUMABLE,
            symbol = "M",
            color = Color.rgb(200, 50, 50),
            value = 200,
            effect = ItemEffect(ItemEffect.EffectType.RESTORE_ENERGY, 50)
        )

        fun createStealthChip(): Item = Item(
            id = "stealth_chip",
            name = "Stealth Chip",
            description = "Makes you harder to detect.",
            type = ItemType.CYBERWARE,
            symbol = "?",
            color = Color.rgb(100, 100, 150),
            stackable = false,
            value = 300
        )

        fun createEMPGrenade(): Item = Item(
            id = "emp_grenade",
            name = "EMP Grenade",
            description = "Disables electronics in an area.",
            type = ItemType.WEAPON,
            symbol = "E",
            color = Color.rgb(100, 200, 255),
            value = 150
        )
    }
}

data class ItemEffect(
    val type: EffectType,
    val value: Int
) {
    enum class EffectType {
        HEAL,
        RESTORE_ENERGY,
        DAMAGE,
        BUFF_STRENGTH,
        BUFF_AGILITY
    }
}
