package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lk.fortyfourss.ejb.bankingsystemee.interceptor.Logging;
import lk.fortyfourss.ejb.bankingsystemee.interceptor.LoggingIntc;
import lk.fortyfourss.ejb.bankingsystemee.interceptor.Performance;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.model.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@Logging
@Performance
public class AccountService {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    private static final Logger LOGGER = Logger.getLogger(AccountService.class.getName());

    @Transactional
    public void updateBalances() {
        List<Account> accounts = em.createQuery("SELECT a FROM Account a", Account.class).getResultList();

        for (Account acc : accounts) {
            if ("BLOCKED".equalsIgnoreCase(acc.getStatus())) {
                LOGGER.info("[Interest Skipped] BLOCKED account: " + acc.getAccountNumber());
                continue;
            }

            if (acc.getAccountType().equals("FIXED") && "CLOSED".equalsIgnoreCase(acc.getStatus())) {
                LOGGER.info("[Interest Skipped] CLOSED FIXED account: " + acc.getAccountNumber());
                continue;
            }

            LocalDate today = LocalDate.now();
            LocalDate lastApplied = acc.getLastInterestDate() != null ? acc.getLastInterestDate() : today.minusDays(1);

            double totalIncrement = 0.0;

            while (lastApplied.isBefore(today)) {
                double increment = 0.0;
                if ("FIXED".equalsIgnoreCase(acc.getAccountType())) {
                    increment = acc.getBalance() * 0.10 / 365;
                } else if ("SAVINGS".equalsIgnoreCase(acc.getAccountType())) {
                    increment = acc.getBalance() * 0.04 / 365;
                } else if ("CHECKING".equalsIgnoreCase(acc.getAccountType())) {
                    increment = acc.getBalance() * 0.005 / 365;
                } else {
                    break;
                }
                acc.setBalance(acc.getBalance() + increment);
                acc.setLastInterestDate(lastApplied.plusDays(1));
                logInterestTransaction(acc, increment);
                lastApplied = lastApplied.plusDays(1);
                totalIncrement += increment;
            }
            acc.setLastUpdated(LocalDateTime.now());
            em.merge(acc);

            LOGGER.info("[Balance Updated] Account=" + acc.getAccountNumber() + ", Total Interest Added=" + totalIncrement);
        }
    }




    public void validateTransferConditions(Account from, double amount) {
        if (from.getAccountType().equalsIgnoreCase("SAVINGS") &&
                from.getBalance() - amount < 1000) {
            throw new RuntimeException("Minimum Savings Balance Violation");
        }
        if (from.getBalance() < amount) {
            throw new RuntimeException("Insufficient Balance");
        }
    }

    private void logInterestTransaction(Account account, double increment) {
        String rateDescription = "";
        if ("FIXED".equalsIgnoreCase(account.getAccountType())) {
            rateDescription = "(10% p.a.)";
        } else if ("SAVINGS".equalsIgnoreCase(account.getAccountType())) {
            rateDescription = "(4% p.a.)";
        } else if ("CHECKING".equalsIgnoreCase(account.getAccountType())) {
            rateDescription = "(0.5% p.a.)";
        }

        Transaction interestTx = new Transaction();
        interestTx.setAccount(account);
        interestTx.setTransactionType("CREDIT");
        interestTx.setAmount(BigDecimal.valueOf(increment));
        interestTx.setDescription("Interest Added " + rateDescription);
        interestTx.setTransactionTime(new Timestamp(System.currentTimeMillis()));
        em.persist(interestTx);
        LOGGER.info("[Interest Log] Account=" + account.getAccountNumber() + " | Amount=" + increment + " | Rate=" + rateDescription);
    }

    public void debitFromAccount(String accNo, double amount) {
        Account acc = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accNo", Account.class)
                .setParameter("accNo", accNo).getSingleResult();

        if (acc.getBalance() < amount) throw new RuntimeException("Insufficient Balance");
        acc.setBalance(acc.getBalance() - amount);
        em.merge(acc);
    }

    public void creditToAccount(String accNo, double amount) {
        Account acc = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accNo", Account.class)
                .setParameter("accNo", accNo).getSingleResult();

        acc.setBalance(acc.getBalance() + amount);
        em.merge(acc);
    }

    public Account getAccountByNumber(String accNo) {
        return em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accNo", Account.class)
                .setParameter("accNo", accNo)
                .getSingleResult();
    }

    public List<Account> getAccountsByUserId(int userId) {
        return em.createQuery("SELECT a FROM Account a WHERE a.userId = :uid AND a.status = 'ACTIVE' AND (a.accountType = 'SAVINGS' OR a.accountType = 'CHECKING')", Account.class)
                .setParameter("uid", userId)
                .getResultList();
    }

    public List<Account> getAllAccountsByUserId(int userId) {
        return em.createQuery("SELECT a FROM Account a WHERE a.userId = :uid", Account.class)
                .setParameter("uid", userId)
                .getResultList();
    }

//    public List<Account> getAccountsByUser(User user) {
//        return em.createQuery("SELECT a FROM Account a WHERE a.user = :user AND a.status = 'ACTIVE'", Account.class)
//                .setParameter("user", user)
//                .getResultList();
//
//
//    }

    public List<Account> getNonFixedAccountsByUser(int userId) {
        return em.createQuery("SELECT a FROM Account a WHERE a.userId = :uid AND a.accountType != 'FIXED'\n", Account.class)
                .setParameter("uid", userId)
                .getResultList();
    }

    public Account getAccountById(int id) {
        return em.find(Account.class, id);
    }

    public void updateAccount(Account account) {
        em.merge(account);
    }

    // FIXED dep closing
    public void debitFromAccountWithoutBlock(String accNo, double amount) {
        Account acc = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accNo", Account.class)
                .setParameter("accNo", accNo)
                .getSingleResult();

        if (acc.getBalance() < amount) {
            throw new RuntimeException("Insufficient Balance");
        }

        acc.setBalance(acc.getBalance() - amount);
        acc.setLastUpdated(LocalDateTime.now());
        em.merge(acc);
        LOGGER.info("[AccountService] Debit Without Block | AccNo=" + acc.getAccountNumber() + " | Amount=" + amount);
    }

    public List<Account> getAllFixedActiveAccounts() {
        return em.createQuery("SELECT a FROM Account a WHERE a.accountType = 'FIXED' AND a.status = 'ACTIVE'", Account.class)
                .getResultList();
    }



}
