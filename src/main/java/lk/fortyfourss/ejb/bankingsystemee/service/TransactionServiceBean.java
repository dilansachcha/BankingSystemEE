package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import lk.fortyfourss.ejb.bankingsystemee.interceptor.Audit;
import lk.fortyfourss.ejb.bankingsystemee.interceptor.Logging;
import lk.fortyfourss.ejb.bankingsystemee.interceptor.Performance;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.singleton.NotificationPublisherBean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@Logging
@Performance
@TransactionManagement(TransactionManagementType.BEAN)
@PermitAll
public class TransactionServiceBean {

    @EJB
    private AccountService accountService;

    @EJB
    private NotificationPublisherBean notificationPublisher;

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    @Resource
    private UserTransaction transaction;

    private static final Logger LOGGER = Logger.getLogger(TransactionServiceBean.class.getName());

    @Audit
    public void transfer(String fromAccNo, String toAccNo, double amount) {
        LOGGER.info("[TransactionService] Transfer Initiated from " + fromAccNo + " to " + toAccNo + " Amount=" + amount);
        try {
            transaction.begin();

            Account fromAcc = accountService.getAccountByNumber(fromAccNo);
            Account toAcc = accountService.getAccountByNumber(toAccNo);

            accountService.validateTransferConditions(fromAcc, amount);

            if ("BLOCKED".equalsIgnoreCase(fromAcc.getStatus())) {
                throw new RuntimeException("Transaction Blocked: Source account BLOCKED!");
            }
            if ("FIXED".equalsIgnoreCase(fromAcc.getAccountType())) {
                throw new RuntimeException("Transaction Blocked: Source account FIXED DEPOSIT!");
            }
            if ("FIXED".equalsIgnoreCase(toAcc.getAccountType())) {
                throw new RuntimeException("Destination account FIXED DEPOSIT cannot receive funds!");
            }


            accountService.debitFromAccount(fromAccNo, amount);
            accountService.creditToAccount(toAccNo, amount);

            logTransaction(fromAcc, "DEBIT", amount, "Transferred to AccNo " + toAccNo);
            logTransaction(toAcc, "CREDIT", amount, "Received from AccNo " + fromAccNo);

            transaction.commit();
            LOGGER.info("[TransactionService] Transfer Successful");

            //WebSocket Broadcast
            String wsMessage = String.format("{\"type\":\"TRANSFER\", \"from\":\"%s\", \"to\":\"%s\", \"amount\":%.2f}",
                    fromAccNo, toAccNo, amount);
            lk.fortyfourss.ejb.bankingsystemee.websocket.AdminNotificationWebSocket.broadcast(wsMessage);

            if (amount >= 50000.0) {
                notificationPublisher.sendHighAmountTransaction(fromAcc.getUser().getEmail(), amount);
            }
            LOGGER.info("[TransactionService] High Amount Notification Sent for amount: " + amount);

        } catch (Exception e) {
            LOGGER.warning("[TransactionService] Transfer Failed - Rolling Back: " + e.getMessage());
            try {
                transaction.rollback();
            } catch (Exception ex) {
                LOGGER.severe("[TransactionService] Rollback Failed: " + ex.getMessage());
            }
            throw new RuntimeException("Transaction Failed: " + e.getMessage());
        }
    }


    @Audit
    public void closeFixedDeposit(String fromAccNo, String toAccNo, double amount) {
        LOGGER.info("[TransactionService] Closing Fixed Deposit from " + fromAccNo + " to " + toAccNo + " Amount=" + amount);
        try {
            transaction.begin();

            Account fromAcc = accountService.getAccountByNumber(fromAccNo);
            Account toAcc = accountService.getAccountByNumber(toAccNo);

            if (!"FIXED".equalsIgnoreCase(fromAcc.getAccountType())) {
                throw new RuntimeException("Not a fixed deposit account!");
            }
            if ("FIXED".equalsIgnoreCase(toAcc.getAccountType())) {
                throw new RuntimeException("Destination cannot be a Fixed Deposit!");
            }

            double balance = fromAcc.getBalance();
            double initial = fromAcc.getInitialDeposit();
            double interestEarned = balance - initial;

            LOGGER.info("[FixedDeposit Closure] Interest Earned=" + interestEarned + ", will be deducted.");

            accountService.debitFromAccountWithoutBlock(fromAccNo, balance);
            accountService.creditToAccount(toAccNo, initial);

            logTransaction(fromAcc, "DEBIT", balance, "Fixed Deposit Closure Transfer (interest deducted)");
            logTransaction(toAcc, "CREDIT", initial, "Received Initial Deposit from Fixed Closure");

            transaction.commit();
            LOGGER.info("[TransactionService] Fixed Deposit Closure Successful");

            //WebSocket Broadcast
            String wsMessage = String.format("{\"type\":\"TRANSFER\", \"from\":\"%s\", \"to\":\"%s\", \"amount\":%.2f}",
                    fromAccNo, toAccNo, amount);
            lk.fortyfourss.ejb.bankingsystemee.websocket.AdminNotificationWebSocket.broadcast(wsMessage);

        } catch (Exception e) {
            LOGGER.warning("[TransactionService] Closure Failed Rolling Back: " + e.getMessage());
            try { transaction.rollback(); } catch (Exception ex) {}
            throw new RuntimeException("Fixed Deposit Closure Failed: " + e.getMessage());
        }
    }



    public void logTransaction(Account account, String type, double amount, String description) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setTransactionType(type);
        tx.setAmount(BigDecimal.valueOf(amount));
        tx.setDescription(description);
        tx.setTransactionTime(new Timestamp(System.currentTimeMillis()));
        em.persist(tx);
        LOGGER.info("[TransactionService] Transaction Logged: " + type + " | AccNo=" + account.getAccountNumber() + " | Amount=" + amount);
    }

    public Transaction getTransactionById(int transactionId) {
        return em.find(Transaction.class, transactionId);
    }

    public List<Transaction> getAllTransactionsWithDetails() {
        return em.createQuery("SELECT t FROM Transaction t JOIN FETCH t.account a JOIN FETCH a.user u ORDER BY t.transactionTime DESC", Transaction.class)
                .getResultList();
    }

    @Audit
    public void withdrawMaturedFixedDeposit(Account fixed, Account target) {
        try {
            transaction.begin();

            double amount = fixed.getBalance();

            accountService.creditToAccount(target.getAccountNumber(), amount);
            logTransaction(target, "CREDIT", amount, "Withdrawn Matured Fixed Deposit from AccNo " + fixed.getAccountNumber());

            fixed.setStatus("CLOSED");
            fixed.setBalance(0);
            fixed.setLastUpdated(LocalDateTime.now());
            accountService.updateAccount(fixed);

            transaction.commit();
            LOGGER.info("[TransactionService] Matured FD Withdrawal Successful from " + fixed.getAccountNumber() + " to " + target.getAccountNumber());

            // WebSocket Broadcast
            String wsMessage = String.format("{\"type\":\"TRANSFER\", \"from\":\"%s\", \"to\":\"%s\", \"amount\":%.2f}",
                    fixed.getAccountNumber(), target.getAccountNumber(), amount);
            lk.fortyfourss.ejb.bankingsystemee.websocket.AdminNotificationWebSocket.broadcast(wsMessage);

        } catch (Exception e) {
            try { transaction.rollback(); } catch (Exception ex) {}
            throw new RuntimeException("Withdrawal Failed: " + e.getMessage());
        }
    }


}
