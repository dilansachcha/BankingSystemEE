package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;
import lk.fortyfourss.ejb.bankingsystemee.singleton.NotificationPublisherBean;

import java.io.IOException;

@WebServlet("/admin/action")
public class AdminActionServlet extends HttpServlet {
    @Inject private NotificationPublisherBean notificationPublisher;
    @EJB private UserService userService;

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = Integer.parseInt(req.getParameter("userId"));
        String action = req.getParameter("action");

        if ("activate".equals(action)) {
            String email = userService.findById(userId).getEmail();
            userService.approveUser(userId);
            notificationPublisher.sendUserApproved(email);
        }
        res.sendRedirect("dashboard");
    }

}



