package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import com.github.miracle.SecretConfig.owner
import com.github.miracle.utils.data.WoPayData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendAddEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun Bot.reaction() {
    eventChannel.subscribeAlways<MemberJoinEvent> {
        group.sendMessage("欢迎")
        // TODO 自定义
    }

    eventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent>(priority = EventPriority.HIGH) {
        intercept()
        val date = LocalDate.parse(WoPayData.inquire(this.groupId), DateTimeFormatter.ISO_DATE).minusDays(1)
        if (date.isBefore(LocalDate.now())) {
            launch {
                logger.info("$groupName($groupId) 邀请入群, 第一次?")
                delay(TimeUnit.SECONDS.toMillis(25))
                accept()
                delay(TimeUnit.SECONDS.toMillis(5))
                getGroupOrFail(groupId).apply {
                    getFriendOrFail(SecretConfig.owner).sendMessage(
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
            getFriendOrFail(owner).sendMessage(
                "$groupName($groupId) 邀请入群, 已加入"
            )
        }
    }
    eventChannel.subscribeAlways<NewFriendRequestEvent> {
        accept()
        getFriendOrFail(owner).sendMessage(
            "$fromId 添加好友"
        )
    }
}