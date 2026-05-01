package lk.fortyfourss.ejb.bankingsystemee.listener;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import jakarta.jms.MessageListener;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/NewUserTopic"),
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic")
        }
)
public class UserApprovalListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String text = textMessage.getText();

                if (text.startsWith("REGISTERED") || text.startsWith("APPROVED")) {
                    String[] parts = text.split(":", 2);
                    String type = parts[0];
                    String email = parts[1];

                    System.out.println("[UserApprovalListener] Type: " + type + ", Email: " + email);
                    sendEmail(email, type);
                } else {
                    System.out.println("[UserApprovalListener] Ignored non-user message: " + text);
                }
            }
        } catch (Exception e) {
            System.err.println("[UserApprovalListener] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendEmail(String recipient, String type) throws MessagingException {
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

        MimeMessage msg = new MimeMessage(session);
        msg.setText("Your email content...", "UTF-8");
        msg.setFrom(new InternetAddress("dilansachintha44@gmail.com"));
        msg.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(recipient));

        if ("REGISTERED".equals(type)) {
            msg.setSubject("Banking System - Registration Success");
            msg.setText("Registration Successful! Await Admin Approval.");
        } else if ("APPROVED".equals(type)) {
            msg.setSubject("Banking System - Approval Notification");
            msg.setText("You are now approved! You can login to your account.");
        }

        Transport.send(msg);
        System.out.println("[MAIL SENT] to " + recipient + " regarding " + type);
    }
}
