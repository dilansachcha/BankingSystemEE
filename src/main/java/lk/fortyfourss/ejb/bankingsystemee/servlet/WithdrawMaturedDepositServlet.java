package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;

import java.io.IOException;

@WebServlet("/withdraw-matured")
public class WithdrawMaturedDepositServlet extends HttpServlet {

    @EJB private AccountService accountService;
    @EJB private TransactionServiceBean transactionService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            res.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        int fixedId = Integer.parseInt(req.getParameter("fixedId"));
        int targetId = Integer.parseInt(req.getParameter("targetId"));

        Account fixed = accountService.getAccountById(fixedId);
        Account target = accountService.getAccountById(targetId);

        if (fixed == null || !"FIXED".equalsIgnoreCase(fixed.getAccountType()) ||
                !"MATURED".equalsIgnoreCase(fixed.getMaturityStatus())) {
            session.setAttribute("error", "Invalid or non-matured fixed account.");
            res.sendRedirect("dashboard");
            return;
        }

        if (target == null || "FIXED".equalsIgnoreCase(target.getAccountType())) {
            session.setAttribute("error", "Invalid target account.");
            res.sendRedirect("dashboard");
            return;
        }

        transactionService.withdrawMaturedFixedDeposit(fixed, target);
        session.setAttribute("success", "Matured Fixed Deposit Withdrawn Successfully!");
        res.sendRedirect("dashboard");
    }
}
