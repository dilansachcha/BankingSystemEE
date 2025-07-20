package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.model.User;

import java.util.List;

@Stateless
public class UserTransactionHistoryServiceBean {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    public List<Transaction> getAllTransactionsForUser(User user) {
        return em.createQuery("""
    SELECT t FROM Transaction t WHERE t.account.user = :user ORDER BY t.transactionTime DESC""", Transaction.class)
                .setParameter("user", user)
                .getResultList();
    }
}
