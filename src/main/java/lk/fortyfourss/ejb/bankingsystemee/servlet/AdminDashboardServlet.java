package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;

import java.io.IOException;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    @EJB
    private UserService userService;

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        req.setAttribute("pendingUsers", userService.getPendingUsers());
        req.setAttribute("allUsers", userService.getAllUsers());
        req.getRequestDispatcher("/admin/admin-dashboard.jsp").forward(req, res);
    }
}
