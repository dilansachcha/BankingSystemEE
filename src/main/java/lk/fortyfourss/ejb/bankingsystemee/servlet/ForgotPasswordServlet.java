package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;

import java.io.IOException;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    @EJB
    private UserService userService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String email = req.getParameter("email");
        boolean sent = userService.assignVerificationCode(email);

        if (sent) {
            res.setContentType("text/html");
            res.getWriter().write("<script>alert('Verification Code sent!'); window.location='login.jsp?showReset=" + email + "';</script>");
        } else {
            res.sendRedirect("login.jsp?error=notfound");
        }
    }
}

