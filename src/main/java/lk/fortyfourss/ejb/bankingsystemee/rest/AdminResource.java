package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import lk.fortyfourss.ejb.bankingsystemee.annotation.Secured;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.AdminAccountServiceBean;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;
import lk.fortyfourss.ejb.bankingsystemee.singleton.NotificationPublisherBean;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/admin")
@Secured
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @EJB private UserService userService;
    @EJB private AdminAccountServiceBean adminAccountService;
    @EJB private TransactionServiceBean transactionService;
    @EJB private NotificationPublisherBean notificationPublisher;

    //Enforce Admin role
    private void requireAdmin(ContainerRequestContext requestContext) {
        String role = (String) requestContext.getProperty("role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Access Denied. Admins only.\"}").build()
            );
        }
    }

    //Dashboard Data
    @GET
    @Path("/dashboard-data")
    public Response getDashboardData(@Context ContainerRequestContext requestContext) {
        requireAdmin(requestContext);

        try {
            Map<String, Object> data = new HashMap<>();

            //Users
            List<User> rawPending = userService.getPendingUsers();
            List<Map<String, Object>> safePending = new java.util.ArrayList<>();
            for(User u : rawPending) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", u.getId());
                map.put("fullName", u.getFullName());
                map.put("email", u.getEmail());
                map.put("nic", u.getNic());
                safePending.add(map);
            }
            data.put("pendingUsers", safePending);

            //Accounts
            List<Account> rawAccounts = adminAccountService.getAllAccounts();
            List<Map<String, Object>> safeAccounts = new java.util.ArrayList<>();
            for(Account a : rawAccounts) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", a.getId());
                map.put("accountNumber", a.getAccountNumber());
                map.put("accountType", a.getAccountType());
                map.put("balance", a.getBalance());
                map.put("status", a.getStatus());

                if(a.getUser() != null) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", a.getUser().getId());
                    userMap.put("email", a.getUser().getEmail());
                    map.put("user", userMap);
                }
                safeAccounts.add(map);
            }
            data.put("allAccounts", safeAccounts);

            return Response.ok(data).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to load dashboard data.\"}")
                    .build();
        }
    }

    //Approve Pending User
    @POST
    @Path("/users/{id}/approve")
    public Response approveUser(@PathParam("id") int userId, @Context ContainerRequestContext requestContext) {
        requireAdmin(requestContext);
        try {
            String email = userService.findById(userId).getEmail();
            userService.approveUser(userId);
            notificationPublisher.sendUserApproved(email);

            //to WebSocket
            lk.fortyfourss.ejb.bankingsystemee.websocket.AdminNotificationWebSocket.broadcast(
                    "{\"type\":\"ALERT\", \"message\":\"New User Approved: " + email + "\"}"
            );

            return Response.ok("{\"message\":\"User approved successfully!\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Failed to approve user.\"}").build();
        }
    }

    //Block / Unblock Accounts
    public static class AccountActionRequest {
        public String action;
        public String reason;
    }

    @POST
    @Path("/accounts/{id}/action")
    public Response handleAccountAction(@PathParam("id") int accountId, AccountActionRequest req, @Context ContainerRequestContext requestContext) {
        requireAdmin(requestContext);
        try {
            if ("block".equalsIgnoreCase(req.action)) {
                adminAccountService.blockAccount(accountId, req.reason != null ? req.reason : "Administrative Action");
                lk.fortyfourss.ejb.bankingsystemee.websocket.AdminNotificationWebSocket.broadcast("{\"type\":\"ALERT\", \"message\":\"Account " + accountId + " was BLOCKED.\"}");
                return Response.ok("{\"message\":\"Account blocked successfully!\"}").build();
            } else if ("unblock".equalsIgnoreCase(req.action)) {
                adminAccountService.unblockAccount(accountId);
                lk.fortyfourss.ejb.bankingsystemee.websocket.AdminNotificationWebSocket.broadcast(
                        "{\"type\":\"ALERT\", \"message\":\"Account " + accountId + " was UNBLOCKED.\"}"
                );
                return Response.ok("{\"message\":\"Account unblocked successfully!\"}").build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid action.\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Action failed.\"}").build();
        }
    }

    //PDF Audit Report
    @GET
    @Path("/audit-report")
    @Produces("application/pdf")
    public Response downloadAuditReport(@Context ContainerRequestContext requestContext) {
        requireAdmin(requestContext);

        List<Transaction> transactions = transactionService.getAllTransactionsWithDetails();

        StreamingOutput stream = output -> {
            try {
                PdfWriter writer = new PdfWriter(output);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                document.add(new Paragraph("FORTRESS Banking - Admin Audit Report").setFontSize(18));
                document.add(new Paragraph("Generated On: " + java.time.LocalDateTime.now()).setFontSize(12));
                document.add(new Paragraph(" "));

                float[] columnWidths = {40, 100, 60, 60, 100, 100};
                Table table = new Table(columnWidths);

                table.addHeaderCell(new Cell().add(new Paragraph("ID")));
                table.addHeaderCell(new Cell().add(new Paragraph("From Account")));
                table.addHeaderCell(new Cell().add(new Paragraph("Amount")));
                table.addHeaderCell(new Cell().add(new Paragraph("Type")));
                table.addHeaderCell(new Cell().add(new Paragraph("User Email")));
                table.addHeaderCell(new Cell().add(new Paragraph("Time")));

                for (Transaction t : transactions) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(t.getId()))));
                    table.addCell(new Cell().add(new Paragraph(t.getAccount().getAccountNumber())));
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", t.getAmount()))));
                    table.addCell(new Cell().add(new Paragraph(t.getTransactionType())));
                    table.addCell(new Cell().add(new Paragraph(t.getAccount().getUser().getEmail())));
                    table.addCell(new Cell().add(new Paragraph(t.getTransactionTime().toString())));
                }

                document.add(table);
                document.close();
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        };

        return Response.ok(stream)
                .header("Content-Disposition", "attachment; filename=\"audit-report.pdf\"")
                .build();
    }
}