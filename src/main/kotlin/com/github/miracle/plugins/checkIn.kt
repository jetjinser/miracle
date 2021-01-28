package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import com.github.miracle.utils.data.CheckInData
import com.github.miracle.utils.data.TipsData
import com.github.miracle.utils.tools.checkIn.CheckInModel
import com.github.miracle.utils.tools.checkIn.CheckInPicture
import com.github.miracle.utils.tools.checkIn.CheckInPicture.BackgroundImageType
import com.github.miracle.utils.tools.statistics.UserStatHandle
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.subscribeFriendMessages
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import sun.font.FontDesignMetrics
import java.awt.Font
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


fun Bot.checkIn() {
    eventChannel.subscribeGroupMessages {
        case("签到", trim = true) {
            CheckInModel(this).also {
                if (it.checkIn()) {
                    val type = listOf(BackgroundImageType.LoyPoly, BackgroundImageType.Gaussian)
                    val imgBuffer = CheckInPicture(sender.avatarUrl, it).generate(type.random())
                    buildMessageChain {
                        add(imgBuffer.toExternalResource().uploadAsImage(subject))
                    }.sendTo(subject)
                } else {
                    subject.sendMessage("您今天已经签到过了")
                }
            }
        }

        case("查询", trim = true) {
            val model = CheckInModel(this)
            val data = model.checkInData
            val symbol = if (model.haveCheckIn()) "已签到 √" else "未签到 ×"
            subject.sendMessage(buildMessageChain {
                add(sender.nameCardOrNick)
                add("\nCuprum ${data.cuprum}    今日$symbol\n")
                add("签到天数  ${data.checkInDays}    好感度  ${data.favor}\n")
                add("via checkIn")
            })
        }

        // ----


        startsWith("提交 ", removePrefix = true) { s ->
            if (message[Image] != null) {
                subject.sendMessage("无法提交图片")
                return@startsWith
            }
            var tipWidth = 0
            val tipsFont = Font("Microsoft JhengHei", Font.BOLD, 20)
            s.forEach { word -> tipWidth += FontDesignMetrics.getMetrics(tipsFont).charWidth(word) }
            if (tipWidth > 540) {
                subject.sendMessage("tip 过长, 提交失败")
                return@startsWith
            }

            var tip = s.trim()
            if (tip.isEmpty()) {
                tip = nextMessage(3000) { message.content.isNotEmpty() }.content.trim()
                if (tip in listOf("算了", "取消", "不要了")) {
                    subject.sendMessage("好的")
                    return@startsWith
                }
            }

            val data = CheckInData(this)
            data.consumeCuprum(20) {
                if (it.first) {
                    val success = TipsData.add(tip, sender.id)
                    if (success) subject.sendMessage("提交成功:\n$s") else {
                        subject.sendMessage("提交失败, tip已存在")
                        return@consumeCuprum false
                    }
                } else {
                    subject.sendMessage("铜币不足 20 , 提交取消, 铜币可由签到获得\n当前铜币: ${it.second}")
                }
                true
            }
        }

        startsWith("历史提交", removePrefix = true, trim = true) { s ->
            val page = s.toIntOrNull() ?: 1
            val history = TipsData.getHistory(sender.id)
            if (history != null) {
                subject.sendMessage(buildMessageChain {
                    history.drop((page - 1) * 10).take(page * 10).forEach {
                        val symbol = if (it.review == null) "*" else if (it.review) "√" else "×"
                        add("${it.id} - ${it.tip} | $symbol\n")
                    }
                    add("Page $page/${if (history.size % 10 == 0) history.size / 10 else (history.size / 10) + 1}\n")
                    add("可发送格式: `历史提交 [page]` 来选择页码\n")
                    add("via checkInTips")
                })
            } else subject.sendMessage("你还没有提交过tip")
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
                }.sendTo(subject)
            } else subject.sendMessage("现在没有正在审核的tip")

        }
    }

    eventChannel.subscribeFriendMessages {
        sentBy(SecretConfig.owner) {
            case("审核", trim = true) {
                val pendingTip = TipsData.getPendingTips()
                if (pendingTip != null) {
                    val id = pendingTip.id
                    subject.sendMessage(
                        "> $id\ntip: [${pendingTip.tip}]\ndate: ${pendingTip.date}\nSubmitted by ${pendingTip.qqId}\nvia checkInTip"
                    )
                    val msg = nextMessage { true }
                    if (msg.content == "好") {
                        TipsData.review(id, true)
                        subject.sendMessage("已通过")
                    } else {
                        subject.sendMessage("已标记为不通过")
                        TipsData.review(id, false)
                    }
                } else subject.sendMessage("暂时没有更多了")
            }

            case("stat", ignoreCase = true, trim = true) {
                val stat = UserStatHandle.stat
                if (stat == null) {
                    subject.sendMessage("Failure")
                    return@case
                }

                subject.sendMessage(
                    "用户: ${stat.count}人\n最高签到天数: ${stat.mostDays}\n最多持有铜币: ${stat.mostCuprum}\n最大拥有好感: ${stat.mostFavor}"
                )
            }
        }
    }
}
