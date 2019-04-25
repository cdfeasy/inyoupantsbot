package idgtl

import java.util.*

class ChatPrefs {
    lateinit var single:List<String>;
    lateinit var specials:HashMap<String,String>;
    lateinit var phrase:HashMap<String,String>;
    @Transient
    lateinit  var specialsMapped:HashMap<String,String>;
    var chatId:Long=0;
    var chatLang:Boolean=true;
    constructor(){
        single=ArrayList();
        specials=HashMap();
        specialsMapped=HashMap();
        phrase=HashMap();
    }
}