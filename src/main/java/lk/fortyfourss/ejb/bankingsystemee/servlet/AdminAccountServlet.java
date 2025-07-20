package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.service.AdminAccountServiceBean;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/accounts")
public class AdminAccountServlet extends HttpServlet {

    @EJB
    private AdminAccountServiceBean adminAccountService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Account> accounts = adminAccountService.getAllAccounts();
        request.setAttribute("accounts", accounts);
        request.getRequestDispatcher("/admin/admin-account-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int accId = Integer.parseInt(request.getParameter("accountId"));
        String action = request.getParameter("action");

        if ("block".equals(action)) {
            String reason = request.getParameter("reason");
            adminAccountService.blockAccount(accId, reason);
        } else if ("unblock".equals(action)) {
            adminAccountService.unblockAccount(accId);
        }

        response.sendRedirect("accounts");
    }
}
