package com.github.miracle.utils.tools.help

class Helper(pluginName: String) {
    private val ePlugin = Plugin.findPlugin(pluginName)

    companion object {
        val antiLightAppDesc = """[AntiLightApp]
            |自动识别群聊消息中的小程序, 解析小程序并以一般的图文消息发送
        """.trimMargin()
        val bili = """[Bili]
            |自动识别群聊消息中的av/BV/cv号, 并返回相应的详细信息
            |可用指令:
            |  - 提取封面 <av 或 BV>
            |       用于提取b站视频的封面
        """.trimMargin()
        val builtInReply = """[BuiltInReply]
            |一些内置回复
            |可用指令:
            |  - 嘤一个
            |  - 喵一个
            |  - wei,zaima
            |  - 你好
            |  - 草
            |  - 机屑人
        """.trimMargin()
        val button = """[Button]
            |发送 mea 和 aqua 的语音
            |可用指令:
            |  - 咩叫
            |       返回 mea 的语音 
            |  - 夸叫
            |       返回 aqua 的语音
        """.trimMargin()
        val checkIn = """[CheckIn]
            |每日签到, (目前)是铜币的唯一获取来源
            |与 tips 相关, tips 即签到图下的小字, 全局可见
            |可用指令:
            |  - 签到
            |  - 查询
            |       查询个人信息
            |  - 提交
            |       提交tips, 消耗 20 铜币
            |  - 历史提交
            |       查看所有提交过的tips, 
            |       ps: [√][×][*] 分别表示 [审核通过][审核不通过][正在审核]
            |  - 正在审核
            |       查看正在审核中的tips
        """.trimMargin()
        val help = """[Help]
            |帮助消息
            |可用指令:
            |  - 帮助 <插件名>
            |       当不提供插件名时提供 about, 插件名可模糊识别
        """.trimMargin()
        val information = """[Information]
            |互联网开放api的一些信息
            |可用指令:
            |  - 一言
            |       获取一言/二次元语录/five语录, 即一句话
            |  - 找点乐子
            |       提供一条现在可以做什么的建议
        """.trimMargin()
        val music = """[Music]
            |点歌, 目前支持: 网易云, 酷狗
            |可用指令:
            |  - [网易云/酷狗]点歌 <歌名>
            |       消费 50 铜币 @see CheckIn
            |       [网易云/酷狗] 可选, 若不指定则默认网易云
        """.trimMargin()
        val random = """[Random]
            |一些关于"随机"的指令
            |  - 随机数 <数字> <另一个数字>
            |       返回在两个数字区间之内的一个随机数, 若不指定则默认为 1~100 的区间, 最大区间为 -10000~10000
            |  - 打乱 <需要打乱的内容>
            |       打乱的内容需要用空格隔开, 如 `打乱 张三 李四 王五`
            |  - 抽签 <需要从中抽签的内容>
            |       抽签的内容需要用空格隔开, 如 `抽签 起床 睡觉`
        """.trimMargin()
        val reaction = """[Reaction]
            |识别群内变化做出的响应
            |如匿名设置发生变化时会发消息
            |新群友入群时主动欢迎
            | > 并没有什么用, 考虑后续更新中取消或添加开关
        """.trimMargin()
        val remind = """[Remind]
            |自定义的定时(延时)提醒
            |可用指令:
            |  - <数字><分/时/.../秒>后提醒我<做什么>
            |       如 `三分钟后提醒我起床`, `三天后提醒我交作业`, "后提醒我" 是必须的. 不稳定, 在bot更新/重启后会被取消, 在后续更新中会修复 
        """.trimMargin()
        val scheduler = """[Scheduler]
            |定点消息, 六点时会发送 `早`, 十二点时会发送 `🕛`
        """.trimMargin()
        val setting = """[Setting]
            |还没写完
        """.trimMargin()
        val tuling = """[Tuling]
            |对接图灵对话, @bot 的消息会与图灵交互
        """.trimMargin()
        val thesaurus = """[Thesaurus]
            |自定义词库, 各群独立
            |可用指令:
            |  - 添加问<Q>答<A>
            |       每次消耗 200 铜币
        """.trimMargin()
        val woPay = """[WoPay]
            |可用指令:
            |  - @bot token
            |  - 查询到期
        """.trimMargin()
    }

    fun getDesc() =
        when (ePlugin) {
            Plugin.EPlugin.AntiLightApp -> antiLightAppDesc
            Plugin.EPlugin.Bili -> bili
            Plugin.EPlugin.BuiltInReply -> builtInReply
            Plugin.EPlugin.Button -> button
            Plugin.EPlugin.CheckIn -> checkIn
            Plugin.EPlugin.Help -> help
            Plugin.EPlugin.Information -> information
            Plugin.EPlugin.Music -> music
            Plugin.EPlugin.Random -> random
            Plugin.EPlugin.Reaction -> reaction
            Plugin.EPlugin.Remind -> remind
            Plugin.EPlugin.Scheduler -> scheduler
            Plugin.EPlugin.Setting -> setting
            Plugin.EPlugin.Tuling -> tuling
            Plugin.EPlugin.Thesaurus -> thesaurus
            Plugin.EPlugin.WoPay -> woPay
            else -> null
        }
}