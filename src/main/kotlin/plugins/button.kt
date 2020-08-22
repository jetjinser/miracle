package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.subscribeGroupMessages
import java.io.File

fun Bot.button() {
    fun traverseFileTree(filename: String): List<File> {
        val f = File(filename)
        val fileTreeWalk = f.walk()
        return fileTreeWalk.toList()
    }

    val aquaFileList = traverseFileTree("C:/Users/cmdrj/Desktop/archived/miracle/src/main/resources/button/aqua_voices")
    val meaFileList = traverseFileTree("C:/Users/cmdrj/Desktop/archived/miracle/src/main/resources/button/mea_voices")

    subscribeGroupMessages {
        Regex("""\s*(?:夸(?:叫|按钮))|(?:aqua +button)\s*""") matching  {
            val inputStream = aquaFileList.random().inputStream()
            inputStream.use { inp ->
                group.uploadVoice(inp).send()
            }
        }
        Regex("""\s*(?:咩(?:叫|按钮))|(?:mea +button)\s*""") matching {
            val inputStream = meaFileList.random().inputStream()
            inputStream.use { inp ->
                group.uploadVoice(inp).send()
            }
        }
    }
}