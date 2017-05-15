package idgtl

import com.google.gson.Gson
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * Created by dmitry on 01.01.2017.
 */
class Config {
    lateinit var single:List<String>;
    lateinit var tables:List<String>;
    lateinit var contains:List<String>;
    lateinit var ignore:List<String>;
    lateinit var specials:Map<String,String>;
    lateinit var admins:List<String>;
    lateinit var chatPrefs:Map<String,Map<String,String>>;



    companion object {
        @JvmStatic fun main(args: Array<String>) {
             var base= Config();
            base.single= listOf("aaaa");
            base.tables= listOf("aaaa");
            base.contains= listOf("aaaa");
            base.ignore= listOf("aaaa");
            base.admins= listOf("aaaa");
            base.specials= mapOf("aaaa" to "bbb","ccc" to "eee");
            var gson= Gson();
            System.out.println(gson.toJson(base));

        }
    }


}
