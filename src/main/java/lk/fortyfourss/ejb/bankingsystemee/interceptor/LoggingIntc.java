package lk.fortyfourss.ejb.bankingsystemee.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger;

@Logging
@Interceptor
public class LoggingIntc {

    private static final Logger LOGGER = Logger.getLogger(LoggingIntc.class.getName());

    @AroundInvoke
    public Object logMethod(InvocationContext ctx) throws Exception {
        LOGGER.info("[LoggingIntc] Method START: " + ctx.getMethod().getName());

        Object result;
        try {
            result = ctx.proceed();
            LOGGER.info("[LoggingIntc] Method END: " + ctx.getMethod().getName());
        } catch (Exception e) {
            LOGGER.warning("[LoggingIntc] Exception in method: " + ctx.getMethod().getName() + " Exception: " + e.getMessage());
            throw e;
        }
        return result;
    }
}
