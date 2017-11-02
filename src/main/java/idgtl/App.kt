package idgtl

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.support.FileSystemXmlApplicationContext
import org.telegram.telegrambots.ApiContextInitializer

/**
 * Created by d.asadullin on 12.10.2016.
 */
object App {
    val logger = LoggerFactory.getLogger(Bot::class.java.getSimpleName())
    @JvmStatic fun main(args: Array<String>) {
        try {
            ApiContextInitializer.init();
            val ctx = FileSystemXmlApplicationContext("/opt/inyoupantsbot/conf/inyoupanstbot.xml")
          //  val ctx = ClassPathXmlApplicationContext("local.xml")

            val bot = ctx.getBean<Bot>(Bot::class.java)
            bot.start()
            Thread.sleep(100000);
            System.exit(1);
        }catch ( ex:Exception){
           logger.error("cannot start",ex)
        }

    }
}
