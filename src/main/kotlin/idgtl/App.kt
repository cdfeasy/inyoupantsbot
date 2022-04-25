package idgtl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * Created by d.asadullin on 12.10.2016.
 */
@Configuration
@ComponentScan(basePackages = ["idgtl"])
@PropertySource("\${SPRING_CONFIG_LOCATION}")
open class App {
    val logger = LoggerFactory.getLogger(Bot::class.java)
    @Autowired
    lateinit var bot:Bot
    @Autowired
    lateinit var base: WordBase
    @Autowired
    lateinit var inMemoryData: InMemoryData

    fun start(){
        bot.start()
        inMemoryData.start()
    }

    fun stop(){
        bot.stop()
        inMemoryData.stop()
    }
}

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(Bot::class.java)
    try {
        //ApiContextInitializer.init();
        // val ctx = FileSystemXmlApplicationContext("/opt/inyoupantsbot/conf/inyoupanstbot.xml")
        val ctx = AnnotationConfigApplicationContext(App::class.java)
        val app = ctx.getBean(App::class.java)
        app.start()
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                logger.info("stopping bot")
                app.stop()
                ctx.stop()
                logger.info("bot stopped")
            }
        })
    } catch (ex: Exception) {
        logger.error("cannot start", ex)
    }
}

