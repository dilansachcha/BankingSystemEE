package lk.fortyfourss.ejb.bankingsystemee.singleton;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class TimerSessionBean {

    private static final Logger LOGGER = Logger.getLogger(TimerSessionBean.class.getName());

    @EJB
    private AccountService accountService;

    //@Schedule(second = "0,30", minute = "*", hour = "*", persistent = false) //30sec
    //@Schedule(hour = "*", minute = "*", persistent = false)
    @Schedule(hour = "0", minute = "0", persistent = false)
    public void autoScheduledBankProcess() {
        LOGGER.info("[Timer Triggered] Scheduled " +
                "Banking Process Running at: " + LocalDateTime.now());
        accountService.updateBalances();
    }

    @Schedule(hour = "0", minute = "30", persistent = false)
    public void updateMaturityStatus() {
        LOGGER.info("[Timer Triggered] Checking Maturity Status at: " + LocalDateTime.now());
        List<Account> fixedAccounts = accountService.getAllFixedActiveAccounts();
        for (Account acc : fixedAccounts) {
            if (acc.getMaturityDate() != null && acc.getMaturityDate().isBefore(LocalDateTime.now())) {
                acc.setMaturityStatus("MATURED");
                accountService.updateAccount(acc);
                LOGGER.info("Maturity Updated for Account: " + acc.getAccountNumber());
            }
        }
    }

}
