package lk.fortyfourss.ejb.bankingsystemee.singleton;

import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.jms.*;

@Singleton
public class NotificationPublisherBean {

    @Resource(lookup = "jms/NewUserTopic")
    private Topic topic;

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    public void sendUserRegistered(String email) {
        sendMessage("REGISTERED: " + email);
    }

    public void sendUserApproved(String email) {
        sendMessage("APPROVED: " + email);
    }

    private void sendMessage(String messageText) {
        try (JMSContext context = connectionFactory.createContext()) {
            JMSProducer producer = context.createProducer();
            producer.send(topic, messageText);
            System.out.println("Sent JMS Message: " + messageText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendHighAmountTransaction(String email, double amount) {
        sendMessage("HIGH_AMOUNT:" + email + ":" + amount);
    }

}
