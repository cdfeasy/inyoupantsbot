package idgtl

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.api.objects.User
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Component()
@Scope("singleton")
class InMemoryData {
    internal var admins: ConcurrentHashMap<Int, List<Int>> = ConcurrentHashMap()
    internal var counterCount: ConcurrentHashMap<Int, HashMap<Long, Int>> = ConcurrentHashMap()
    internal var counters: ConcurrentHashMap<Int, HashMap<String, Int>> = ConcurrentHashMap()
    internal var date: Instant = Instant.now()
    internal var DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

    fun refreshAdmins(chatId: Int, users: List<User>) {
        var userIds = users.map { it -> it.id }
        admins.put(chatId, userIds)
    }

    fun isAdmin(chatId: Int, userId: Int): Boolean {
        val isAdmin = admins.get(chatId)?.contains(userId)
        if (isAdmin == null) {
            return false
        }
        return isAdmin;
    }

    private fun getHour(): Long {
        return Instant.now().toEpochMilli() / 1000;
    }

    fun addCounter(chatId: Int) {
        val hour = getHour();
        if (counterCount.containsKey(chatId)) {
            var value = counterCount.get(chatId)?.get(hour)
            if (value == null) {
                value = 0
            }
            counterCount.get(chatId)?.put(hour, value + 1);
        } else {
            counterCount.put(chatId, HashMap());
            counterCount.get(chatId)?.put(hour, 1);
        }
    }

    fun getCounters(chatId: Int): String {
        val hour = getHour();
        var sb = StringBuilder()
        sb.append("restart: ").append(DATE_TIME_FORMATTER.format(date)).append("\n")
        if (counterCount.containsKey(chatId)) {
            var cnt = 0
            for (i in 0..24) {
                cnt += counterCount.get(chatId)?.get(hour - i) ?: 0
            }
            sb.append("this chat today: ").append(cnt).append("\n")

        }
        var cnt = 0
        for (entry in counterCount.values) {
            for (i in 0..24) {
                cnt += entry.get(hour - i) ?: 0
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
            var InMemoryData: InMemoryData = InMemoryData();
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
            Thread.sleep(450)
            System.out.println(InMemoryData.getCounters(10))


        } catch (ex: Exception) {
            logger.error("cannot start", ex)
        }

    }
}

