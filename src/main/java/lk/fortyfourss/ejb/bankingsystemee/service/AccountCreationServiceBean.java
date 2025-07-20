package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.fortyfourss.ejb.bankingsystemee.exception.AccountCreationException;
import lk.fortyfourss.ejb.bankingsystemee.interceptor.Audit;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.AccountType;
import lk.fortyfourss.ejb.bankingsystemee.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Stateless
public class AccountCreationServiceBean {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    public List<Account> getAccountsByUser(User user) {
        return em.createQuery("SELECT a FROM Account a WHERE a.user = :user", Account.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Audit
    public void createAccount(User user, AccountType accountType, double initialDeposit, Integer maturityMonths) {
        long count = em.createQuery("SELECT COUNT(a) FROM Account a WHERE a.user = :user", Long.class)
                .setParameter("user", user)
                .getSingleResult();
        if (count >= 8) {
            throw new AccountCreationException("One user can only have up to 8 accounts.");
        }

        double minDeposit = switch (accountType) {
            case SAVINGS -> 2000;
            case CHECKING -> 1000;
            case FIXED -> 5000;
        };

        if (initialDeposit < minDeposit) {
            throw new AccountCreationException("Minimum deposit for " + accountType + " is " + minDeposit);
        }

        String accNo = "USR" + user.getId() + "-" + accountType.name().substring(0, 3) + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(accNo);
        account.setAccountType(accountType.name());
        account.setBalance(initialDeposit);
        account.setInitialDeposit(initialDeposit);
        account.setCreatedAt(LocalDateTime.now());
        account.setLastUpdated(LocalDateTime.now());
        account.setStatus("ACTIVE");

        // Maturity
        if (accountType == AccountType.FIXED && maturityMonths != null) {
            account.setMaturityDate(LocalDateTime.now().plusMonths(maturityMonths));
            account.setMaturityStatus("ONGOING");
        } else {
            account.setMaturityDate(null);
            account.setMaturityStatus(null);
        }

        em.persist(account);
    }
}
