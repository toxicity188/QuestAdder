package kor.toxicity.questadder.shop.blueprint

import kor.toxicity.questadder.QuestAdderBukkit
import kor.toxicity.questadder.extension.*
import kor.toxicity.questadder.manager.DialogManager
import kor.toxicity.questadder.manager.ItemManager
import kor.toxicity.questadder.mechanic.dialog.Dialog
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

data class ShopItemBlueprint(
    val stock: Long,
    val global: Boolean,
    val builder: () -> ItemStack,

    val buyPrice: ShopPrice,
    val sellPrice: ShopPrice
) {
    companion object {
        private val emptyPrice = ShopPrice(-1, emptyList())
        private fun buildItem(section: ConfigurationSection) = (section.findString("Item","item") ?: throw RuntimeException("item value not found!")).let {
            ItemManager.getItemSupplier(it)?.let { supplier ->
                val i = supplier.get()
                val a = section.findInt(1,"Amount","amount").coerceAtLeast(1).coerceAtMost(i.maxStackSize)
                if (section.findBoolean("AlwaysRebuild","always-rebuild")) {
                    {
                        supplier.get().apply {
                            amount = a
                        }
                    }
                } else {
                    {
                        i.clone().apply {
                            amount = a
                        }
                    }
                }
            } ?: throw RuntimeException("unable to get this item: $it")
        }
    }
    constructor(adder: QuestAdderBukkit, section: ConfigurationSection): this(
        section.findLong(-1,"stock"),
        section.findBoolean("Global","global"),
        buildItem(section),
        section.findConfig("Buy","buy")?.let {
            ShopPrice(adder, it)
        } ?: emptyPrice,
        section.findConfig("Sell","sell")?.let {
            ShopPrice(adder, it)
        } ?: emptyPrice
    )

    data class ShopPrice(
        val price: Int,
        val item: List<ShopItemPrice>,
    ) {
        var dialog: Dialog? = null
            private set
        constructor(adder: QuestAdderBukkit, section: ConfigurationSection): this(
            section.findInt(-1,"Price","price"),
            section.findConfig("Items","item","Item","items")?.let {
                it.getKeys(false).mapNotNull { s ->
                    it.getConfigurationSection(s)?.let { config ->
                        ShopItemPrice(config)
                    }
                }
            } ?: emptyList()
        ) {
            section.findString("Dialog","dialog")?.let {
                adder.addLazyTask {
                    dialog = DialogManager.getDialog(it)
                }
            }
        }
    }
    data class ShopItemPrice(
        val item: () -> ItemStack,
        val chance: Double
    ) {
        constructor(section: ConfigurationSection): this(
            buildItem(section),
            section.findDouble(100.0,"Chance","chance").coerceAtLeast(0.0).coerceAtMost(100.0)
        )
    }
}
