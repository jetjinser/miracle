package com.github.miracle.plugins

import com.github.miracle.utils.data.LotteryData
import com.github.miracle.utils.tools.RemindDate
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

/**
 * 抽奖机器人
 * 基本逻辑：
 * - 发起抽奖，设置截止时间
 * - 全群抽奖
 * - 参与抽奖
 *   - 截止时间前艾特机器人参与抽奖
 *   - 到时间时在参与人中随机抽取
 */
fun Bot.lottery() {
    var lotteryList = LotteryData.getAllLotteryList()
    eventChannel.subscribeGroupMessages {
        Regex("""(?i).*\s抽奖.*""") matching regex@{
            if (message.firstIsInstanceOrNull<At>()?.target != bot.id) {
                return@regex
            }
            // at bot 并带有关键词抽奖
            // 解析奖品 x个something
            val msg = message.first { it is PlainText }.content
            val prizeResult = Regex(""".*\s抽奖\s(?<count>.*)个(?<something>.*)""").matchEntire(msg)
            val count = prizeResult?.groups?.get(1)?.value?.trim()
            val prize = prizeResult?.groups?.get(2)?.value?.trim()
            if (count.isNullOrEmpty() || prize.isNullOrEmpty()) {
                subject.sendMessage("at机器人并发送[抽奖 x个奖品名]设置抽奖")
                return@regex
            }
            subject.sendMessage("抽$count 个 $prize, 什么时候开奖?使用[X分钟/小时/天后]设置")
            intercept()
            val timeMsg = nextMessage(
                timeoutMillis = TimeUnit.MINUTES.toMillis(2),
                priority = EventPriority.HIGH
            ) {
                message[MessageSource.Key] != null
            }.content
            val timeString = Regex("""(?<date>.*后)\s*""").matchEntire(timeMsg)
            if (timeString == null) {
                subject.sendMessage("格式错误")
                return@regex
            }
            val preDate = timeString.groups[1]?.value?.trim()
            val date = preDate?.dropLast(1) ?: return@regex
            val deadline = RemindDate.getDate(date)
            if (deadline == null) {
                subject.sendMessage("单位不支持或时间为0, 请注意, 过大的数字会导致溢出, 使时间错误")
                return@regex
            }
            subject.sendMessage("$deadline 开奖, 请设置抽奖类型，回复0全群随机抽奖，回复其他数字限制参与人数")
            intercept()
            val limitMsg = nextMessage(
                timeoutMillis = TimeUnit.MINUTES.toMillis(2),
                priority = EventPriority.HIGH
            ) {
                message[MessageSource.Key] != null
            }.content
            val limit = limitMsg.toInt()

            val result = LotteryData.addNewLottery(
                group.id,
                sender.id,
                prize,
                count.toInt(),
                deadline.time,
                limit
            )
            if (result >= 0) {
                subject.sendMessage(
                    "抽奖设置完成！id为$result\n" +
                            "$deadline 开奖抽 ${count}个$prize\n" +
                            "限制最大参与人数：${
                                if (limit == 0) "全群自动参与"
                                else "$limit 人，请群员艾特机器人并发送抽奖id参与抽奖"
                            }"
                )
                lotteryList = LotteryData.getAllLotteryList()
            }

        }
        Regex("""(?i).*参加抽奖.*""") matching regex@{
            if (message.firstIsInstanceOrNull<At>()?.target != bot.id) {
                return@regex
            }
            val msg = message.first { it is PlainText }.content
            val joinResult = Regex(""".*参加抽奖\s(?<id>.*)""").matchEntire(msg)
            val lotId = joinResult?.groups?.get(1)?.value?.trim()
            if (lotId.isNullOrEmpty() || lotId.toIntOrNull() == null) {
                subject.sendMessage("请at机器人并发送抽奖id参与抽奖")
                return@regex
            }
            if (LotteryData.isLotteryAble(lotId.toInt(), group.id)) {
                // 判断是不是本群的和参与人数是否已满
                subject.sendMessage("该抽奖参与人数已满")
            } else {
                // 参与抽奖
                val result = LotteryData.joinLottery(sender.id, lotId)
                if (result) {
                    subject.sendMessage("${sender.nameCard}已参与${lotId}号抽奖")
                } else {
                    subject.sendMessage("请at机器人并发送抽奖id参与抽奖")
                }
            }
        }
        case("抽奖列表") {
            val lotList = LotteryData.getLotteryList(group.id)
            if (lotList.isEmpty()) {
                subject.sendMessage("本群还没有抽奖")
            } else {
                subject.sendMessage(
                    lotList.joinToString("\n") {
                        "${it.element1} - ${it.element3}，最多${
                            if (it.element2 == 0) "全群"
                            else it.element2
                        }人参与，${Date(it.element4)}"
                    }
                )
            }
        }
    }
    Timer().schedule(Date(), period = TimeUnit.MINUTES.toMillis(1)) {
        launch {
            lotteryList.forEach {
                if (it.element4 <= Date().time) { // ddl < now
                    // 开奖
                    val winMemberIdList = mutableListOf<Long>()
                    if (it.element3 == 0) {
                        // 在全群中抽取
                        val groupAllMembers = getGroupOrFail(it.element2).members
                        val maxCount =
                            if (groupAllMembers.size < it.element5) {
                                groupAllMembers.size
                            } else {
                                it.element5
                            }
                        while (winMemberIdList.size < maxCount) {
                            val winMember = groupAllMembers.random()
                            if (!winMemberIdList.contains(winMember.id)) {
                                winMemberIdList.add(winMember.id)
                            }
                        }
                    } else {
                        // 获取所有参与者的qq抽取
                        val joinIds = LotteryData.getJoinId(it.element1)
                        val maxCount =
                            if (joinIds.size < it.element5) {
                                joinIds.size
                            } else {
                                it.element5
                            }
                        while (winMemberIdList.size < maxCount) {
                            val winMemberId = joinIds.random()
                            if (!winMemberIdList.contains(winMemberId)) {
                                winMemberIdList.add(winMemberId)
                            }
                        }
                    }

                    buildMessageChain {
                        add("${LotteryData.getLotteryInfoById(it.element1)}开奖，获奖者为：\n")
                        winMemberIdList.forEach { id ->
                            add(At(id))
                        }
                    }.sendTo(getGroupOrFail(it.element2))
                    LotteryData.deleteLottery(it.element1)
                    lotteryList = LotteryData.getAllLotteryList()
                }
            }
        }
    }
}