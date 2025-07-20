package lk.fortyfourss.ejb.bankingsystemee.singleton;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.ScheduledTransaction;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;


@Singleton
public class ScheduledTransactionPollingBean {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    @EJB
    private TransactionServiceBean transactionService;

    @EJB
    private AccountService accountService;

    @Schedule(hour="*", minute="*/5", persistent=false)
    @jakarta.ejb.TransactionAttribute(jakarta.ejb.TransactionAttributeType.REQUIRES_NEW)
    public void processScheduledTransactions() {
        List<ScheduledTransaction> due = em.createQuery(
                "SELECT s FROM ScheduledTransaction s WHERE s.status = 'PENDING' AND s.scheduledTime <= CURRENT_TIMESTAMP",
                ScheduledTransaction.class
        ).getResultList();

        for (ScheduledTransaction s : due) {
            try {
                Account fromAcc = accountService.getAccountByNumber(s.getFromAccount());
                accountService.validateTransferConditions(fromAcc, s.getAmount());
                transactionService.transfer(s.getFromAccount(), s.getToAccount(), s.getAmount());
                s.setStatus("COMPLETED");
                s.setLastExecuted(new Timestamp(System.currentTimeMillis()));
                em.merge(s);

                if (s.isRecurring()) {
                    Timestamp next = calculateNextTime(s.getScheduledTime(), s.getRecurrenceType());
                    s.setScheduledTime(next);
                    s.setNextScheduledTime(next);
                    s.setStatus("PENDING");
                    s.setRetryCount(0);
                    em.merge(s);
                }

            } catch (Exception e) {
                s.setRetryCount(s.getRetryCount() + 1);
                if (s.getRetryCount() >= 3) {
                    s.setStatus("FAILED");
                }
                em.merge(s);
            }
        }
    }

    private Timestamp calculateNextTime(Timestamp current, String recurrenceType) {
        LocalDateTime ldt = current.toLocalDateTime();
        if ("DAILY".equalsIgnoreCase(recurrenceType)) {
            ldt = ldt.plusDays(1);
        } else if ("WEEKLY".equalsIgnoreCase(recurrenceType)) {
            ldt = ldt.plusWeeks(1);
        }
        return Timestamp.valueOf(ldt);
    }
}

