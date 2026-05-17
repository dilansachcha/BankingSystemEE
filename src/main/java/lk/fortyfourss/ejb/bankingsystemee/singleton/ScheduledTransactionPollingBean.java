package lk.fortyfourss.ejb.bankingsystemee.singleton;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;

import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.ScheduledTransaction;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
@RunAs("SYSTEM_TIMER")
@TransactionManagement(TransactionManagementType.BEAN)
public class ScheduledTransactionPollingBean {

    private static final Logger LOGGER = Logger.getLogger(ScheduledTransactionPollingBean.class.getName());

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    @EJB
    private TransactionServiceBean transactionService;

    @EJB
    private AccountService accountService;

    @Resource
    private UserTransaction utx;

    @Schedule(hour="*", minute="*", persistent=false)
    @PermitAll
    public void processScheduledTransactions() {
        LOGGER.info("--- [POLLING WAKEUP] Checking for due transactions... ---");

        List<ScheduledTransaction> due = null;
        try {
            utx.begin();
            due = em.createQuery(
                    "SELECT s FROM ScheduledTransaction s WHERE s.status = 'PENDING' AND s.scheduledTime <= CURRENT_TIMESTAMP",
                    ScheduledTransaction.class
            ).getResultList();
            utx.commit();
        } catch (Exception e) {
            LOGGER.severe("Failed to fetch due transactions: " + e.getMessage());
            try { utx.rollback(); } catch (Exception ex) {}
            return;
        }

        if (due == null || due.isEmpty()) {
            return;
        }

        LOGGER.info("Found " + due.size() + " transactions ready to execute.");

        for (ScheduledTransaction s : due) {
            try {
                LOGGER.info("Attempting to execute Transaction ID: " + s.getId() + " | Amount: " + s.getAmount());

                Account fromAcc = accountService.getAccountByNumber(s.getFromAccount());
                accountService.validateTransferConditions(fromAcc, s.getAmount());
                transactionService.transfer(s.getFromAccount(), s.getToAccount(), s.getAmount());

                // SUCCESS! Open a fresh transaction to update schedule
                utx.begin();
                s = em.find(ScheduledTransaction.class, s.getId());
                s.setStatus("COMPLETED");
                s.setLastExecuted(new Timestamp(System.currentTimeMillis()));

                if (s.isRecurring()) {
                    Timestamp next = calculateNextTime(s.getScheduledTime(), s.getRecurrenceType());
                    s.setScheduledTime(next);
                    s.setNextScheduledTime(next);
                    s.setStatus("PENDING");
                    s.setRetryCount(0);
                    LOGGER.info("Successfully updated recurring transaction. Next run: " + next);
                } else {
                    LOGGER.info("Successfully completed one-time transaction.");
                }
                em.merge(s);
                utx.commit();

            } catch (Exception e) {
                LOGGER.severe("FAILED to execute Transaction ID: " + s.getId() + " | Reason: " + e.getMessage());

                // FAILURE - Open a fresh transaction to update retry count
                try {
                    utx.begin();
                    s = em.find(ScheduledTransaction.class, s.getId());
                    s.setRetryCount(s.getRetryCount() + 1);
                    if (s.getRetryCount() >= 3) {
                        s.setStatus("FAILED");
                        LOGGER.severe("Transaction ID: " + s.getId() + " reached max retries. Marking as FAILED.");
                    }
                    em.merge(s);
                    utx.commit();
                } catch (Exception ex) {
                    LOGGER.severe("Failed to save retry count for ID: " + s.getId());
                    try { utx.rollback(); } catch (Exception rollbackEx) {}
                }
            }
        }
    }

    private Timestamp calculateNextTime(Timestamp current, String recurrenceType) {
        LocalDateTime nextTime = current.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        while (nextTime.isBefore(now) || nextTime.isEqual(now)) {
            if ("DAILY".equalsIgnoreCase(recurrenceType)) {
                nextTime = nextTime.plusDays(1);
            } else if ("WEEKLY".equalsIgnoreCase(recurrenceType)) {
                nextTime = nextTime.plusWeeks(1);
            } else {
                nextTime = now.plusDays(1);
            }
        }

        return Timestamp.valueOf(nextTime);
    }
}