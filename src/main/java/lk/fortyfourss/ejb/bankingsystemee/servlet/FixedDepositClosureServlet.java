package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;

import java.io.IOException;
import java.util.List;

@WebServlet("/close-fixed")
public class FixedDepositClosureServlet extends HttpServlet {

    @EJB
    private AccountService accountService;
    @EJB private TransactionServiceBean transactionService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            res.sendRedirect("login.jsp?error=session");
            return;
        }

        List<Account> fixedAccounts = accountService.getAllAccountsByUserId(user.getId())
                .stream().filter(a -> "FIXED".equalsIgnoreCase(a.getAccountType()) && "ACTIVE".equalsIgnoreCase(a.getStatus())).toList();

        List<Account> targetAccounts = accountService.getNonFixedAccountsByUser(user.getId());

        req.setAttribute("fixedAccounts", fixedAccounts);
        req.setAttribute("targetAccounts", targetAccounts);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            res.sendRedirect("login.jsp?error=session");
            return;
        }

        int fixedId = Integer.parseInt(req.getParameter("fixedId"));
        int targetId = Integer.parseInt(req.getParameter("targetId"));

        Account fixed = accountService.getAccountById(fixedId);
        Account target = accountService.getAccountById(targetId);

        if (fixed == null || !"FIXED".equalsIgnoreCase(fixed.getAccountType()) || fixed.getUserId() != user.getId()) {
            req.setAttribute("message", "Invalid fixed account selected.");
            doGet(req, res);
            return;
        }

        if (target == null || fixed.getUserId() != user.getId() || "FIXED".equalsIgnoreCase(target.getAccountType())) {
            req.setAttribute("message", "Invalid target account.");
            doGet(req, res);
            return;
        }

        double amount = fixed.getBalance();

        transactionService.closeFixedDeposit(fixed.getAccountNumber(), target.getAccountNumber(), amount);
        fixed.setStatus("CLOSED");
        fixed.setBalance(0);
        accountService.updateAccount(fixed);

        session.setAttribute("success", "Fixed deposit closed and funds transferred!");
        res.sendRedirect("dashboard");
    }
}
