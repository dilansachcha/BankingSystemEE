package lk.fortyfourss.ejb.bankingsystemee.singleton;

import jakarta.ejb.Singleton;
import java.util.logging.Logger;

@Singleton
public class NotificationPublisherBean {

    private static final Logger LOGGER = Logger.getLogger(NotificationPublisherBean.class.getName());

    public void sendUserRegistered(String email) {
        LOGGER.info("[Notification] USER_REGISTERED: " + email);
    }

    public void sendUserApproved(String email) {
        LOGGER.info("[Notification] USER_APPROVED: " + email);
    }

    public void sendHighAmountTransaction(String email, double amount) {
        LOGGER.info("[Notification] HIGH_AMOUNT: " + email + " | " + amount);
    }
}
