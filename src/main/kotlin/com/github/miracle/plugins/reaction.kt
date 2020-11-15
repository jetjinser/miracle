package com.github.miracle.plugins

import com.github.miracle.SecretConfig
import com.github.miracle.SecretConfig.owner
import com.github.miracle.utils.data.WoPayData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.getGroupOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun Bot.reaction() {
//    subscribeAlways<GroupEntranceAnnouncementChangeEvent> {
//        group.sendMessage("🔈 群公告已改变, 请及时查看\nchange by ${operatorOrBot.nameCardOrNick}")
//    }

    subscribeAlways<BotJoinGroupEvent> {
        group.sendMessage("大家好")
        val date = LocalDate.parse(WoPayData.inquire(group.id), DateTimeFormatter.ISO_DATE).minusDays(1)
        if (date.isBefore(LocalDate.now())) {
            launch {
                logger.info("${group.name}(${group.id}) 邀请入群, 第一次?")
                delay(TimeUnit.SECONDS.toMillis(15))
                getGroupOrNull(group.id)?.apply {
                    getFriendOrNull(SecretConfig.owner)?.sendMessage(
                        "${group.name}(${group.id}) 邀请入群, 已加入并注册"
                    )
                    sendMessage("bot已到期, 如有意愿续费请加群了解:\n117340135\nps: 五分钟后退群")
                    delay(TimeUnit.MINUTES.toMillis(5))
                    quit()
                }
            }
        } else {
            logger.info("加入 ${group.name}(${group.id})")
            getFriendOrNull(owner)?.sendMessage(
                "${group.name}(${group.id}) 邀请入群, 已加入"
            )
        }
    }

    subscribeAlways<MemberJoinEvent> {
        group.sendMessage("欢迎")
        // TODO 自定义
    }

//    subscribeAlways<BotLeaveEvent.Kick> {
//        getFriendOrNull(owner)?.sendMessage(
//            "已被 ${operator.nameCard}(${operator.id}) 踢出群 ${group.name}(${group.id})"
//        )
//    }

//    subscribeAlways<BotLeaveEvent.Active> {
//        getFriendOrNull(owner)?.sendMessage(
//            "已离开群 ${group.name}(${group.id})"
//        )
//    }

//    subscribeAlways<MemberLeaveEvent.Kick> {
//        group.sendMessage(
//            "🔈 ${member.nick}${if (member.nameCard.isEmpty()) "" else "(${member.nameCard})"}被踢出本群 " +
//                    "\nOperated by ${operator?.nameCardOrNick ?: nick}"
//        )
//    }

//    subscribeAlways<MemberLeaveEvent.Quit> {
//        group.botAsMember.sendMessage("🔈 ${member.nick}(${member.nameCard})退出本群")
//    } 感觉会很吵

//    subscribeAlways<MemberSpecialTitleChangeEvent> {
//        group.sendMessage("${member.nameCardOrNick}获得头衔：$new \nAwarded by ${operatorOrBot.nameCardOrNick}")
//    }
}