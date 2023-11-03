package kor.toxicity.questadder.font

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.stream.JsonWriter
import kor.toxicity.questadder.extension.parseToSpaceComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.round

class QuestFont(
    file: File,
    private val scale: Int,
    private val height: Int,
    private val ascent: Int,

    width: Double,
    widthMultiplier: Double,

    private val blank: Int,

    private val jsonDir: File,
    private val fontDir: File
) {
    companion object {
        private const val ESTIMATED_WIDTH = 16
        private val frc = FontRenderContext(null, true, true)
        private val gson = Gson()
    }

    //Font
    private val font = file.inputStream().buffered().use {
        Font.createFont(Font.TRUETYPE_FONT, it)
    }.deriveFont(scale.toFloat())

    private val availableChar = run {
        val list = (Char.MIN_VALUE..Char.MAX_VALUE).filter {
            font.canDisplay(it)
        }
        list.subList(0, list.size - list.size % ESTIMATED_WIDTH)
    }

    private val metrics = run {
        val image = BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        val metrics = graphics.getFontMetrics(font)
        graphics.dispose()
        metrics
    }
    private val fileName = file.nameWithoutExtension
    private val jsonName = "${fileName}_${ascent}_$height"
    private val key = Key.key("questadder:tooltip/$jsonName")

    private val charWidth = availableChar.associateWith {
        val bound = font.getStringBounds(it.toString(), frc)
        round((bound.width * height / bound.height / 1.4 + width) * widthMultiplier).toInt()
    }

    init {
        createBitmap()
        createJson()
    }

    private fun createBitmap() {
        val newDir = File(fontDir, fileName)
        if (!newDir.exists()) {
            newDir.mkdir()
            var i = 0
            var num = 0
            val yAxis = (metrics.height * 1.4F).toInt()
            while (i < availableChar.size) {
                val image = BufferedImage(scale * ESTIMATED_WIDTH, yAxis * ESTIMATED_WIDTH, BufferedImage.TYPE_INT_ARGB)
                val graphics = image.createGraphics().apply {
                    composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
                    font = this@QuestFont.font
                    setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                    setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
                }
                for ((y,i2) in (0 until ESTIMATED_WIDTH).withIndex()) {
                    val max = (i + (i2 + 1) * ESTIMATED_WIDTH)
                    if (max >= availableChar.size) break
                    availableChar.subList(i + i2 * ESTIMATED_WIDTH, max).forEachIndexed { x, c ->
                        val str = c.toString()
                        graphics.drawString(str, x * scale, ((y + 0.75) * yAxis).toInt())
                    }
                }
                graphics.dispose()
                ImageIO.write(image, "png", File(newDir, "${fileName}_${++num}.png"))

                i += ESTIMATED_WIDTH * ESTIMATED_WIDTH
            }
        }
    }
    private fun createJson() {
        val json = File(jsonDir, "$jsonName.json")
        if (!json.exists()) {
            val array = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("type","space")
                    add("advances",JsonObject().apply {
                        addProperty(" ", 4)
                    })
                })
            }
            var i = 0
            var num = 0
            while (i < availableChar.size) {
                array.add(JsonObject().apply {
                    addProperty("type", "bitmap")
                    addProperty("file", "questadder:font/tooltip/$fileName/${fileName}_${++num}.png")
                    addProperty("ascent", ascent)
                    addProperty("height", height)
                    add("chars", JsonArray().apply {
                        for (i2 in 0 until ESTIMATED_WIDTH) {
                            val max = (i + (i2 + 1) * ESTIMATED_WIDTH)
                            if (max >= availableChar.size) break
                            add(availableChar.subList(i + i2 * ESTIMATED_WIDTH, max).joinToString(""))
                        }
                    })
                })
                i += ESTIMATED_WIDTH * ESTIMATED_WIDTH
            }
            JsonWriter(json.bufferedWriter()).use {
                gson.toJson(JsonObject().apply {
                    add("providers", array)
                }, it)
            }
        }
    }

    fun asComponent(component: Component): Component {
        component as TextComponent
        var comp = Component.empty()
        val style = component.style()
        component.content().forEach {
            comp = comp.append(asComponent(it, style))
        }
        component.children().forEach {
            comp = comp.append(asComponent(it))
        }
        return comp
    }
    fun asComponent(char: Char, style: Style): Component {
        var width = if (char == ' ') 4 else charWidth[char] ?: 0
        if (style.hasDecoration(TextDecoration.BOLD)) width++
        if (style.hasDecoration(TextDecoration.ITALIC)) width++
        return Component.text(char).style(style.font(key)).append((-width).parseToSpaceComponent()).append(blank.parseToSpaceComponent())
    }
    fun widthComponent(int: Int) = (blank * int).parseToSpaceComponent()
    fun widthComponent(component: Component): Int {
        component as TextComponent
        var width = component.content().length * blank
        component.children().forEach {
            width += widthComponent(it)
        }
        return width
    }
}
