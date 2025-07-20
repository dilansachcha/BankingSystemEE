package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.UserTransactionHistoryServiceBean;

import java.io.IOException;
import java.util.List;

@WebServlet("/user-transactions-history")
public class UserTransactionHistoryServlet extends HttpServlet {

    @EJB
    private UserTransactionHistoryServiceBean transactionHistoryService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            res.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        List<Transaction> transactions = transactionHistoryService.getAllTransactionsForUser(user);
        req.setAttribute("transactions", transactions);

        req.getRequestDispatcher("user-transactions-history.jsp").forward(req, res);
    }
}
