package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import com.github.miracle.SecretConfig.owner
import com.github.miracle.utils.data.WoPayData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.getGroupOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun Bot.reaction() {
    subscribeAlways<MemberJoinEvent> {
        group.sendMessage("欢迎")
        // TODO 自定义
    }

    subscribeAlways<BotLeaveEvent.Kick> {
        getFriendOrNull(owner)?.sendMessage(
            "已被 ${operator.nameCard}(${operator.id}) 踢出群 ${group.name}(${group.id})"
        )
    }

    subscribeAlways<BotLeaveEvent.Active> {
        getFriendOrNull(owner)?.sendMessage(
            "已离开群 ${group.name}(${group.id})"
        )
    }

    subscribeAlways<BotJoinGroupEvent> {
        group.sendMessage("大家好")
    }

    subscribeAlways<BotInvitedJoinGroupRequestEvent>(priority = Listener.EventPriority.HIGH) {
        intercept()
        val date = LocalDate.parse(WoPayData.inquire(this.groupId), DateTimeFormatter.ISO_DATE).minusDays(1)
        if (date.isBefore(LocalDate.now())) {
            launch {
                logger.info("$groupName($groupId) 邀请入群, 第一次?")
                delay(TimeUnit.SECONDS.toMillis(25))
                accept()
                delay(TimeUnit.SECONDS.toMillis(5))
                getGroupOrNull(groupId)?.apply {
                    getFriendOrNull(SecretConfig.owner)?.sendMessage(
                        "$groupName($groupId) 邀请入群, 已加入并注册"
                    )
                    sendMessage("bot已到期, 如有意愿续费请加群了解:\n117340135\nps: 五分钟后退群")
                    delay(TimeUnit.MINUTES.toMillis(5))
                    quit()
                }
            }
        } else {
            logger.info("加入 $groupName($groupId)")
            accept()
            getFriendOrNull(owner)?.sendMessage(
                "$groupName($groupId) 邀请入群, 已加入"
            )
        }
    }
}