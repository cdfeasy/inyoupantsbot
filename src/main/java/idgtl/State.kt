package idgtl

import com.google.gson.Gson
import java.util.*

/**
 * Created by dmitry on 01.01.2017.
 */
class State {
    var chatId:Long=0
    lateinit var single:List<String>
    lateinit var tables:List<String>
    lateinit var contains:List<String>
    lateinit var ignore:List<String>
    lateinit var specials:HashMap<String,String>
    lateinit var phrase:HashMap<String,String>
    @Transient
    lateinit var specialsMapped:HashMap<String,String>
    lateinit var admins:List<String>
    lateinit var chatPrefs:List<ChatPrefs>



    companion object {
        @JvmStatic fun main(args: Array<String>) {
             var base= State();
            base.single= listOf("aaaa");
            base.tables= listOf("aaaa");
            base.contains= listOf("aaaa");
            base.ignore= listOf("aaaa");
            base.admins= listOf("aaaa");
          //  base.specials= mapOf("aaaa" to "bbb","ccc" to "eee");
            var gson= Gson();
            System.out.println(gson.toJson(base));

        }
    }


}
