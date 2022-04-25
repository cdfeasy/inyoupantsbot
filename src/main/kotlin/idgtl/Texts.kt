package idgtl

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.random.Random

@Component
@Scope("singleton")
class Texts {
    private val logger = LoggerFactory.getLogger(Bot::class.java.getSimpleName())
    val TEXTS_PATH = "TEXTS_PATH";
    var rus: TextVariants
    var eng: TextVariants
    val random = Random.Default
    fun isVerb(word: String, isRus: Boolean): Boolean {
        if (isRus) {
            return (word.endsWith("ай") || word.endsWith("уй")) && !word.equals("хуй");
        } else {
            return false;
        }
    }

    fun prefix(isRus: Boolean): String {
        return if (isRus) rus.textPrefix[random.nextInt(rus.textPrefix.size)] else eng.textPrefix[random.nextInt(eng.textPrefix.size)];
    }

    fun postfix(isRus: Boolean): String {
        return if (isRus) rus.textPostfix[random.nextInt(rus.textPostfix.size)] else eng.textPostfix[random.nextInt(eng.textPostfix.size)];
    }

    fun verbPrefix(isRus: Boolean): String {
        return if (isRus) rus.verbPrefix[random.nextInt(rus.verbPrefix.size)] else eng.verbPrefix[random.nextInt(eng.verbPrefix.size)];
    }

    fun verbPostfix(isRus: Boolean): String {
        return if (isRus) rus.verbPostfix[random.nextInt(rus.verbPostfix.size)] else eng.verbPostfix[random.nextInt(eng.verbPostfix.size)];
    }

    fun add(isRus: Boolean): String {

        return if (isRus) {
            val r = random.nextInt(rus.addon.size)
            // logger.info("$r ${rus.addon.size}")
            rus.addon[r]
        } else eng.addon[random.nextInt(eng.addon.size)];
    }

    @Autowired
    constructor() {
        val mapper = ObjectMapper().registerKotlinModule()
        val path = System.getenv().get(TEXTS_PATH)
        if (path != null) {
            val readValue = mapper.readValue(Files.newBufferedReader(Paths.get(path)), TextsInfo::class.java)
            eng = readValue.eng
            rus = readValue.rus
        } else {
            val readValue = mapper.readValue(this.javaClass.getResourceAsStream("texts.json"), TextsInfo::class.java)
            eng = readValue.eng
            rus = readValue.rus
        }
    }

}

data class TextVariants(
    @JsonProperty("textPrefix") val textPrefix: List<String>,
    @JsonProperty("textPostfix") val textPostfix: List<String>,
    @JsonProperty("addon") val addon: List<String>,
    @JsonProperty("verbPrefix") val verbPrefix: List<String>,
    @JsonProperty("verbPostfix") val verbPostfix: List<String>
)

data class TextsInfo(
    @JsonProperty("rus") val rus: TextVariants,
    @JsonProperty("eng") val eng: TextVariants
)