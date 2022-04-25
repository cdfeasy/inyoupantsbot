package idgtl

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.User
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.scheduleAtFixedRate


@Component()
@Scope("singleton")
class InMemoryData {
    internal var admins: ConcurrentHashMap<Int, List<Long>> = ConcurrentHashMap()
    internal var counterCount: ConcurrentHashMap<Long, HashMap<Int, Int>> = ConcurrentHashMap()
    internal var counters: ConcurrentHashMap<Int, HashMap<String, Int>> = ConcurrentHashMap()
    internal var countersQueue = LinkedBlockingQueue<Long>()
    internal var date: Instant = Instant.now()
    internal var DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    internal var timer = Timer("indata", false)

    fun start() {
        timer.scheduleAtFixedRate(1000, 1000) {
            addCounters()
        }
        timer.scheduleAtFixedRate(600000, 600000) {
            clear()
        }
    }

    fun stop() {
        timer.cancel()
    }

    fun refreshAdmins(chatId: Int, users: List<User>) {
        var userIds = users.map { it -> it.id }
        admins.put(chatId, userIds)
    }

    fun isAdmin(chatId: Int, userId: Long): Boolean {
        val isAdmin = admins.get(chatId)?.contains(userId)
        if (isAdmin == null) {
            return false
        }
        return isAdmin
    }

    private fun getHour(): Int {
        return LocalDateTime.now().hour
    }

    fun addCounters() {
        while (!countersQueue.isEmpty()) {
            val chatId = countersQueue.poll()
            val hour = getHour()
            counterCount.putIfAbsent(chatId, HashMap())
            var value = counterCount.get(chatId)!![hour]
            if (value == null) {
                value = 0
            }
            counterCount.get(chatId)!!.put(hour, value + 1)
        }
    }

    fun clear() {
        for (entry in counterCount.values) {
            entry.remove((getHour() + 1) % 24)
        }
    }

    fun addCounter(chatId: Long) {
        countersQueue.add(chatId)
    }

    fun getCounters(chatId: Long): String {
        val hour = getHour()
        var sb = StringBuilder()
        sb.append("restart: ").append(DATE_TIME_FORMATTER.format(date)).append("\n")
        if (counterCount.containsKey(chatId)) {
            var cnt = 0
            for (i in 0..23) {
                cnt += counterCount.get(chatId)?.get(i) ?: 0
            }
            sb.append("this chat today: ").append(cnt).append("\n")
        }
        var cnt = 0
        for (entry in counterCount.values) {
            for (i in 0..23) {
                cnt += entry.get(i) ?: 0
            }
        }
        sb.append("all chats today: ").append(cnt)
        return sb.toString()
    }

}

object InMemoryDataObj {
    val logger = LoggerFactory.getLogger(Bot::class.java.getSimpleName())

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            var InMemoryData: InMemoryData = InMemoryData()
            InMemoryData.start()
            InMemoryData.addCounter(10)
            Thread.sleep(450)
            InMemoryData.addCounter(10)
            Thread.sleep(450)
            InMemoryData.addCounter(10)
            InMemoryData.addCounter(10)
            InMemoryData.addCounter(10)
            InMemoryData.addCounter(10)
            Thread.sleep(450)
            InMemoryData.addCounter(10)
            Thread.sleep(450)
            InMemoryData.addCounter(10)
            Thread.sleep(450)
            InMemoryData.addCounter(10)
            InMemoryData.addCounter(11)
            Thread.sleep(450)
            InMemoryData.addCounter(10)
            Thread.sleep(4450)
            System.out.println(InMemoryData.getCounters(10))
            System.out.println(InMemoryData.counterCount.get(10))
            InMemoryData.stop()

        } catch (ex: Exception) {
            logger.error("cannot start", ex)
        }

    }
}

