package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.fortyfourss.ejb.bankingsystemee.model.ScheduledTransaction;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.ScheduledTransactionServiceBean;

import java.io.IOException;
import java.util.List;

@WebServlet("/scheduled-transfers")
public class ScheduledTransfersServlet extends HttpServlet {

    @EJB
    private ScheduledTransactionServiceBean scheduledTransactionService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            res.sendRedirect("login.jsp?error=unauthorized");
            return;
        }
        List<ScheduledTransaction> scheduledList = scheduledTransactionService.getScheduledTransactionsByUser(user.getId());
        req.setAttribute("scheduledList", scheduledList);
        req.getRequestDispatcher("scheduled-transfers.jsp").forward(req, res);
    }
}
