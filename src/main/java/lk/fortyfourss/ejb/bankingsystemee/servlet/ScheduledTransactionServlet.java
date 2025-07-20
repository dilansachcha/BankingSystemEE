//package lk.fortyfourss.ejb.bankingsystemee.servlet;
//
//import jakarta.ejb.EJB;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lk.fortyfourss.ejb.bankingsystemee.model.ScheduledTransaction;
//import lk.fortyfourss.ejb.bankingsystemee.model.User;
//import lk.fortyfourss.ejb.bankingsystemee.model.Account;
//import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
//import lk.fortyfourss.ejb.bankingsystemee.service.ScheduledTransactionServiceBean;
//import lk.fortyfourss.ejb.bankingsystemee.singleton.ScheduledTransactionTimerBean;
//
//import java.io.IOException;
//import java.sql.Timestamp;
//import java.util.List;
//
//@WebServlet("/schedule-transaction")
//public class ScheduledTransactionServlet extends HttpServlet {
//
//    @EJB
//    private ScheduledTransactionServiceBean scheduledTransactionService;
//
//    @EJB
//    private ScheduledTransactionTimerBean timerBean;
//
//    @EJB
//    private AccountService accountService;
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
//        User user = (User) req.getSession().getAttribute("user");
//        if (user == null) {
//            res.sendRedirect("login.jsp?error=unauthorized");
//            return;
//        }
//
//        List<Account> accounts = accountService.getAccountsByUserId(user.getId());
//        req.setAttribute("accounts", accounts);
//
//        req.getRequestDispatcher("schedule-transfer.jsp").forward(req, res);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
//    User user = (User) req.getSession().getAttribute("user");
//        if (user == null) {
//            res.sendRedirect("login.jsp?error=unauthorized");
//            return;
//        }
//
//        try {
//            String fromAcc = req.getParameter("fromAcc");
//            String toAcc = req.getParameter("toAcc");
//            double amount = Double.parseDouble(req.getParameter("amount"));
//            String rawTime = req.getParameter("scheduledTime");
//            String parsedTime = rawTime.replace("T", " ") + ":00";
//            Timestamp scheduledTime = Timestamp.valueOf(parsedTime);
//            boolean recurring = req.getParameter("recurring") != null;
//            String recurrenceType = req.getParameter("recurrenceType");
//
//            if (fromAcc.equals(toAcc)) {
//                res.sendRedirect("schedule-transaction?error=Cannot transfer to the same account!");
//                return;
//            }
//
//            Account fromAccountObj = accountService.getAccountByNumber(fromAcc);
//            if (fromAccountObj == null) {
//                res.sendRedirect("schedule-transaction?error=Invalid Source Account!");
//                return;
//            }
//
//            if (amount <= 0) {
//                res.sendRedirect("schedule-transaction?error=Transfer amount must be greater than zero!");
//                return;
//            }
//
//            double balance = fromAccountObj.getBalance();
//            if (amount > balance) {
//                res.sendRedirect("schedule-transaction?error=Cannot schedule transfer more than current balance!");
//                return;
//            }
//
//            if ("BLOCKED".equalsIgnoreCase(fromAccountObj.getStatus()) ||
//                    "FIXED".equalsIgnoreCase(fromAccountObj.getAccountType())) {
//                res.sendRedirect("schedule-transaction?error=Source account cannot be used for scheduled transfers (Blocked or Fixed Deposit).");
//                return;
//            }
//
//            Account toAccountObj;
//            try {
//                toAccountObj = accountService.getAccountByNumber(toAcc);
//            } catch (Exception e) {
//                res.sendRedirect("schedule-transaction?error=Invalid Destination Account Number!");
//                return;
//            }
//
//            if (toAccountObj == null) {
//                res.sendRedirect("schedule-transaction?error=Invalid Destination Account Number!");
//                return;
//            }
//
//            if ("FIXED".equalsIgnoreCase(toAccountObj.getAccountType())) {
//                res.sendRedirect("schedule-transaction?error=Destination FIXED DEPOSIT account cannot receive scheduled transfers.");
//                return;
//            }
//
//            ScheduledTransaction st = new ScheduledTransaction();
//            st.setUserId(user.getId());
//            st.setFromAccount(fromAcc);
//            st.setToAccount(toAcc);
//            st.setAmount(amount);
//            st.setScheduledTime(scheduledTime);
//            st.setRecurring(recurring);
//            st.setRecurrenceType(recurrenceType);
//
//            scheduledTransactionService.persist(st);
//            System.out.println("Persisted ScheduledTransaction ID: " + st.getId());
//
//            //timerBean.createTimer(st);
//            st.setStatus("PENDING");
//            st.setRetryCount(0);
//            st.setNextScheduledTime(st.getScheduledTime());
//            System.out.println("Timer created for transaction ID: " + st.getId());
//
//            res.sendRedirect("scheduled-transfers?success=true");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.sendRedirect("schedule-transaction?error=Something went wrong: " + e.getMessage());
//        }
//
//    }
//
//    private void reloadForm(HttpServletRequest req, HttpServletResponse res, User user) throws ServletException, IOException {
//        List<Account> accounts = accountService.getAccountsByUserId(user.getId());
//        req.setAttribute("accounts", accounts);
//        req.getRequestDispatcher("schedule-transfer.jsp").forward(req, res);
//    }
//
//}
package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import lk.fortyfourss.ejb.bankingsystemee.model.ScheduledTransaction;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.service.ScheduledTransactionServiceBean;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/schedule-transaction")
public class ScheduledTransactionServlet extends HttpServlet {

    @EJB private ScheduledTransactionServiceBean scheduledTransactionService;
    @EJB private AccountService accountService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            res.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        List<Account> accounts = accountService.getAccountsByUserId(user.getId());
        req.setAttribute("accounts", accounts);
        req.getRequestDispatcher("schedule-transfer.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            res.sendRedirect("login.jsp?error=unauthorized");
            return;
        }

        try {
            String fromAcc = req.getParameter("fromAcc");
            String toAcc = req.getParameter("toAcc");
            double amount = Double.parseDouble(req.getParameter("amount"));
            Timestamp scheduledTime = Timestamp.valueOf(req.getParameter("scheduledTime").replace("T", " ") + ":00");
            boolean recurring = req.getParameter("recurring") != null;
            String recurrenceType = req.getParameter("recurrenceType");

            Account from = accountService.getAccountByNumber(fromAcc);
            Account to = accountService.getAccountByNumber(toAcc);

            if (from == null || to == null || fromAcc.equals(toAcc) || amount <= 0 || amount > from.getBalance() ||
                    "FIXED".equalsIgnoreCase(from.getAccountType()) ||
                    "BLOCKED".equalsIgnoreCase(from.getStatus()) ||
                    "FIXED".equalsIgnoreCase(to.getAccountType())) {
                res.sendRedirect("schedule-transaction?error=Invalid transfer setup");
                return;
            }

            ScheduledTransaction st = new ScheduledTransaction();
            st.setUserId(user.getId());
            st.setFromAccount(fromAcc);
            st.setToAccount(toAcc);
            st.setAmount(amount);
            st.setScheduledTime(scheduledTime);
            st.setRecurring(recurring);
            st.setRecurrenceType(recurrenceType);
            st.setStatus("PENDING");
            st.setRetryCount(0);
            st.setNextScheduledTime(scheduledTime);
            scheduledTransactionService.persist(st);

            res.sendRedirect("scheduled-transfers?success=true");

        } catch (Exception e) {
            e.printStackTrace();
            res.sendRedirect("schedule-transaction?error=Something went wrong");
        }
    }
}

