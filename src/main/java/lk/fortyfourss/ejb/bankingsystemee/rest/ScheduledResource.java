package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.fortyfourss.ejb.bankingsystemee.annotation.Secured;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.model.ScheduledTransaction;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.service.ScheduledTransactionServiceBean;

import java.sql.Timestamp;
import java.util.List;

@Path("/scheduled")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScheduledResource {

    @EJB
    private ScheduledTransactionServiceBean scheduledService;

    @EJB
    private AccountService accountService;

    // DTO
    public static class ScheduleRequest {
        public String fromAcc;
        public String toAcc;
        public double amount;
        public String scheduledTime;
        public boolean recurring;
        public String recurrenceType;
    }

    @GET
    @Secured
    public Response getMySchedules(@Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");
            List<ScheduledTransaction> list = scheduledService.getScheduledTransactionsByUser(userId);

            List<java.util.Map<String, Object>> safeList = new java.util.ArrayList<>();
            for (ScheduledTransaction st : list) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", st.getId());
                map.put("fromAccount", st.getFromAccount());
                map.put("toAccount", st.getToAccount());
                map.put("amount", st.getAmount());
                map.put("scheduledTime", st.getScheduledTime() != null ? st.getScheduledTime().toString() : "");
                map.put("recurring", st.isRecurring());
                map.put("recurrenceType", st.getRecurrenceType());
                map.put("status", st.getStatus());
                map.put("lastExecuted", st.getLastExecuted() != null ? st.getLastExecuted().toString() : "Never");
                safeList.add(map);
            }

            return Response.ok(safeList).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Could not fetch scheduled transactions\"}").build();
        }
    }

    @POST
    @Secured
    public Response createSchedule(ScheduleRequest req, @Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");

            Account from = accountService.getAccountByNumber(req.fromAcc);
            Account to = accountService.getAccountByNumber(req.toAcc);

            if (from == null || to == null || req.fromAcc.equals(req.toAcc) || req.amount <= 0 || req.amount > from.getBalance() ||
                    "FIXED".equalsIgnoreCase(from.getAccountType()) ||
                    "BLOCKED".equalsIgnoreCase(from.getStatus()) ||
                    "FIXED".equalsIgnoreCase(to.getAccountType())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid transfer setup\"}").build();
            }

            if (from.getUser().getId() != userId) {
                return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"You do not own this account\"}").build();
            }

            Timestamp scheduledTime = Timestamp.valueOf(req.scheduledTime.replace("T", " ") + ":00");

            ScheduledTransaction st = new ScheduledTransaction();
            st.setUserId(userId);
            st.setFromAccount(req.fromAcc);
            st.setToAccount(req.toAcc);
            st.setAmount(req.amount);
            st.setScheduledTime(scheduledTime);
            st.setRecurring(req.recurring);
            st.setRecurrenceType(req.recurrenceType);
            st.setStatus("PENDING");
            st.setRetryCount(0);
            st.setNextScheduledTime(scheduledTime);

            scheduledService.persist(st);
            return Response.ok("{\"message\":\"Schedule created successfully!\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Server error creating schedule\"}").build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured
    public Response deleteSchedule(@PathParam("id") int id, @Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");
            ScheduledTransaction st = scheduledService.getById(id);

            if (st == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"error\":\"Schedule not found\"}").build();
            }

            if (st.getUserId() != userId) {
                return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Unauthorized action\"}").build();
            }

            scheduledService.delete(st);
            return Response.ok("{\"message\":\"Schedule cancelled successfully\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Could not cancel schedule\"}").build();
        }
    }
}