package idgtl

import com.google.inject.Inject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import org.telegram.telegrambots.meta.generics.BotSession
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

private val s = "В штанах твоих "

/**
 * Created by d.asadullin on 13.07.2016.
 */
@Component
class Bot : TelegramLongPollingBot() {
    companion object {
        val logger = LoggerFactory.getLogger(Bot::class.java.getSimpleName())
    }

    internal var random = Random()
    internal var command: Command = Command(this)
    internal var texts = Texts()
    internal var percent = AtomicInteger(50)
    lateinit var botSession: BotSession
    lateinit var wordBase: WordBase

    fun start() {
        val telegramBotsApi = TelegramBotsApi()
        try {
            botSession = telegramBotsApi.registerBot(this)
        } catch (e: TelegramApiRequestException) {
            logger.error("cannot start telegramm", e)
        }

    }
    fun stop() {
        botSession.stop()
    }

    lateinit var _botUsername: String
        set;

    override fun getBotUsername(): String? = _botUsername

    lateinit var _botToken: String
        set;

    override fun getBotToken(): String? = _botToken


    private fun checkTableflip(msg: String, message: Message): Boolean {
        if ("/tableflip" == message.text || "/tableflip@InYourPantsBot" == message.text) {
            sendMsg(message, wordBase.getRandomTable(), false, System.currentTimeMillis());
            return true
        }
        return false
    }

    override fun onUpdateReceived(update: Update) {
        if (update.hasInlineQuery()) {
            if (update.inlineQuery.query != null && update.inlineQuery.query.contains("tableflip")) {
                sendInlineMsg(update.inlineQuery.id)
            }
            return
        }
        var date = System.currentTimeMillis();
        val message = update.message
        val chatId = message?.chatId ?: 0;
        if (chatId == 0L) {
            logger.error("unknown error in " + message.text);
        }

        val isRus = wordBase.isRus(chatId);
        if (message == null || message.date == null || Date().time / 1000 - message.date!! >= 10) {
            return
        }
        if (!message.hasText()) {
            return
        }
        val msg = message.text
        if (checkTableflip(msg, message)) {
            return
        }

        try {
            if (command.checkCommand(msg, message, chatId)) {
                return
            }
        } catch (ex: Exception) {
            sendMsg(message, "error" + ex.message, false, System.currentTimeMillis())
            return
        }

        if (!message.hasText() || random.nextInt(100) > percent.get()) {
            return
        }
        val parts = message.text.toLowerCase().split(" ");

        for (part in parts) {
            if (texts.isVerb(part, isRus)) {
                when (random.nextInt(2)) {
                    0 -> sendMsg(message, texts.verbPrefix1(isRus) + part, false, date)
                    1 -> sendMsg(message, part + texts.verbPostfix1(isRus), false, date)
                    else -> sendMsg(message, texts.verbPrefix1(isRus) + part, false, date)
                }
                return
            }
            if (wordBase.containWord(part, chatId)) {
                when (random.nextInt(7)) {
                    1 -> sendMsg(message, part + texts.postfix1(isRus), false, date)
                    2 -> sendMsg(message, texts.prefix1(isRus) + part, false, date)
                    3 -> sendMsg(message, part + texts.postfix1(isRus), false, date)
                    4 -> sendMsg(message, texts.prefix1(isRus) + part, false, date)
                    5 -> sendMsg(message, part + texts.postfix2(isRus), false, date)
                    6 -> sendMsg(message, texts.prefix1(isRus) + part + ", " + texts.dog1(isRus), false, date)
                    else -> sendMsg(message, texts.prefix2(isRus) + part, false, date)
                }
                return
            }
            var spec = wordBase.specialWord(part, chatId);
            if (spec != null) {
                sendMsg(message, spec, false, date)
                return
            }
        }

        if (!message.text.startsWith("/") && parts.size <= 2 && !message.text.contains("http")) {
            var text = message.text
            text = text.toLowerCase()
            while (text.endsWith("!") || text.endsWith("?") || text.endsWith(".") || text.endsWith(",")) {
                text = text.substring(0, text.length - 1)
            }
            if (text.length <= 2) {
                return;
            }
            when (random.nextInt(7)) {
                1 -> sendMsg(message, text + texts.postfix1(isRus), false, date)
                2 -> sendMsg(message, texts.prefix1(isRus) + text, false, date)
                3 -> sendMsg(message, text + texts.postfix1(isRus), false, date)
                4 -> sendMsg(message, texts.prefix1(isRus) + text, false, date)
                5 -> sendMsg(message, text + texts.postfix2(isRus), false, date)
                6 -> sendMsg(message, texts.prefix1(isRus) + text + ", " + texts.dog1(isRus), false, date)
                else -> sendMsg(message, texts.prefix2(isRus) + text, false, date)
            }
            return
        }
    }

    fun sendMsg(message: Message, text: String, reply: Boolean = false, date: Long) {
        val msg = SendMessage()
        msg.enableMarkdown(true)
        msg.chatId = message.chatId!!.toString()
        if (reply) {
            msg.replyToMessageId = message.messageId
        }
        msg.text = text
        var start = System.currentTimeMillis();
        try {
            execute(msg)
        } catch (e: TelegramApiException) {
            logger.error("cannot send " + text, e);
        }
        logger.info("send {} {} {} for {}/{}", message.from.id, message.from.userName, text, System.currentTimeMillis() - date, System.currentTimeMillis() - start);

    }

    fun checkAdmin(chatId: String) {
        val msg = GetChatAdministrators()
        msg.chatId = chatId
        var start = System.currentTimeMillis();
        try {
            val execute = execute(msg)
            logger.info("ChatAdms {}", execute);
        } catch (e: TelegramApiException) {
            logger.error("cannot send ", e);
        }
    }

    private fun sendInlineMsg(msgId: String) {
        val inlineQuery = AnswerInlineQuery()
        inlineQuery.inlineQueryId = msgId
        inlineQuery.cacheTime = 0
        val list = ArrayList<InlineQueryResult>()
        var i = 1000
        for (s in wordBase.getTables()) {
            val res = InlineQueryResultArticle()
            res.id = Integer.toString(i++)
            res.title = s
            val textMessageContent = InputTextMessageContent()
            textMessageContent.messageText = s
            res.inputMessageContent = textMessageContent
            list.add(res)
        }
        inlineQuery.results = list
        try {
            execute(inlineQuery)
        } catch (e: TelegramApiException) {
            logger.error("cannot send inline", e);
        }

    }
}