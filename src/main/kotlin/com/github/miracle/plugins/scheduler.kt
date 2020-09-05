package com.github.miracle.plugins

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule
import kotlin.random.Random

fun calendarGen(hourOfDay: Int): Calendar {
    return Calendar.getInstance().also {
        it.set(Calendar.MINUTE, 0)
        it.set(Calendar.SECOND, 0)
        if (it.get(Calendar.HOUR_OF_DAY) > hourOfDay) it.add(Calendar.DATE, 1)
        it.set(Calendar.HOUR_OF_DAY, hourOfDay)
    }
}

fun Bot.scheduler() {
    suspend fun sendToEveryGroup(message: String) {
        this.groups.forEach {
            delay(Random.nextLong(0, 5))
            it.sendMessage(message)
        }
    }

    Timer().apply {
        schedule(calendarGen(6).time, TimeUnit.DAYS.toMillis(1)) {
            launch { sendToEveryGroup("早") }
        }
        // need fix 立即执行了
        schedule(calendarGen(0).time, TimeUnit.DAYS.toMillis(1)) {
            launch { sendToEveryGroup("🕛") }
        }
    }
}