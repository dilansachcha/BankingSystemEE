package lk.fortyfourss.ejb.bankingsystemee.listener;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lk.fortyfourss.ejb.bankingsystemee.websocket.AdminNotificationWebSocket;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/NewUserTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic")
})
public class AdminNotificationListener implements MessageListener {

    @Inject
    private AdminNotificationWebSocket webSocket;

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String text = textMessage.getText();
                System.out.println("[JMS RECEIVED] → " + text);

                if (text.startsWith("HIGH_AMOUNT")) {
                    String[] parts = text.split(":");
                    String email = parts[1];
                    String amount = parts[2];
                    System.out.println("[JMS] HIGH_AMOUNT, Email: " + email + ", Amount: " + amount);
                    webSocket.broadcast("HIGH AMOUNT ALERT 💸 " + amount + " from " + email);
                } else if (text.startsWith("REGISTERED")) {
                    String email = text.split(":")[1];
                    System.out.println("[JMS] REGISTERED, Email: " + email);
                    webSocket.broadcast("NEW REGISTRATION:" + email);
                } else {
                    webSocket.broadcast("" + text);
                }
            }
        } catch (Exception e) {
            System.err.println("[JMS Listener Error]: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
