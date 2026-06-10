package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.singleton.NotificationPublisherBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionServiceBean transactionService;

    @Mock
    private AccountService accountService;

    @Mock
    private NotificationPublisherBean notificationPublisher;

    @Mock
    private EntityManager em;

    //BMT Mock
    @Mock
    private UserTransaction transaction;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        User mockUser = new User();
        mockUser.setEmail("test@fortressbank.com");

        senderAccount = new Account();
        senderAccount.setAccountNumber("ACC-SENDER-123");
        senderAccount.setBalance(5000.0);
        senderAccount.setStatus("ACTIVE");
        senderAccount.setAccountType("SAVINGS");
        senderAccount.setUser(mockUser);

        receiverAccount = new Account();
        receiverAccount.setAccountNumber("ACC-RECEIVER-456");
        receiverAccount.setBalance(1000.0);
        receiverAccount.setStatus("ACTIVE");
        receiverAccount.setAccountType("CHECKING");
    }

    @Test
    void testTransfer_Successful() throws Exception {
        when(accountService.getAccountByNumber("ACC-SENDER-123")).thenReturn(senderAccount);
        when(accountService.getAccountByNumber("ACC-RECEIVER-456")).thenReturn(receiverAccount);

        transactionService.transfer("ACC-SENDER-123", "ACC-RECEIVER-456", 2000.0);

        verify(transaction, times(1)).begin(); // Ensure transaction started
        verify(accountService, times(1)).validateTransferConditions(senderAccount, 2000.0);
        verify(accountService, times(1)).debitFromAccount("ACC-SENDER-123", 2000.0);
        verify(accountService, times(1)).creditToAccount("ACC-RECEIVER-456", 2000.0);

        verify(em, times(2)).persist(any(Transaction.class));
        verify(transaction, times(1)).commit();
    }

    @Test
    void testTransfer_FailsAndRollsBack() throws Exception {
        when(accountService.getAccountByNumber("ACC-SENDER-123")).thenReturn(senderAccount);
        when(accountService.getAccountByNumber("ACC-RECEIVER-456")).thenReturn(receiverAccount);

        doThrow(new RuntimeException("Insufficient funds!"))
                .when(accountService).validateTransferConditions(senderAccount, 10000.0);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> transactionService.transfer("ACC-SENDER-123", "ACC-RECEIVER-456", 10000.0)
        );

        assertTrue(exception.getMessage().contains("Transaction Failed: Insufficient funds!"));

        //rollback proof
        verify(transaction, times(1)).rollback();
        verify(accountService, never()).debitFromAccount(anyString(), anyDouble());
        verify(accountService, never()).creditToAccount(anyString(), anyDouble());
    }
}