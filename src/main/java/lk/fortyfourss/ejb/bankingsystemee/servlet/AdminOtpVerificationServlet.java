package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;

import java.io.IOException;

@WebServlet("/admin/verify-admin")
public class AdminOtpVerificationServlet extends HttpServlet {
    @EJB
    private UserService userService;

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String email = req.getParameter("email");
        String code = req.getParameter("otp");

        if (userService.validateAdminVerificationCode(email, code)) {
            userService.clearAdminVerificationCode(email);
            User admin = userService.findByEmail(email);
            req.getSession().setAttribute("user", admin);
            res.sendRedirect(req.getContextPath() + "/admin/dashboard");
        } else {
            res.sendRedirect(req.getContextPath() + "/admin/verify-admin.jsp?email=" + email + "&error=invalid");
        }
    }
}

