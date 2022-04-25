package idgtl

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class State(
    @JsonProperty("chatId") var chatId: Long = 0,
    @JsonProperty("single") var single: List<String>?,
    @JsonProperty("tables") var tables: List<String>?,
    @JsonProperty("contains") var contains: List<String>?,
    @JsonProperty("ignore") var ignore: List<String>?,
    @JsonProperty("specials") var specials: HashMap<String, String>?,
    @JsonProperty("phrase") var phrase: HashMap<String, String>?,
    @JsonProperty("admins") var admins: List<String>?,
    @JsonProperty("chatLang") var chatLang: Boolean = true,
    @JsonIgnore var specialsMapped: HashMap<String, String>? = HashMap()
) {
    constructor(
        chatId: Long = 0,
        single: List<String>?,
        tables: List<String>?,
        contains: List<String>?,
        ignore: List<String>?,
        specials: HashMap<String, String>?,
        phrase: HashMap<String, String>?,
        admins: List<String>?,
        chatLang: Boolean
    ) : this(
        chatId,
        single,
        tables,
        contains,
        ignore,
        specials,
        phrase,
        admins,
        chatLang,
        HashMap()
    )
}


data class StateConfig(
    var global: State,
    var chatPrefs: List<State>
)

