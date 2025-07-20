package lk.fortyfourss.ejb.bankingsystemee.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false) // Important!
public class AccountCreationException extends RuntimeException {
    public AccountCreationException(String message) {
        super(message);
    }
}
