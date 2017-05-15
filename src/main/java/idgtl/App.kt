package idgtl

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * Created by d.asadullin on 12.10.2016.
 */
object App {
    val logger = LoggerFactory.getLogger(Bot::class.java.getSimpleName())
    @JvmStatic fun main(args: Array<String>) {
        try {
            val ctx = ClassPathXmlApplicationContext(
                    "alerting.xml")

            val bot = ctx.getBean<Bot>(Bot::class.java)
            bot.start()
        }catch ( ex:Exception){
           logger.error("cannot start",ex)
        }

    }
}
