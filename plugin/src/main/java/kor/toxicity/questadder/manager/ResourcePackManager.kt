package kor.toxicity.questadder.manager

import kor.toxicity.questadder.QuestAdder
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ResourcePackManager: QuestAdderManager {
    override fun start(adder: QuestAdder) {
    }

    override fun reload(adder: QuestAdder) {
        val resource = File(adder.dataFolder,"resources").apply {
            mkdir()
        }
        val build = File(resource,"build").apply {
            deleteRecursively()
            mkdir()
        }
        adder.getResource("pack.zip")?.use {
            ZipUtil.unpack(it,build)
        }
    }

    override fun end(adder: QuestAdder) {
    }
}