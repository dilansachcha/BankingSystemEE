package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.util.EncryptionUtil;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Properties;

@Stateless
@Named
public class UserService {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    @Transactional
    public void register(User user) {
        em.persist(user);
    }

    @Transactional
    public void updateUserStatus(int userId, String status) {
        User user = em.find(User.class, userId);
        if (user != null) {
            user.setStatus(status);
            em.merge(user);
        }
    }

    public boolean emailExists(String email) {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    public boolean nicExists(String nic) {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.nic = :nic", Long.class)
                .setParameter("nic", nic)
                .getSingleResult();
        return count > 0;
    }

    public List<User> getPendingUsers() {
        List<User> pending = em.createQuery("SELECT u FROM User u WHERE u.status = 'INACTIVE'", User.class)
                .getResultList();
        System.out.println("Found Pending Users = " + pending.size());
        return pending;
    }

    public boolean validate(String email, String password){
        User user = findByEmail(email);
        if (user == null) return false;

        boolean isCorrect = EncryptionUtil.verifyPassword(password, user.getPassword());

        if (isCorrect && user.getPassword().length() == 64) {
            user.setPassword(EncryptionUtil.hashPassword(password));
            em.merge(user);
            System.out.println("🔒 [Security] Upgraded user " + email + " to BCrypt!");
        }

        return isCorrect;
    }

    public User findById(int userId) {
        return em.find(User.class, userId);
    }

    public User findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    @Transactional
    public void approveUser(int userId) {
        User u = em.find(User.class, userId);
        u.setStatus("ACTIVE");
        em.merge(u);
    }

    @Transactional
    public boolean assignVerificationCode(String email) {
        User user = findByEmail(email);
        if (user == null) return false;
        if ("ADMIN".equalsIgnoreCase(user.getRole())) return false;
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        user.setVerificationCode(code);
        em.merge(user);
        sendForgotPasswordEmail(user.getEmail(), code);
        return true;
    }

    @Transactional
    public boolean validateVerificationCode(String email, String code) {
        User user = findByEmail(email);
        return user != null && code.equals(user.getVerificationCode());
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = findByEmail(email);
        if (user != null) {
            user.setPassword(EncryptionUtil.hashPassword(newPassword)); // <-- Updated!
            user.setVerificationCode(null);
            em.merge(user);
        }
    }

    private void sendForgotPasswordEmail(String recipient, String code) {
        String subject = "Password Reset Code - Banking System";
        String text = "Your password reset code is: " + code + "\nPlease use it on the reset password popup.";
        sendEmail(recipient, subject, text);
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
            System.out.println("[MAIL SENT - Forgot Password] to " + recipient);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public boolean assignAdminVerificationCode(String email) {
        User user = findByEmail(email);
        if (user == null) return false;
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        user.setAdminVerificationCode(code);
        em.merge(user);
        sendAdminOtpEmail(user.getEmail(), code);
        return true;
    }

    private void sendAdminOtpEmail(String recipient, String code) {
        String subject = "Admin OTP Verification - Banking System";
        String text = "Your Admin OTP Code is: " + code + "\nUse this code to complete your login.";
        sendEmail(recipient, subject, text);
    }

    public boolean validateAdminVerificationCode(String email, String code) {
        User user = findByEmail(email);
        return user != null && code.equals(user.getAdminVerificationCode());
    }

    @Transactional
    public void clearAdminVerificationCode(String email) {
        User user = findByEmail(email);
        if (user != null) {
            user.setAdminVerificationCode(null);
            em.merge(user);
        }
    }




}
