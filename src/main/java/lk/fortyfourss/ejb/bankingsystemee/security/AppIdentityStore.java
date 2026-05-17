package lk.fortyfourss.ejb.bankingsystemee.security;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;
import lk.fortyfourss.ejb.bankingsystemee.util.EncryptionUtil;

import java.util.Set;

@ApplicationScoped
public class AppIdentityStore implements IdentityStore {

    @EJB
    private UserService userService;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (credential instanceof UsernamePasswordCredential upc) {
            String email = upc.getCaller();
            String rawPassword = upc.getPasswordAsString();

            User user = userService.findByEmail(email);
            if (user != null && user.getStatus().equalsIgnoreCase("ACTIVE")) {

                // ---> NEW BCRYPT VERIFICATION STRATEGY <---
                if (EncryptionUtil.verifyPassword(rawPassword, user.getPassword())) {
                    return new CredentialValidationResult(user.getEmail(), Set.of(user.getRole()));
                }
            }
        }
        return CredentialValidationResult.INVALID_RESULT;
    }
}
