package lk.fortyfourss.ejb.bankingsystemee.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Logger;

@Performance
@Interceptor
public class PerformanceIntc {

    private static final Logger LOGGER = Logger.getLogger(PerformanceIntc.class.getName());

    @AroundInvoke
    public Object logPerformance(InvocationContext ctx) throws Exception {
        long start = System.nanoTime();
        Object result = ctx.proceed();
        long end = System.nanoTime();
        LOGGER.info("[PERFORMANCE] " + ctx.getMethod().getName() +
                " executed in " + (end - start) / 1_000_000 + " ms");
        return result;
    }
}
