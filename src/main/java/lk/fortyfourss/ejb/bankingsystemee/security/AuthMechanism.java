package lk.fortyfourss.ejb.bankingsystemee.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@AutoApplySession
@ApplicationScoped
public class AuthMechanism implements HttpAuthenticationMechanism {

    @Inject private IdentityStore identityStore;

    @Override
    public jakarta.security.enterprise.AuthenticationStatus validateRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpMessageContext context) throws AuthenticationException {

        if (context.getAuthParameters().getCredential() != null) {
            CredentialValidationResult result = identityStore.validate(context.getAuthParameters().getCredential());
            if (result.getStatus() == CredentialValidationResult.Status.VALID) {
                return context.notifyContainerAboutLogin(result);
            }
            return jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;
        }
        if (context.isProtected() && context.getCallerPrincipal() == null) {
            try { response.sendRedirect(request.getContextPath() + "/login.jsp?error=session"); }
            catch (Exception e) { throw new RuntimeException("Redirect failed", e); }
        }
        return context.doNothing();
    }
}
