package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;

import java.io.IOException;
import java.util.List;

@WebServlet("/dashboard")
public class IndexServlet extends HttpServlet {

    @EJB private AccountService accountService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null || !"CUSTOMER".equalsIgnoreCase(user.getRole())) {
            res.sendRedirect(req.getContextPath() + "/login.jsp?error=session");
            return;
        }

        List<Account> accounts = accountService.getAllAccountsByUserId(user.getId());
        List<Account> nonFixedAccounts = accountService.getNonFixedAccountsByUser(user.getId());
        req.setAttribute("accounts", accounts);
        req.setAttribute("nonFixedAccounts", nonFixedAccounts);
        req.getRequestDispatcher("/index.jsp").forward(req, res);

    }
}
