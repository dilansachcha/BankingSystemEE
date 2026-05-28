package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.User;

import java.util.List;
import java.util.Properties;

@Stateless
@PermitAll
public class AdminAccountServiceBean {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    public List<Account> getAllAccounts() {
        return em.createQuery("SELECT a FROM Account a ORDER BY a.createdAt DESC", Account.class).getResultList();
    }

    public void blockAccount(int accountId, String reason) {
        Account acc = em.find(Account.class, accountId);
        if (acc != null) {
            acc.setStatus("BLOCKED");
            em.merge(acc);

            String userEmail = acc.getUser().getEmail();
            sendEmail(userEmail, "Account Blocked", "Your account (" + acc.getAccountNumber() + ") was blocked.\nReason: " + reason);
        }
    }

    public void unblockAccount(int accountId) {
        Account acc = em.find(Account.class, accountId);
        if (acc != null) {
            acc.setStatus("ACTIVE");
            em.merge(acc);

            String userEmail = acc.getUser().getEmail();
            sendEmail(userEmail, "Account Unblocked", "Your account (" + acc.getAccountNumber() + ") has been unblocked and is now ACTIVE.");
        }
    }

    public Account getAccountById(int id) {
        return em.find(Account.class, id);
    }

    private void sendEmail(String recipient, String subject, String text) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("dilansachintha44@gmail.com", "uztn zoen wxse kjja");
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setText("Your email content...", "UTF-8");
            msg.setFrom(new InternetAddress("dilansachintha44@gmail.com"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            msg.setSubject(subject);
            msg.setText(text);
            Transport.send(msg);
            System.out.println("[MAIL SENT] to " + recipient + " | " + subject);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
