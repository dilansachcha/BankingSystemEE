package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;

import java.io.IOException;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    @EJB
    private UserService userService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String email = req.getParameter("email");
        String code = req.getParameter("code");
        String newPassword = req.getParameter("newPassword");

        if (userService.validateVerificationCode(email, code)) {
            userService.resetPassword(email, newPassword);
            res.getWriter().write("<script>alert('Password Reset Successful!'); window.location='login.jsp';</script>");
        } else {
            res.getWriter().write("<script>alert('Invalid Verification Code!'); window.location='login.jsp?showReset=" + email + "';</script>");
        }
    }
}
