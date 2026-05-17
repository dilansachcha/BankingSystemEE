package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.fortyfourss.ejb.bankingsystemee.annotation.Secured;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.service.UserTransactionHistoryServiceBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
public class HistoryResource {

    @EJB
    private UserTransactionHistoryServiceBean historyService;

    @GET
    @Secured
    public Response getMyHistory(@Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");

            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            // Fetch trans via JWT userId
            List<Transaction> transactions = historyService.getTransactionsByUserId(userId);

            List<Map<String, Object>> safeList = new ArrayList<>();
            for (Transaction t : transactions) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", t.getId());
                map.put("accountNumber", t.getAccount().getAccountNumber());
                map.put("type", t.getTransactionType());
                map.put("amount", t.getAmount());
                map.put("description", t.getDescription());
                map.put("date", t.getTransactionTime().toString());
                safeList.add(map);
            }

            return Response.ok(safeList).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Could not fetch history\"}").build();
        }
    }
}