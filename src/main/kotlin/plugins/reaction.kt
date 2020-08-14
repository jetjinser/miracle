package plugins

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribeAlways

fun Bot.reaction() {
    subscribeAlways<GroupEntranceAnnouncementChangeEvent> {
        group.botAsMember.sendMessage("🔈 群公告已改变, 请及时查看 change by ${operator?.nick ?: nick}")
    }

    subscribeAlways<GroupAllowAnonymousChatEvent> {
        val opt = if (group.settings.isAnonymousChatEnabled) {
            "🔈 匿名被禁止了"
        } else {
            "🔈 匿名被开启了"
        }
        group.botAsMember.sendMessage(opt)
    }

    subscribeAlways<GroupAllowConfessTalkEvent> {
        val opt = if (group.settings.isConfessTalkEnabled) {
            "🔈 坦白说被禁止了"
        } else {
            "🔈 坦白说被开启了"
        }
        group.botAsMember.sendMessage(opt)
    }

    subscribeAlways<GroupAllowMemberInviteEvent> {
        val opt = if (group.settings.isAllowMemberInvite) {
            "🔈 现在允许群友邀请好友入群了"
        } else {
            "🔈 现在群友不能邀请好友入群了"
        }
        group.botAsMember.sendMessage(opt)
    }

    subscribeAlways<MemberJoinEvent.Invite> {
        group.botAsMember.sendMessage("大家好")
    }
    subscribeAlways<BotJoinGroupEvent.Invite> {
        group.botAsMember.sendMessage("大家好")
    }

    subscribeAlways<MemberJoinEvent.Active> {
        group.botAsMember.sendMessage("欢迎")
        // TODO 自定义
    }

    subscribeAlways<MemberLeaveEvent.Kick> {
        group.botAsMember.sendMessage("🔈 ${member.nick}(${member.nameCard})被踢出本群 Operated by ${operator?.nick ?: nick}")
    }

//    subscribeAlways<MemberLeaveEvent.Quit> {
//        group.botAsMember.sendMessage("🔈 ${member.nick}(${member.nameCard})退出本群")
//    } 感觉会很吵

    subscribeAlways<BotInvitedJoinGroupRequestEvent> {
        ignore()
    }
}