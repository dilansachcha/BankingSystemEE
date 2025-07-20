package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;

import java.io.IOException;
import java.util.List;

@WebServlet("/transfer")
public class TransactionServlet extends HttpServlet {

    @EJB
    private TransactionServiceBean transactionService;

    @EJB
    private AccountService accountService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect("login.jsp?error=session");
            return;
        }

        List<Account> accounts = accountService.getAccountsByUserId(user.getId());
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/transfer.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fromAccNo = request.getParameter("fromAcc");
        String toAccNo = request.getParameter("toAcc");
        double amount = Double.parseDouble(request.getParameter("amount"));

        Account fromAccount = accountService.getAccountByNumber(fromAccNo);
        if (fromAccount == null) {
            response.sendRedirect("transfer?error=Source Account Not Found!");
            return;
        }

        Account toAccount;
        try {
            toAccount = accountService.getAccountByNumber(toAccNo);
        } catch (Exception e) {
            response.sendRedirect("transfer?error=Invalid Destination Account Number!");
            return;
        }

        if (toAccount == null) {
            response.sendRedirect("transfer?error=Destination Account Not Found!");
            return;
        }

        if (fromAccNo.equals(toAccNo)) {
            response.sendRedirect("transfer?error=Cannot transfer to the same account!");
            return;
        }

        if ("FIXED".equalsIgnoreCase(toAccount.getAccountType())) {
            response.sendRedirect("transfer?error=Cannot transfer to FIXED deposit account!");
            return;
        }

        if ("BLOCKED".equalsIgnoreCase(fromAccount.getStatus())) {
            response.sendRedirect("transfer?error=Source Account is BLOCKED!");
            return;
        }

        if ("FIXED".equalsIgnoreCase(fromAccount.getAccountType())) {
            response.sendRedirect("transfer?error=Source Account is FIXED Deposit. Cannot Transfer!");
            return;
        }

        if (amount <= 0) {
            response.sendRedirect("transfer?error=Transfer amount must be greater than zero!");
            return;
        }

        double balance = fromAccount.getBalance();
        if (amount > balance) {
            response.sendRedirect("transfer?error=Insufficient funds in Source Account!");
            return;
        }

        try {
            transactionService.transfer(fromAccNo, toAccNo, amount);
            response.sendRedirect("transfer?success=Transfer Successful!");
        } catch (Exception e) {
            response.sendRedirect("transfer?error=Transfer Failed: " + e.getMessage());
        }
    }



}
