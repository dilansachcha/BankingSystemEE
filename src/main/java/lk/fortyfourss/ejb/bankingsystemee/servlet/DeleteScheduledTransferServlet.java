package lk.fortyfourss.ejb.bankingsystemee.servlet;

import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.model.ScheduledTransaction;
import lk.fortyfourss.ejb.bankingsystemee.service.ScheduledTransactionServiceBean;
import java.io.IOException;

@WebServlet("/delete-scheduled-transfer")
public class DeleteScheduledTransferServlet extends HttpServlet {

    @EJB
    private ScheduledTransactionServiceBean scheduledTransactionService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int id = Integer.parseInt(req.getParameter("scheduledId"));
        ScheduledTransaction st = scheduledTransactionService.getById(id);
        if (st != null) {
            scheduledTransactionService.delete(st);
        }
        res.sendRedirect("scheduled-transfers?deleted=true");
    }
}


