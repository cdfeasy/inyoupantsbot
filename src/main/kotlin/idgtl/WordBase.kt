package idgtl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Created by dmitry on 30.12.2016.
 */
@Component()
@Scope("singleton")
open class WordBase {
    val STATES_PATH = "STATES_PATH";
    val logger = LoggerFactory.getLogger(WordBase::class.java.getSimpleName())
    val random = Random()
    val fullState: StateConfig
    val global: State
    val prefs: HashMap<Long, State>
    var changed: Boolean = false

    @Autowired
    constructor() {
        prefs = HashMap()
        val mapper = ObjectMapper().registerKotlinModule()
        val path = System.getenv().get(STATES_PATH)
        fullState = mapper.readValue(Files.newBufferedReader(Paths.get(path)), StateConfig::class.java)
        global = fullState.global
        val specialMap = HashMap<String, String>()
        for (entry in global.specials!!) {
            val parts = entry.key.split(",")
            if (parts.size == 1) {
                specialMap.put(entry.key, entry.value)
            } else {
                parts.forEach { specialMap.put(it, entry.value); }
            }
        }

        global.specialsMapped = specialMap;
        for (pref in fullState.chatPrefs) {
            pref.specialsMapped = HashMap<String, String>()
            prefs.put(pref.chatId, pref)
            if (pref.specials != null)
                for (entry in pref.specials!!) {
                    val parts = entry.key.split(",")
                    if (parts.size == 1) {
                        pref.specialsMapped!!.put(entry.key, entry.value)
                    } else {
                        parts.forEach { pref.specialsMapped!!.put(it, entry.value); }
                    }
                }
        }
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
            {
                if (changed) {
                    try {
                        val json = mapper.writeValueAsString(fullState)
                        val writer = Files.newBufferedWriter(Paths.get(path))
                        writer.write(json)
                        writer.flush()
                        writer.close()
                        changed = false
                    } catch (ex: Exception) {
                        logger.error("IO error", ex)
                    }
                }
            }, 60, 60, TimeUnit.SECONDS
        )
    }

    fun getTables(): List<String> {
        return global.tables!!
    }

    fun isAdmin(id: String): Boolean {
        return global.admins!!.contains(id)
    }

    fun addAdmin(id: String) {
        if (!global.admins!!.contains(id)) {
            changed = true
            (global.admins as ArrayList).add(id)
        }
    }

    fun setLang(chatId: Long, isRus: Boolean) {
        if (!prefs.containsKey(chatId)) {
            changed = true;
            val cp = State(
                chatId, ArrayList(), ArrayList(), ArrayList(), ArrayList(), HashMap(),
                HashMap(), ArrayList(), isRus
            )
            prefs.put(chatId, cp)
            (fullState.chatPrefs as ArrayList).add(cp)
        }
    }

    fun exists(chatId: Long) {
        if (!prefs.containsKey(chatId)) {
            changed = true;
            val cp = State(
                chatId, ArrayList(), ArrayList(), ArrayList(), ArrayList(), HashMap(),
                HashMap(), ArrayList()
            )
            prefs.put(chatId, cp)
            (fullState.chatPrefs as ArrayList).add(cp)
        }
    }

    fun isRus(chatId: Long): Boolean {
        return prefs.get(chatId)?.chatLang ?: true
    }

    fun addSingle(singleWord: String, chatId: Long?) {
        if (chatId == null) {
            if (!global.single!!.contains(singleWord) && global.single?.size ?: 0 < 10000) {
                (global.single as ArrayList).add(singleWord);
                changed = true
            }
        } else {
            if (!(prefs.get(chatId)?.single?.contains(singleWord)
                    ?: true) && (prefs.get(chatId)?.single?.size ?: 0) < 10000
            ) {
                (prefs.get(chatId)?.single as ArrayList).add(singleWord)
                changed = true
            }
        }
    }

    fun removeSingle(singleWord: String, chatId: Long?) {
        if (chatId == null) {
            (global.single as ArrayList).remove(singleWord)
            changed = true
        } else {
            (prefs.get(chatId)?.single as ArrayList)?.remove(singleWord)
            changed = true
        }
    }

    fun addSpecial(specialWord: String, specialVal: String, chatId: Long?) {
        if (chatId == null) {
            if (!global.specials!!.containsKey(specialWord) && global.specials!!.size < 10000) {
                global.specials!!.put(specialWord, specialVal)
                var parts = specialWord.split(",")
                if (parts.size == 1) {
                    global.specialsMapped!!.put(specialWord, specialVal)
                } else {
                    parts.forEach { global.specialsMapped!!.put(it, specialVal); }
                }
                changed = true
            }
        } else {
            if (!(prefs[chatId]?.specials?.contains(specialWord) ?: true) &&
                (prefs[chatId]?.specials?.size ?: 0) < 10000
            ) {
                prefs.get(chatId)?.specials?.put(specialWord, specialVal)
                var parts = specialWord.split(",")
                if (parts.size == 1) {
                    prefs[chatId]?.specialsMapped?.put(specialWord, specialVal)
                } else {
                    parts.forEach { prefs[chatId]?.specialsMapped?.put(it, specialVal); }
                }
                changed = true
            }
        }
    }

    fun removeSpecial(specialWord: String, chatId: Long?) {
        if (chatId == null) {
            global.specials!!.remove(specialWord)
            var parts = specialWord.split(",");
            if (parts.size == 1) {
                global.specialsMapped!!.remove(specialWord)
            } else {
                parts.forEach { global.specialsMapped!!.remove(it); }
            }
            changed = true
        } else {
            prefs.get(chatId)?.specials?.remove(specialWord)
            var parts = specialWord.split(",")
            if (parts.size == 1) {
                prefs.get(chatId)?.specialsMapped?.remove(specialWord)
            } else {
                parts.forEach { prefs.get(chatId)?.specialsMapped?.remove(it); }
            }
            changed = true
        }
    }


    fun getRandomTable(): String {
        return global.tables!![random.nextInt(global.tables!!.size)]
    }

    fun containWord(word: String, chatId: Long): Boolean {
        return global.single!!.contains(word) || prefs.get(chatId)?.single?.contains(word) ?: false
    }

    fun containPhrase(text: String, chatId: Long): String? {
        for (phrase in global.phrase!!) {
            if (text.contains(phrase.key)) {
                return phrase.value
            }
        }
        val pref = prefs.get(chatId)
        if (pref != null && pref.phrase != null) {
            for (phrase in pref.phrase!!) {
                if (text.contains(phrase.key)) {
                    return phrase.value
                }
            }
        }
        return null
    }

    fun specialWord(word: String, chatId: Long): String? {
        var res = global.specialsMapped!!.get(word)
        if (res != null) {
            var parts = res.split(";")
            return parts[random.nextInt(parts.size)]
        } else {
            val pref = prefs.get(chatId)
            if (pref != null) {
                res = pref.specialsMapped!!.get(word);
                if (res != null) {
                    var parts = res.split(";")
                    return parts[random.nextInt(parts.size)]
                }
            }
        }
        return null;
    }

}
