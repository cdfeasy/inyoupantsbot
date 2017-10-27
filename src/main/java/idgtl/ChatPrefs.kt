package idgtl

import java.util.*

class ChatPrefs {
    lateinit var single:List<String>;
    lateinit var specials:Map<String,String>;
    lateinit var phrase:Map<String,String>;
    @Transient
    lateinit  var specialsMapped:Map<String,String>;
    var chatId:Long=0;
    var chatLang:Boolean=true;
    constructor(){
        single=ArrayList();
        specials=HashMap();
        specialsMapped=HashMap();
        phrase=HashMap();
    }
}