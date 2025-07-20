package lk.fortyfourss.ejb.bankingsystemee.servlet;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/download-receipt")
public class DownloadReceiptServlet extends HttpServlet {

    @EJB
    private TransactionServiceBean transactionService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int transactionId = Integer.parseInt(req.getParameter("transactionId"));
        Transaction t = transactionService.getTransactionById(transactionId);

        if (t == null || !"DEBIT".equals(t.getTransactionType())) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Receipt unavailable.");
            return;
        }

        res.setContentType("application/pdf");
        res.setHeader("Content-Disposition", "attachment; filename=receipt-" + t.getId() + ".pdf");

        try (OutputStream out = res.getOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Transaction Receipt").setBold());
            document.add(new Paragraph("Transaction ID: " + t.getId()));
            document.add(new Paragraph("From Account: " + t.getAccount().getAccountNumber()));
            document.add(new Paragraph("Amount Sent: " + t.getAmount()));
            document.add(new Paragraph("Description: " + t.getDescription()));
            document.add(new Paragraph("Date: " + t.getTransactionTime()));
            document.add(new Paragraph("==========================="));

            document.close();
        }
    }
}
