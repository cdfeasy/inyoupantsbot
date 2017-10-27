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

private val s = "В штанах твоих "

/**
 * Created by d.asadullin on 13.07.2016.
 */
@Component
class Bot : TelegramLongPollingBot() {
    companion object {
        val logger = LoggerFactory.getLogger(Bot::class.java.getSimpleName())
    }

    internal var random = Random();
    internal var texts = Texts();
    internal var percent = AtomicInteger(50);
    @Inject
    lateinit var wordBase: WordBase;

    fun start() {

        val telegramBotsApi = TelegramBotsApi()
        try {
            val botSession = telegramBotsApi.registerBot(this)
        } catch (e: TelegramApiRequestException) {
            logger.error("cannot start telegramm", e)
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
            sendMsg(message, wordBase.getRandomTable(), false, System.currentTimeMillis());
            return true
        }
        return false
    }

    private fun checkCommand(msg: String, message: Message, chatId: Long): Boolean {
        if (message.text.startsWith("/edit")) {
            if("/edit help".equals(message.text)){
                val text="/edit addLocal pants (only for this group)\n" +
                        "/edit addGlobal pants (all groups, admin only)\n"+
                        "/edit removeLocal pants\n"+
                        "/edit removeGlobal pants\n"+
                        "/edit addSpecialLocal pants1,pants2=pants in you pants\n"+
                        "/edit addSpecialGlobal pants1,pants2=pants in you pants\n"+
                        "/edit removeSpecialLocal pants1,pants2\n"+
                        "/edit removeSpecialGlobal pants1,pants2\n";
                sendMsg(message, text, false, System.currentTimeMillis())
                return true;
            }
            val parts = message.text.split(" ");
            if (parts.size < 3) return false;
            when (parts[1]) {
                "addLocal" ->
                    if (parts[2].contains(" "))
                        sendMsg(message, "Incorrect value, cannot contain spaces", false, System.currentTimeMillis())
                    else {
                        wordBase.exists(chatId);
                        wordBase.addSingle(parts[2], chatId)
                        sendMsg(message, "Ok", false, System.currentTimeMillis())
                    }
                "addGlobal" ->
                    if (!wordBase.isAdmin(message.from.id.toString())) {
                        sendMsg(message, "You are not admin", false, System.currentTimeMillis())
                        return false;
                    } else {
                        if (parts[2].contains(" "))
                            sendMsg(message, "Incorrect value, cannot contain spaces", false, System.currentTimeMillis())
                        else {
                            wordBase.addSingle(parts[2], null)
                            sendMsg(message, "Ok", false, System.currentTimeMillis())
                        }
                    }
                "removeLocal" -> {
                    wordBase.exists(chatId);
                    wordBase.removeSingle(parts[2], chatId)
                }
                "removeGlobal" ->
                    if (!wordBase.isAdmin(message.from.id.toString())) {
                        sendMsg(message, "You are not admin", false, System.currentTimeMillis())
                        return false;
                    } else {
                        wordBase.removeSingle(parts[2], null)
                        sendMsg(message, "Ok", false, System.currentTimeMillis())
                    }
                "addSpecialLocal" -> {
                    val keyValue = message.text.replace("/edit addSpecialLocal ", "").split("=");
                    if (keyValue.size != 2) {
                        sendMsg(message, "Incorrect value, template bla1,bla2=text", false, System.currentTimeMillis())
                    } else {
                        wordBase.exists(chatId);
                        wordBase.addSpecial(keyValue[0], keyValue[1], chatId);
                        sendMsg(message, "Ok", false, System.currentTimeMillis())
                    }
                }
                "addSpecialGlobal" -> {
                    if (!wordBase.isAdmin(message.from.id.toString())) {
                        sendMsg(message, "You are not admin", false, System.currentTimeMillis())
                        return false;
                    }
                    val keyValue = message.text.replace("/edit addSpecialGlobal ", "").split("=");
                    if (keyValue.size != 2) {
                        sendMsg(message, "Incorrect value, template bla1,bla2=text", false, System.currentTimeMillis())
                    } else {
                        wordBase.addSpecial(keyValue[0], keyValue[1], null);
                        sendMsg(message, "Ok", false, System.currentTimeMillis())
                    }
                }
                "removeSpecialLocal" -> {
                    val text = message.text.replace("/edit removeSpecialLocal ", "");
                    wordBase.exists(chatId);
                    wordBase.removeSpecial(text, chatId);
                    sendMsg(message, "Ok", false, System.currentTimeMillis())
                }
                "removeSpecialGlobal" -> {
                    if (!wordBase.isAdmin(message.from.id.toString())) {
                        sendMsg(message, "You are not admin", false, System.currentTimeMillis())
                        return false;
                    }
                    val text = message.text.replace("/edit removeSpecialGlobal ", "");
                    wordBase.removeSpecial(text, null);
                    sendMsg(message, "Ok", false, System.currentTimeMillis())
                }
            }
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
        val chatId = message?.chatId?:0;
        if(chatId==0L){
            logger.error("unknown error in "+message.text);
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
            if (checkCommand(msg, message, chatId)) {
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
                    6 -> sendMsg(message, texts.prefix1(isRus) + part + ", "+texts.dog1(isRus), false, date)
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
                6 -> sendMsg(message, texts.prefix1(isRus) + text + ", "+texts.dog1(isRus), false, date)
                else -> sendMsg(message, texts.prefix2(isRus) + text, false, date)
            }
            return
        }
    }

    private fun sendMsg(message: Message, text: String, reply: Boolean = false, date: Long) {
        val sendMessage = SendMessage()
        sendMessage.enableMarkdown(true)
        sendMessage.chatId = message.chatId!!.toString()
        if (reply) {
            sendMessage.replyToMessageId = message.messageId
        }
        sendMessage.text = text
        var start = System.currentTimeMillis();
        try {
            sendMessage(sendMessage)
        } catch (e: TelegramApiException) {
            logger.error("cannot send " + text, e);
        }
        logger.info("send {} {} {} for {}/{}", message.from.id, message.from.userName, text, System.currentTimeMillis() - date, System.currentTimeMillis() - start);

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
            logger.error("cannot send inline", e);
        }

    }
}