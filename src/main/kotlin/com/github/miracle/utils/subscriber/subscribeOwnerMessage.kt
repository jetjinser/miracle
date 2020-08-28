package com.github.miracle.utils.subscriber

import com.github.miracle.SecretConfig.owner
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.event.*
import net.mamoe.mirai.message.MessageEvent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun <R> CoroutineScope.subscribeOwnerMessage(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: MessagePacketSubscribersBuilder.() -> R
): R {
    return MessagePacketSubscribersBuilder(Unit)
    { filter, messageListener: MessageListener<MessageEvent, Unit> ->
        subscribeAlways(coroutineContext, concurrencyKind, priority) {
            if (sender.id != owner) return@subscribeAlways
            val toString = this.message.contentToString()
            if (filter.invoke(this, toString))
                messageListener.invoke(this, toString)
        }
    }.run(listeners)
}