package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;
import lk.fortyfourss.ejb.bankingsystemee.singleton.NotificationPublisherBean;
import lk.fortyfourss.ejb.bankingsystemee.util.EncryptionUtil;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    @Inject private NotificationPublisherBean notificationPublisher;
    @EJB private UserService userService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.getRequestDispatcher("register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String name = req.getParameter("fullName");
        String email = req.getParameter("email");
        String nic = req.getParameter("nic");
        String mobile = req.getParameter("mobile");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            res.sendRedirect("register.jsp?error=Passwords do not match!");
            return;
        }

        if (userService.emailExists(email)) {
            res.sendRedirect("register.jsp?error=Email already exists!");
            return;
        }
        if (userService.nicExists(nic)) {
            res.sendRedirect("register.jsp?error=NIC already exists!");
            return;
        }

        String encryptedPassword = EncryptionUtil.hashPassword(password);

        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setNic(nic);
        user.setMobile(mobile);
        user.setPassword(encryptedPassword);
        user.setRole("CUSTOMER");
        user.setStatus("INACTIVE");
        user.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        userService.register(user);
        notificationPublisher.sendUserRegistered(user.getEmail());

        res.sendRedirect("login.jsp?success=registered");

    }

}
