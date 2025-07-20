package lk.fortyfourss.ejb.bankingsystemee.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Logger;

@Interceptor
@Audit
public class AuditIntc {
    private static final Logger LOGGER = Logger.getLogger(AuditIntc.class.getName());
    @AroundInvoke
    public Object logAudit(InvocationContext ctx) throws Exception {
        LOGGER.info("[AUDIT] Business Action: " + ctx.getMethod().getName());
        return ctx.proceed();
    }
}
