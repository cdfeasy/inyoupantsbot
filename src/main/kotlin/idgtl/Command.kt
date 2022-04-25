package idgtl

import org.telegram.telegrambots.meta.api.objects.Message

class Command(val bot: Bot) {
    fun checkCommand(msg: String, message: Message, chatId: Long): Boolean {
        if ("/admins".equals(message.text)) {
            bot.checkAdmin(chatId.toString())
            return true
        }
        if ("/counters".equals(message.text)) {
            val text = bot.inMemoryData.getCounters(chatId)
            bot.sendMsg(message, text, false, System.currentTimeMillis(), true)
            return true
        }
        if (message.text.startsWith("/edit")) {
            if ("/edit help".equals(message.text)) {
                val text = "" +
                        "Пока в разработе\n" +
                        "/edit addLocal pants (only for this group)\n" +
                        "/edit addGlobal pants (all groups, admin only)\n" +
                        "/edit removeLocal pants\n" +
                        "/edit removeGlobal pants\n" +
                        "/edit addSpecialLocal pants1,pants2=pants in you pants\n" +
                        "/edit addSpecialGlobal pants1,pants2=pants in you pants\n" +
                        "/edit removeSpecialLocal pants1,pants2\n" +
                        "/edit removeSpecialGlobal pants1,pants2\n";
                bot.sendMsg(message, text, false, System.currentTimeMillis())
                return true
            }
            if(true){
                return true
            }


            val parts = message.text.split(" ");
            if (parts.size < 3) return false;
            when (parts[1]) {
                "addLocal" ->
                    if (parts[2].contains(" "))
                        bot.sendMsg(
                            message,
                            "Incorrect value, cannot contain spaces",
                            false,
                            System.currentTimeMillis()
                        )
                    else {
                        bot.wordBase.exists(chatId);
                        bot.wordBase.addSingle(parts[2], chatId)
                        bot.sendMsg(message, "Ok", false, System.currentTimeMillis())
                    }
                "addGlobal" ->
                    if (!bot.wordBase.isAdmin(message.from.id.toString())) {
                        bot.sendMsg(message, "You are not admin", false, System.currentTimeMillis())
                        return false;
                    } else {
                        if (parts[2].contains(" "))
                            bot.sendMsg(
                                message,
                                "Incorrect value, cannot contain spaces",
                                false,
                                System.currentTimeMillis()
                            )
                        else {
                            bot.wordBase.addSingle(parts[2], null)
                            bot.sendMsg(message, "Ok", false, System.currentTimeMillis())
                        }
                    }
                "removeLocal" -> {
                    bot.wordBase.exists(chatId);
                    bot.wordBase.removeSingle(parts[2], chatId)
                }
                "removeGlobal" ->
                    if (!bot.wordBase.isAdmin(message.from.id.toString())) {
                        bot.sendMsg(message, "You are not admin", false, System.currentTimeMillis())
                        return false;
                    } else {
                        bot.wordBase.removeSingle(parts[2], null)
                        bot.sendMsg(message, "Ok", false, System.currentTimeMillis())
                    }
                "addSpecialLocal" -> {
                    val keyValue = message.text.replace("/edit addSpecialLocal ", "").split("=");
                    if (keyValue.size != 2) {
                        bot.sendMsg(
                            message,
                            "Incorrect value, template bla1,bla2=text",
                            false,
                            System.currentTimeMillis()
                        )
                    } else {
                        bot.wordBase.exists(chatId);
                        bot.wordBase.addSpecial(keyValue[0], keyValue[1], chatId);
                        bot.sendMsg(message, "Ok", false, System.currentTimeMillis())
                    }
                }
                "addSpecialGlobal" -> {
                    if (!bot.wordBase.isAdmin(message.from.id.toString())) {
                        bot.sendMsg(message, "You are not admin", false, System.currentTimeMillis())
                        return false;
                    }
                    val keyValue = message.text.replace("/edit addSpecialGlobal ", "").split("=");
                    if (keyValue.size != 2) {
                        bot.sendMsg(
                            message,
                            "Incorrect value, template bla1,bla2=text",
                            false,
                            System.currentTimeMillis()
                        )
                    } else {
                        bot.wordBase.addSpecial(keyValue[0], keyValue[1], null);
                        bot.sendMsg(message, "Ok", false, System.currentTimeMillis())
                    }
                }
                "removeSpecialLocal" -> {
                    val text = message.text.replace("/edit removeSpecialLocal ", "");
                    bot.wordBase.exists(chatId);
                    bot.wordBase.removeSpecial(text, chatId);
                    bot.sendMsg(message, "Ok", false, System.currentTimeMillis())
                }
                "removeSpecialGlobal" -> {
                    if (!bot.wordBase.isAdmin(message.from.id.toString())) {
                        bot.sendMsg(message, "You are not admin", false, System.currentTimeMillis())
                        return false;
                    }
                    val text = message.text.replace("/edit removeSpecialGlobal ", "");
                    bot.wordBase.removeSpecial(text, null);
                    bot.sendMsg(message, "Ok", false, System.currentTimeMillis())
                }
            }
            return true
        }
        return false
    }


}