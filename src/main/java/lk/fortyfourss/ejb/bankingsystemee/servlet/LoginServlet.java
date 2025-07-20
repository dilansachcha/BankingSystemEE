package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Inject private SecurityContext securityContext;
    @EJB private UserService userService;

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        var status = securityContext.authenticate(req, res,
                jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams()
                        .credential(new UsernamePasswordCredential(email, password)));

        if (status == jakarta.security.enterprise.AuthenticationStatus.SUCCESS) {
            User loggedInUser = userService.findByEmail(email);
            req.getSession().setAttribute("user", loggedInUser);

            if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                userService.assignAdminVerificationCode(email);
                res.sendRedirect("admin/verify-admin.jsp?email=" + email);
                return;
            } else if ("CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
                res.sendRedirect("dashboard");
            } else {
                res.sendRedirect("error.jsp?error=unauthorized");
            }
        } else {
            res.sendRedirect("login.jsp?error=invalid");
        }
    }
}

