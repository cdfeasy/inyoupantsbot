package idgtl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.TelegramBotsApi
import org.telegram.telegrambots.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import org.telegram.telegrambots.exceptions.TelegramApiRequestException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Created by d.asadullin on 13.07.2016.
 */
@Component
class Bot : TelegramLongPollingBot() {
    companion object {
        val logger = LoggerFactory.getLogger(Bot::class.java.getSimpleName())
    }

    internal var random = Random();
    internal var percent = AtomicInteger(10);
    @Inject
    lateinit var wordBase: WordBase;

    fun start() {

        val telegramBotsApi = TelegramBotsApi()
        try {
            val botSession = telegramBotsApi.registerBot(this)
        } catch (e: TelegramApiRequestException) {
            logger.error("cannot start telegramm",e)
        }

    }

    lateinit var _botUsername: String
        set;

    override fun getBotUsername(): String? = _botUsername

    lateinit var _botToken: String
        set;

    override fun getBotToken(): String? = _botToken


    private fun checkTableflip(msg: String, message: Message): Boolean {
        if ("/tableflip" == message.text || "/tableflip@InYourPantsBot" == message.text) {
            sendMsg(message, wordBase.getRandomTable(),false,System.currentTimeMillis());
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
        var date=System.currentTimeMillis();
        val message = update.message

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

        if (message == null || !message.hasText() || random.nextInt(100) > percent.get()) {
            return
        }
        val parts = message.text.toLowerCase().split(" ");

        for (part in parts) {
            if ((part.endsWith("ай") || part.endsWith("уй"))&& !part.equals("хуй")) {
                when (random.nextInt(2)) {
                    0 -> sendMsg(message, "Штаны себе сперва " + part,false,date)
                    1 -> sendMsg(message, part + " в штаны себе, пёс",false,date)
                    else -> sendMsg(message, "Штаны себе сперва " + part,false,date)
                }
                return
            }
            if (wordBase.containWord(part)) {
                when (random.nextInt(7)) {
                    1 -> sendMsg(message, part + " у тебя в штанах",false,date)
                    2 -> sendMsg(message, "В штанах твоих " + part,false,date)
                    3 -> sendMsg(message, part + " у тебя в штанах",false,date)
                    4 -> sendMsg(message, "В штанах твоих " + part,false,date)
                    5 -> sendMsg(message, part + " у тебя в штанах, пёс",false,date)
                    6 -> sendMsg(message, "В штанах твоих " + part+", пёс",false,date)
                    else -> sendMsg(message, "В штанах у тебя "+part,false,date)
                }
                return
            }
            var spec=wordBase.specialWord(part);
            if (spec!=null) {
                sendMsg(message, spec,false,date)
                return
            }
        }

        if (!message.text.startsWith("/") && parts.size <= 2 && !message.text.contains("http")) {
            var text = message.text
            text = text.toLowerCase()
            if (text.endsWith("!") || text.endsWith("?") || text.endsWith(".") || text.endsWith(",")) {
                text = text.substring(0, text.length - 1)
            }
            if(text.length<=2){
                return;
            }
            when (random.nextInt(5)) {
                1 -> sendMsg(message, text + " у тебя в штанах",false,date)
                2 -> sendMsg(message, "В штанах твоих " + text,false,date)
                3 -> sendMsg(message, text + " у тебя в штанах",false,date)
                4 -> sendMsg(message, "В штанах твоих " + text,false,date)
                5 -> sendMsg(message, text + " у тебя в штанах, пёс",false,date)
                6 -> sendMsg(message, "В штанах твоих " + text+", пёс",false,date)
                else -> sendMsg(message, "В штанах у тебя "+text,false,date)
            }
            return
        }
    }

    private fun sendMsg(message: Message, text: String, reply: Boolean = false,date:Long) {
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.chatId = message.chatId!!.toString()
        if (reply) {
            sendMessage.replyToMessageId = message.messageId
        }
        sendMessage.text = text
        var start=System.currentTimeMillis();
        try {
            sendMessage(sendMessage)
        } catch (e: TelegramApiException) {
            logger.error("cannot send "+text,e);
        }
        logger.info("send {} {} {} for {}/{}",message.from.id,message.from.userName, text,System.currentTimeMillis()-date,System.currentTimeMillis()-start);

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
            answerInlineQuery(inlineQuery)
        } catch (e: TelegramApiException) {
            logger.error("cannot send inline",e);
        }

    }
}