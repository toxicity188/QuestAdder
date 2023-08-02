package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import kor.toxicity.questadder.command.CommandAPI
import kor.toxicity.questadder.command.SenderType
import kor.toxicity.questadder.event.PlayerParseEvent
import kor.toxicity.questadder.extension.asComponent
import kor.toxicity.questadder.extension.info
import kor.toxicity.questadder.extension.send
import kor.toxicity.questadder.mechanic.Dialog
import kor.toxicity.questadder.mechanic.Quest
import kor.toxicity.questadder.util.ComponentReader
import kor.toxicity.questadder.util.GuiExecutor
import kor.toxicity.questadder.util.GuiWrapper
import kor.toxicity.questadder.util.MouseButton
import kor.toxicity.questadder.util.action.AbstractAction
import kor.toxicity.questadder.util.action.CancellableAction
import kor.toxicity.questadder.util.action.RegistrableAction
import kor.toxicity.questadder.util.builder.ActionBuilder
import kor.toxicity.questadder.util.builder.FunctionBuilder
import kor.toxicity.questadder.util.event.AbstractEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object DialogManager: QuestAdderManager {

    private val dialogMap = HashMap<String,Dialog>()
    private val actionMap = HashMap<String,RegistrableAction>()
    private val questMap = HashMap<String,Quest>()

    override fun start(adder: QuestAdder) {
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun quit(e: PlayerQuitEvent) {
                val player = e.player
                Dialog.stop(player)
                actionMap.values.forEach {
                    it.cancel(player)
                }
            }
            @EventHandler
            fun death(e: PlayerDeathEvent) {
                Dialog.stop(e.player)
            }
            @EventHandler
            fun click(e: InventoryClickEvent) {
                val player = e.whoClicked
                if (player is Player && Dialog.isRunning(player)) e.isCancelled = true
            }
        },adder)
        adder.command.addCommand("parse") {
            aliases = arrayOf("p")
            description = "parse result from given arguments."
            length = 1
            usage = "parse <text>"
            allowedSender = arrayOf(SenderType.PLAYER)
            executor = { sender, args ->
                val str = args.toMutableList().apply {
                    removeAt(0)
                }.joinToString(" ")
                ComponentReader<PlayerParseEvent>(str).createComponent(PlayerParseEvent(sender as Player).apply {
                    callEvent()
                })?.let { component ->
                    sender.info(component)
                } ?: sender.info("cannot parse this text argument.")
            }
        }
        adder.command.addCommandAPI("var", arrayOf("v","변수"),"variable-related command.", true, CommandAPI("qa v")
            .addCommand("set") {
                aliases = arrayOf("s","설정")
                description = "set the variable."
                usage = "set <player> <name> <value>"
                length = 3
                executor = { sender, args ->
                    Bukkit.getPlayer(args[1])?.let { player ->
                        FunctionBuilder.evaluate(args.toMutableList().apply {
                            removeAt(0)
                            removeAt(0)
                            removeAt(0)
                        }.joinToString(" ")).apply(PlayerParseEvent(player).apply {
                            callEvent()
                        })?.let {
                            QuestAdder.getPlayerData(player)?.set(args[2],it)
                            sender.send("the variable sets: ${args[2]} to $it")
                        } ?: sender.send("set failure!")
                    }
                }
            })
        adder.getCommand("quest")?.setExecutor { sender, _, _, _ ->
            if (sender !is Player) {
                sender.send("player only command.")
            } else {
                val data = QuestAdder.getPlayerData(sender)
                if (data != null) GuiWrapper("quest".asComponent(),3).open(sender,object : GuiExecutor {
                    override fun initialize(inventory: Inventory) {
                        for (i in 9..17) {
                            inventory.setItem(i,null)
                        }
                        var i = 9
                        for (s in data.getQuestKey()) {
                            if (i > 17) break
                            questMap[s]?.let { quest ->
                                inventory.setItem(i++,quest.getIcon(sender))
                            }
                        }
                        sender.updateInventory()
                    }

                    override fun onEnd(inventory: Inventory) {
                    }

                    override fun onClick(
                        inventory: Inventory,
                        isPlayerInventory: Boolean,
                        clickedItem: ItemStack,
                        clickedSlot: Int,
                        action: MouseButton
                    ) {

                    }
                })
            }
            true
        }
    }
    override fun reload(adder: QuestAdder) {
        AbstractEvent.unregisterAll()
        actionMap.values.forEach {
            it.unregister()
        }

        actionMap.clear()
        adder.loadFolder("actions") { file, config ->
            config.getKeys(false).forEach {
                config.getConfigurationSection(it)?.let { c ->
                    ActionBuilder.build(adder,c)?.let { build ->
                        actionMap[it] = build
                    } ?: QuestAdder.warn("unable to load action. ($it in ${file.name})")
                } ?: QuestAdder.warn("syntax error: the key '$it' is not a configuration section. (${file.name})")
            }
        }
        Bukkit.getConsoleSender().send("${actionMap.size} of actions has successfully loaded.")

        dialogMap.clear()
        adder.loadFolder("dialogs") { file, config ->
            config.getKeys(false).forEach {
                config.getConfigurationSection(it)?.let { c ->
                    dialogMap[it] = Dialog(adder,file,it,c)
                } ?: QuestAdder.warn("syntax error: the key '$it' is not a configuration section. (${file.name})")
            }
        }
        Bukkit.getConsoleSender().send("${dialogMap.size} of dialogs has successfully loaded.")

        questMap.clear()
        adder.loadFolder("quests") { file, config ->
            config.getKeys(false).forEach {
                config.getConfigurationSection(it)?.let { c ->
                    try {
                        questMap[it] = Quest(adder, file, it, c)
                    } catch (ex: Exception) {
                        QuestAdder.warn("unable to load the quest. (${file.name})")
                        QuestAdder.warn("reason: ${ex.message ?: ex.javaClass.simpleName}")
                    }
                } ?: QuestAdder.warn("syntax error: the key '$it' is not a configuration section. (${file.name})")
            }
        }
        Bukkit.getConsoleSender().send("${questMap.size} of quests has successfully loaded.")
    }

    fun getDialog(name: String) = dialogMap[name]
    fun getAction(name: String) = actionMap[name]
    fun getQuest(name: String) = questMap[name]


    override fun end(adder: QuestAdder) {
    }
}