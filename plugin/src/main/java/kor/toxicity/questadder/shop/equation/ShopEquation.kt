package kor.toxicity.questadder.shop.equation

import kor.toxicity.questadder.shop.implement.ShopItem
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import org.bukkit.entity.Player

class ShopEquation(string: String) {

    companion object {
        val defaultEquation = ShopEquation("t")
    }

    private val expression = ExpressionBuilder(string)
        .variables(
            "t",
            "b",
            "s",
            "r",
            "k"
        )
        .build()
    fun buyEvaluate(item: ShopItem, player: Player): Double {
        return Expression(expression)
            .setVariables(mapOf(
                "t" to item.blueprint.buyPrice.price.toDouble(),
                "b" to item.totalBuy.toDouble(),
                "s" to item.totalSell.toDouble(),
                "r" to item.buyRandomNumber,
                "k" to item.getStock(player).toDouble()
            ))
            .evaluate()
    }
    fun sellEvaluate(item: ShopItem, player: Player): Double {
        return Expression(expression)
            .setVariables(mapOf(
                "t" to item.blueprint.sellPrice.price.toDouble(),
                "b" to item.totalBuy.toDouble(),
                "s" to item.totalSell.toDouble(),
                "r" to item.sellRandomNumber,
                "k" to item.getStock(player).toDouble()
            ))
            .evaluate()
    }
}
