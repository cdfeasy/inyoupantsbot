package idgtl

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

/**
 * Created by d.asadullin on 12.10.2016.
 */
@Aspect
class Catch {

    @Around("execution(* org.telegram.telegrambots.api.objects.MessageEntity.computeText(..))")
    @Throws(Throwable::class)
    fun profile(pjp: ProceedingJoinPoint): Any {
        try {
            val output = pjp.proceed()
            return output
        } catch (ex: Exception) {
            return ""
        }

    }
    companion object {

        @JvmStatic fun aspectOf(): Catch {
            return Catch()
        }
    }

}
