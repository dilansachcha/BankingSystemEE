package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.fortyfourss.ejb.bankingsystemee.model.ScheduledTransaction;

import java.util.List;

@Stateless
public class ScheduledTransactionServiceBean {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    public List<ScheduledTransaction> getScheduledTransactionsByUser(int userId) {
        return em.createQuery("SELECT s FROM ScheduledTransaction s WHERE s.userId = :userId", ScheduledTransaction.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public ScheduledTransaction getById(int id) {
        return em.find(ScheduledTransaction.class, id);
    }

    public void delete(ScheduledTransaction st) {
        if (em.contains(st)) {
            em.remove(st);
        } else {
            em.remove(em.merge(st));
        }
    }

    public void persist(ScheduledTransaction st) {
        em.persist(st);
    }

    public List<ScheduledTransaction> getPending() {
        return em.createQuery("SELECT s FROM ScheduledTransaction s WHERE s.status='PENDING'", ScheduledTransaction.class).getResultList();
    }

}

