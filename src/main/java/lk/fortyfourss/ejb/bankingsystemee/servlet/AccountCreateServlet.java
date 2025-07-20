package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.exception.AccountCreationException;
import lk.fortyfourss.ejb.bankingsystemee.model.AccountType;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountCreationServiceBean;

import java.io.IOException;

@WebServlet("/account-create")
public class AccountCreateServlet extends HttpServlet {

    @EJB
    private AccountCreationServiceBean accountCreationService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();

        if (session.getAttribute("user") == null) {
            res.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        if (session.getAttribute("error") != null) {
            req.setAttribute("error", session.getAttribute("error"));
            session.removeAttribute("error");
        }

        if (session.getAttribute("success") != null) {
            req.setAttribute("success", session.getAttribute("success"));
            session.removeAttribute("success");
        }

        req.getRequestDispatcher("create-account.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            res.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        try {
            String typeStr = req.getParameter("accountType");
            double deposit = Double.parseDouble(req.getParameter("initialDeposit"));
            AccountType accountType = AccountType.valueOf(typeStr);

            String maturityParam = req.getParameter("maturityPeriod");
            Integer maturityMonths = null;
            if (accountType == AccountType.FIXED && maturityParam != null && !maturityParam.isBlank()) {
                maturityMonths = Integer.parseInt(maturityParam);
            }

            accountCreationService.createAccount(user, accountType, deposit, maturityMonths);
            session.setAttribute("success", "Account created successfully!");
        } catch (AccountCreationException e) {
            session.setAttribute("error", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("error", "Unexpected error occurred.");
        }

        res.sendRedirect("account-create");
    }
}
