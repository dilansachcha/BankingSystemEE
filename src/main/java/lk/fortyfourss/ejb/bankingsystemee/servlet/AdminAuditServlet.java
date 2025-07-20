package lk.fortyfourss.ejb.bankingsystemee.servlet;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/download-audit-report")
public class AdminAuditServlet extends HttpServlet {

    @EJB
    private TransactionServiceBean transactionService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/pdf");
        res.setHeader("Content-Disposition", "attachment; filename=\"audit-report.pdf\"");

        try {
            List<Transaction> transactions = transactionService.getAllTransactionsWithDetails();

            PdfWriter writer = new PdfWriter(res.getOutputStream());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Banking System - Admin Audit Report").setFontSize(18));
            document.add(new Paragraph("Generated On: " + java.time.LocalDateTime.now()).setFontSize(12));
            document.add(new Paragraph(" "));

            float[] columnWidths = {40, 100, 60, 60, 100, 100};
            Table table = new Table(columnWidths);

            table.addHeaderCell(new Cell().add(new Paragraph("Txn ID")));
            table.addHeaderCell(new Cell().add(new Paragraph("From Account")));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount")));
            table.addHeaderCell(new Cell().add(new Paragraph("Txn Type")));
            table.addHeaderCell(new Cell().add(new Paragraph("User Email")));
            table.addHeaderCell(new Cell().add(new Paragraph("Time")));

            for (Transaction t : transactions) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(t.getId()))));
                table.addCell(new Cell().add(new Paragraph(t.getAccount().getAccountNumber() + " (" + t.getAccount().getAccountType() + ")")));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", t.getAmount()))));
                table.addCell(new Cell().add(new Paragraph(t.getTransactionType())));
                table.addCell(new Cell().add(new Paragraph(t.getAccount().getUser().getEmail())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(t.getTransactionTime()))));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            res.sendRedirect("admin-dashboard.jsp?error=pdf_failed");
        }
    }
}
