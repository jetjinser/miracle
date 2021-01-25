package com.github.miracle.utils.expand

import com.github.miracle.SecretConfig.owner
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


//fun <R> CoroutineScope.subscribeOwnerMessage(
//    coroutineContext: CoroutineContext = EmptyCoroutineContext,
//    concurrencyKind: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
//    priority: EventPriority = EventPriority.MONITOR,
//    listeners: MessageEventSubscribersBuilder.() -> R
//): R {
//    return MessagePacketSubscribersBuilder(Unit)
//    { filter, messageListener: MessageListener<MessageEvent, Unit> ->
//        subscribeAlways(coroutineContext, concurrencyKind, priority) {
//            if (sender.id != owner) return@subscribeAlways
//            val toString = this.message.contentToString()
//            if (filter.invoke(this, toString))
//                messageListener.invoke(this, toString)
//        }
//    }.run(listeners)
//}