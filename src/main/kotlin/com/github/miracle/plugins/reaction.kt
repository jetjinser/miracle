package com.github.miracle.plugins

import com.github.miracle.SecretConfig.owner
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.getFriendOrNull

fun Bot.reaction() {
    subscribeAlways<GroupEntranceAnnouncementChangeEvent> {
        group.botAsMember.sendMessage("🔈 群公告已改变, 请及时查看\nchange by ${operatorOrBot.nameCardOrNick}")
    }

    subscribeAlways<GroupAllowAnonymousChatEvent> {
        val opt = if (group.settings.isAnonymousChatEnabled) {
            "🔈 匿名被开启了"
        } else {
            "🔈 匿名被禁止了"
        }
        group.sendMessage(opt)
    }

    subscribeAlways<GroupAllowConfessTalkEvent> {
        val opt = if (group.settings.isConfessTalkEnabled) {
            "🔈 坦白说被禁止了"
        } else {
            "🔈 坦白说被开启了"
        }
        group.sendMessage(opt)
    }

    subscribeAlways<GroupAllowMemberInviteEvent> {
        val opt = if (group.settings.isAllowMemberInvite) {
            "🔈 现在允许群友邀请好友入群了"
        } else {
            "🔈 现在群友不能邀请好友入群了"
        }
        group.sendMessage(opt)
    }

    subscribeAlways<MemberJoinEvent.Invite> {
        group.sendMessage("欢迎")
    }
    subscribeAlways<BotJoinGroupEvent> {
        group.sendMessage("大家好")
    }

    subscribeAlways<MemberJoinEvent.Active> {
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

    subscribeAlways<MemberLeaveEvent.Kick> {
        group.sendMessage(
            "🔈 ${member.nick}${if (member.nameCard.isEmpty()) "" else "(${member.nameCard})"}被踢出本群 " +
                    "\nOperated by ${operator?.nameCardOrNick ?: nick}"
        )
    }

//    subscribeAlways<MemberLeaveEvent.Quit> {
//        group.botAsMember.sendMessage("🔈 ${member.nick}(${member.nameCard})退出本群")
//    } 感觉会很吵

    subscribeAlways<BotInvitedJoinGroupRequestEvent> {
        ignore()
        getFriendOrNull(owner)?.sendMessage(
            "$groupName($groupId) 邀请入群, 已忽略"
        )
    }

    subscribeAlways<MemberSpecialTitleChangeEvent> {
        group.sendMessage("${member.nameCardOrNick}获得头衔：$new \nAwarded by ${operatorOrBot.nameCardOrNick}")
    }
}