package com.github.miracle.plugins

import com.github.miracle.utils.data.CheckInData
import com.github.miracle.utils.data.TipsData
import com.github.miracle.utils.expand.subscribeOwnerMessage
import com.github.miracle.utils.tools.checkIn.CheckInModel
import com.github.miracle.utils.tools.checkIn.CheckInPicture
import com.github.miracle.utils.tools.checkIn.CheckInPicture.BackgroundImageType
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.minutesToMillis
import sun.font.FontDesignMetrics
import java.awt.Font


fun Bot.checkIn() {
    subscribeGroupMessages {
        case("签到", trim = true) {
            CheckInModel(this).also {
                if (it.checkIn()) {
                    val type = listOf(BackgroundImageType.LoyPoly, BackgroundImageType.Gaussian)
                    CheckInPicture(sender.avatarUrl, it).generate(type.random()).send()
                } else {
                    reply("您今天已经签到过了")
                }
            }
        }

        case("查询", trim = true) {
            val model = CheckInModel(this)
            val data = model.checkInData
            val symbol = if (model.haveCheckIn()) "已签到 √" else "未签到 ×"
            buildMessageChain {
                add(sender.nameCardOrNick)
                add("\nCuprum ${data.cuprum}    今日$symbol\n")
                add("签到天数  ${data.checkInDays}    好感度  ${data.favor}\n")
                add("via checkIn")
            }.send()
        }

        // ----

        startsWith("提交", removePrefix = true, trim = true) { s ->
            if (message[Image] != null) {
                reply("无法提交图片")
                return@startsWith
            }
            var tipWidth = 0
            val tipsFont = Font("Microsoft JhengHei", Font.BOLD, 20)
            s.forEach { word -> tipWidth += FontDesignMetrics.getMetrics(tipsFont).charWidth(word) }
            if (tipWidth > 540) {
                reply("tip 过长, 提交失败")
                return@startsWith
            }

            var tip = s
            if (tip.isEmpty()) {
                tip = nextMessage(3.minutesToMillis) { message.content.isNotEmpty() }.content
                if (tip in listOf("算了", "取消", "不要了")) {
                    reply("好的")
                    return@startsWith
                }
            }

            val data = CheckInData(this)
            data.consumeCuprum(20) {
                if (it.first) {
                    val success = TipsData.add(tip, sender.id)
                    if (success) reply("提交成功:\n$s") else {
                        reply("提交失败, tip已存在")
                        return@consumeCuprum false
                    }
                } else {
                    reply("铜币不足 20 , 提交取消, 铜币可由签到获得\n当前铜币: ${it.second}")
                }
                true
            }
        }

        case("历史提交", trim = true) {
            val history = TipsData.getHistory(sender.id)
            if (history != null) {
                buildMessageChain {
                    history.forEach {
                        val symbol = if (it.review == null) "*" else if (it.review) "√" else "×"
                        add("${it.id} - ${it.tip} | $symbol\n")
                    }
                    add("via checkInTips")
                }.send()
            } else reply("你还没有提交过tip")
        }

        case("正在审核", trim = true) {
            val reviewingList = TipsData.getReviewingList(sender.id)
            if (!reviewingList.isNullOrEmpty()) {
                buildMessageChain {
                    println(reviewingList)
                    reviewingList.forEach {
                        add("${it.id} - ${it.tip}\n")
                    }
                    add("via checkInTip")
                }.send()
            } else reply("现在没有正在审核的tip")
             
        }
    }

    subscribeOwnerMessage {
        case("审核", trim = true) {
            val pendingTip = TipsData.getPendingTips()
            if (pendingTip != null) {
                val id = pendingTip.id
                reply(
                    "> $id\ntip: [${pendingTip.tip}]\ndate: ${pendingTip.date}\nSubmitted by ${pendingTip.qqId}\nvia checkInTip"
                )
                val msg = nextMessage { true }
                if (msg.content == "好") {
                    TipsData.review(id, true)
                    reply("已通过")
                } else {
                    reply("已标记为不通过")
                    TipsData.review(id, false)
                }
            } else reply("暂时没有更多了")
        }
    }
}
