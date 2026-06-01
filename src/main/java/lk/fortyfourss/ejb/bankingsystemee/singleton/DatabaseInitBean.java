package lk.fortyfourss.ejb.bankingsystemee.singleton;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.util.EncryptionUtil;

@Singleton
@Startup
public class DatabaseInitBean {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        try {
            long adminCount = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'", Long.class).getSingleResult();

            // Auto-inject Recruiter Admin if database is empty
            if (adminCount == 0) {
                User admin = new User();
                admin.setFullName("System Administrator");
                admin.setEmail("admin@fortress.com");
                admin.setPassword(EncryptionUtil.hashPassword("Admin@123"));
                admin.setNic("000000000v");
                admin.setMobile("0770000000");
                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");
                admin.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

                em.persist(admin);
                System.out.println("✅ RECRUITER ADMIN CREATED: admin@fortress.com");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Init Bean Skipped: " + e.getMessage());
        }
    }
}