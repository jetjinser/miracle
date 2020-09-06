package com.github.miracle.utils.tools.help

object Plugin {
    enum class EPlugin(val pluginRegex: Regex) {
        AntiLightApp(Regex("""(?i)([反抗对]小程序)|AntiLightApp""")),
        Bili(Regex("""(?i)(有?关于)?(bili|哔哩)+([相有]关)?""")),
        BuiltInReply(Regex("""(?i)(内置(回复|[响回]应))|BuiltInReply""")),
        Button(Regex("""(?i)(((mea|aqua)|[咩夸])+([语声]音)|按钮)|Button""")),
        CheckIn(Regex("""(?i)((有?关于)?签到([相有]关)?)|CheckIn""")),
        Help(Regex("""(?i)((有?关于)?帮助([相有]关)?)|Help""")),
        Information(Regex("""(?i)(有?关于)?[信消]息获[取得]([相有]关)?""")),
        Music(Regex("""(?i)((有?关于)?(音乐|点歌)([相有]关)?)|Music""")),
        Random(Regex("""(?i)((有?关于)?随机([相有]关)?)|Random""")),
        Reaction(Regex("""(?i)(被动[反响回][应答])|Reaction""")),

        //    Recode("消息记录")
        Remind(Regex("""(?i)(定[时点]提[醒示])|Remind""")),
        Scheduler(Regex("""(?i)((内置|默认)(定时器?|闹[钟铃]|提[醒示]))|Scheduler""")),
        Setting(Regex("""(?i)((有?关于)?设[置定]([相有]关)?)|Setting""")),
        Tuling(Regex("""(?i)(图灵(回[复答]|[问答]+|对话|交流))|Tuling""")),
        Thesaurus(Regex("""(?i)((有?关于)?(词库|问答)([相有]关)?)|Thesaurus""")),
        WoPay(Regex("""(?i)(赞助支持|租借(系统)?)|WoPay"""))

    }

    fun findPlugin(pluginName: String): EPlugin? {
        for (p in EPlugin.values()) {
            if (p.pluginRegex.matchEntire(pluginName) != null) return p
        }
        return null
    }
}