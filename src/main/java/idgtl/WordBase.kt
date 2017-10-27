package idgtl

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

/**
 * Created by dmitry on 30.12.2016.
 */
@Component()
@Scope("singleton")
class WordBase {
    companion object {
        val logger = LoggerFactory.getLogger(WordBase::class.java.getSimpleName())
    }
    internal var random = Random()
    lateinit internal var state: State;
    @Value("\${state}")
    lateinit var conf: String
    lateinit var prefs: HashMap<Long, ChatPrefs>;
    var changed: Boolean = false;
    @PostConstruct
    fun init() {
        var gson = GsonBuilder().setPrettyPrinting().create();
        prefs = HashMap();
        state = gson.fromJson(Files.newBufferedReader(Paths.get(conf)), State::class.java);
        //  state = gson.fromJson(InputStreamReader(javaClass.getResourceAsStream("/state.json")), State::class.java);
        var specialMap = HashMap<String, String>();
        for (entry in state.specials) {
            var parts = entry.key.split(",");
            if (parts.size == 1) {
                specialMap.put(entry.key, entry.value);
            } else {
                parts.forEach { specialMap.put(it, entry.value); }
            }
        }

        state.specialsMapped = specialMap;
        for (pref in state.chatPrefs) {
            var specialMapLoc = HashMap<String, String>();
            prefs.put(pref.chatId, pref);
            for (entry in pref.specials) {
                var parts = entry.key.split(",");
                if (parts.size == 1) {
                    specialMapLoc.put(entry.key, entry.value);
                } else {
                    parts.forEach { specialMapLoc.put(it, entry.value); }
                }
            }
            pref.specialsMapped = specialMapLoc;
        }
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
                {
                    if (changed) {
                        try {
                            val json=gson.toJson(state);
                            val writer=Files.newBufferedWriter(Paths.get(conf))
                            writer.write(json);
                            writer.flush();
                            writer.close();
                            changed=false;
                        }catch (ex: Exception){
                            logger.error("IO error",ex)
                        }
                    }
                }, 60, 60, TimeUnit.SECONDS);
    }

    fun getTables(): List<String> {
        return state.tables;
    }

    fun isAdmin(id: String): Boolean {
        return state.admins.contains(id);
    }

    fun addAdmin(id: String) {
        if (!state.admins.contains(id)) {
            changed = true;
            (state.admins as ArrayList).add(id);
        }
    }

    fun setLang(chatId: Long, isRus: Boolean) {
        if (!prefs.containsKey(chatId)) {
            changed = true;
            val cp = ChatPrefs();
            cp.chatId = chatId;
            cp.chatLang = isRus;
            prefs.put(chatId, cp);
            (state.chatPrefs as ArrayList).add(cp);
        }
    }

    fun exists(chatId: Long) {
        if (!prefs.containsKey(chatId)) {
            changed = true;
            val cp = ChatPrefs();
            cp.chatId = chatId;
            prefs.put(chatId, cp);
            (state.chatPrefs as ArrayList).add(cp);
        }
    }

    fun isRus(chatId: Long): Boolean {
        return prefs.get(chatId)?.chatLang ?: true;
    }

    fun addSingle(singleWord: String, chatId: Long?) {
        if (chatId == null) {
            if (!state.single.contains(singleWord) && state.single.size < 10000) {
                (state.single as ArrayList).add(singleWord);
                changed = true;
            }
        } else {
            if (!(prefs.get(chatId)?.single?.contains(singleWord) ?: true) && prefs.get(chatId)?.single?.size ?: 0 < 10000) {
                (prefs.get(chatId)?.single as ArrayList).add(singleWord);
                changed = true;
            }
        }
    }

    fun removeSingle(singleWord: String, chatId: Long?) {
        if (chatId == null) {
            (state.single as ArrayList).remove(singleWord);
            changed = true;
        } else {
            (prefs.get(chatId)?.single as ArrayList)?.remove(singleWord);
            changed = true;
        }
    }

    fun addSpecial(specialWord: String, specialVal: String, chatId: Long?) {
        if (chatId == null) {
            if (!state.specials.containsKey(specialWord) && state.specials.size < 10000) {
                (state.specials as HashMap).put(specialWord, specialVal);
                var parts = specialWord.split(",");
                if (parts.size == 1) {
                    (state.specialsMapped as HashMap).put(specialWord, specialVal);
                } else {
                    parts.forEach { (state.specialsMapped as HashMap).put(it, specialVal); }
                }
                changed = true;
            }
        } else {
            if (!(prefs.get(chatId)?.specials?.contains(specialWord) ?: true) && prefs.get(chatId)?.specials?.size ?: 0 < 10000) {
                (prefs.get(chatId)?.specials as HashMap).put(specialWord, specialVal);
                var parts = specialWord.split(",");
                if (parts.size == 1) {
                    (prefs.get(chatId)?.specialsMapped as HashMap).put(specialWord, specialVal);
                } else {
                    parts.forEach { (prefs.get(chatId)?.specialsMapped as HashMap).put(it, specialVal); }
                }
                changed = true;
            }
        }
    }

    fun removeSpecial(specialWord: String, chatId: Long?) {
        if (chatId == null) {
            (state.specials as HashMap).remove(specialWord)
            var parts = specialWord.split(",");
            if (parts.size == 1) {
                (state.specialsMapped as HashMap).remove(specialWord);
            } else {
                parts.forEach { (state.specialsMapped as HashMap).remove(it); }
            }
            changed = true;
        } else {
            (prefs.get(chatId)?.specials as HashMap).remove(specialWord)
            var parts = specialWord.split(",");
            if (parts.size == 1) {
                (prefs.get(chatId)?.specialsMapped as HashMap)?.remove(specialWord);
            } else {
                parts.forEach { (prefs.get(chatId)?.specialsMapped as HashMap)?.remove(it); }
            }
            changed = true;
        }
    }


    fun getRandomTable(): String {
        return state.tables[random.nextInt(state.tables.size)]
    }

    fun containWord(word: String, chatId: Long): Boolean {
        return state.single.contains(word) || prefs.get(chatId)?.single?.contains(word) ?: false;
    }

    fun containPhrase(text: String, chatId: Long): String? {
        for (phrase in state.phrase) {
            if (text.contains(phrase.key)) {
                return phrase.value;
            }
        }
        val pref = prefs.get(chatId);
        if (pref != null) {
            for (phrase in pref.phrase) {
                if (text.contains(phrase.key)) {
                    return phrase.value;
                }
            }
        }
        return null;
    }

    fun specialWord(word: String, chatId: Long): String? {
        var res = state.specialsMapped.get(word);
        if (res != null) {
            var parts = res.split(";");
            return parts[random.nextInt(parts.size)];
        } else {
            val pref = prefs.get(chatId)
            if (pref != null) {
                res = pref.specialsMapped.get(word);
                if (res != null) {
                    var parts = res.split(";");
                    return parts[random.nextInt(parts.size)];
                }
            }
        }
        return null;
    }

}
