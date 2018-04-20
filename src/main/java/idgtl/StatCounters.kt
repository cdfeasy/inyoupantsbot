package idgtl

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.*

@Component()
@Scope("singleton")
class StatCounters {
    internal var counters: Map<Long,HashMap<String, Long>> = HashMap<Long,HashMap<String, Long>>();
    fun increment(isRus: Boolean):String{
      //  return if(isRus) prefix1Rus else prefix1Eng;
        return "";
    }

}