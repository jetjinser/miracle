package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

/**
 * 扫描文件夹，如果有新图片就发送到owner的qq号
 */
fun Bot.shotSender() {
    val dirPath = "I:\\Steam\\userdata\\300641665\\760\\remote\\1366540\\screenshots"
    val fileNameList = emptyList<String>().toMutableList()
    var isFirst = true
    Timer().apply {
        schedule(Date(), period = TimeUnit.MINUTES.toMillis(1)) {
            val fileTree: FileTreeWalk = File(dirPath).walk()
            if (isFirst) {
                fileTree.maxDepth(1) //需遍历的目录层次为1，即无须检查子目录
                    .filter { it.isFile } //只挑选文件，不处理文件夹
                    .filter { it.extension == "jpg" } //选择扩展名为txt的文本文件
                    .forEach {
                        fileNameList.add(it.name)
                    }
                isFirst = false
            } else {
                fileTree.maxDepth(1) //需遍历的目录层次为1，即无须检查子目录
                    .filter { it.isFile } //只挑选文件，不处理文件夹
                    .filter { it.extension == "jpg" } //选择扩展名为txt的文本文件
                    .forEach {
                        if (!fileNameList.contains(it.name)) {
                            launch {
                                it.toExternalResource("jpg")
                                    .sendAsImageTo(getFriendOrFail(SecretConfig.owner))
                            }
                            fileNameList.add(it.name)
                        }
                    }
            }
        }
    }
}