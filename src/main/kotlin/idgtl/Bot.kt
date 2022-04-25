package idgtl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
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
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


/**
 * Created by d.asadullin on 13.07.2016.
 */
@Component
class Bot : TelegramLongPollingBot {
    private val logger = LoggerFactory.getLogger(Bot::class.java.getSimpleName())
    private val random = Random()
    private val command: Command = Command(this)
    private val percent = AtomicInteger(10)
    lateinit var botSession: BotSession
    val wordBase: WordBase
    val texts: Texts
    val inMemoryData: InMemoryData

    fun start() {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        try {
            botSession = telegramBotsApi.registerBot(this)
        } catch (e: TelegramApiRequestException) {
            logger.error("cannot start telegramm", e)
        }
    }


    fun stop() {
        botSession.stop()
    }

    val _botUsername: String
    val _botToken: String

    override fun getBotUsername(): String? = _botUsername

    @Autowired
    constructor(
        wordBase: WordBase,
        texts: Texts,
        inMemoryData: InMemoryData,
        @Value("\${botUsername}") _botUsername: String,
        @Value("\${botToken}") _botToken: String
    ) : super() {
        this._botUsername = _botUsername
        this._botToken = _botToken
        this.texts = texts
        this.wordBase = wordBase
        this.inMemoryData = inMemoryData
    }

    override fun getBotToken(): String? = _botToken


    private fun checkTableflip(message: Message): Boolean {
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
        if (message == null) {
            return
        }
        val chatId = message?.chatId ?: 0
        if (chatId == 0L) {
            logger.error("unknown error in " + message)
        }
        val isRus = wordBase.isRus(chatId);
        if (message == null || message.date == null || Date().time / 1000 - message.date!! >= 10) {
            return
        }
        if (!message.hasText()) {
            return
        }
        val msg = message.text
        if (checkTableflip(message)) {
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
        val parts = message.text.lowercase().split(" ");

        for (part in parts) {
            if (texts.isVerb(part, isRus)) {
                when (random.nextInt(2)) {
                    0 -> sendMsg(message, "${texts.verbPrefix(isRus)} $part", false, date)
                    1 -> sendMsg(message, "$part ${texts.verbPostfix(isRus)}", false, date)
                    else -> sendMsg(message, "${texts.verbPrefix(isRus)} $part", false, date)
                }
                return
            }
            if (wordBase.containWord(part, chatId)) {
                when (random.nextInt(5)) {
                    1 -> sendMsg(message, "${part} ${texts.postfix(isRus)}", false, date)
                    2 -> sendMsg(message, "${texts.prefix(isRus)} $part", false, date)
                    3 -> sendMsg(message, "${texts.prefix(isRus)} $part, ${texts.add(isRus)}", false, date)
                    4 -> sendMsg(message, "$part + ${texts.postfix(isRus)}, ${texts.add(isRus)}", false, date)
                    else -> sendMsg(message, "${texts.prefix(isRus)} $part", false, date)
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
            var text = message.text.lowercase()
            while (text.endsWith("(") ||
                text.endsWith(")") ||
                text.endsWith("!") ||
                text.endsWith("?") ||
                text.endsWith(".") ||
                text.endsWith(",")
            ) {
                text = text.substring(0, text.length - 1)
            }
            if (text.length <= 2) {
                return;
            }
            when (random.nextInt(6)) {
                1 -> sendMsg(message, "$text ${texts.postfix(isRus)}", false, date)
                2 -> sendMsg(message, "${texts.prefix(isRus)} $text", false, date)
                3 -> sendMsg(message, "$text ${texts.postfix(isRus)}, ${texts.add(isRus)}", false, date)
                4 -> sendMsg(message, "${texts.prefix(isRus)} $text, ${texts.add(isRus)}", false, date)
                else -> sendMsg(message, "${texts.prefix(isRus)} $text", false, date)
            }
            return
        }
    }

    fun sendMsg(message: Message, text: String, reply: Boolean = false, date: Long, isService: Boolean = false) {
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
        if (!isService) {
            inMemoryData.addCounter(message.chatId!!)
            logger.info(
                "send {} {} {} for {}/{}",
                message.from.id,
                message.from.userName,
                text,
                System.currentTimeMillis() - date,
                System.currentTimeMillis() - start
            )
        }
    }

    fun checkAdmin(chatId: String) {
        val msg = GetChatAdministrators()
        msg.chatId = chatId
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