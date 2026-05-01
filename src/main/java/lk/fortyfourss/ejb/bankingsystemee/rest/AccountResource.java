package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.fortyfourss.ejb.bankingsystemee.annotation.Secured;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    @EJB
    private AccountService accountService;

    @GET
    @Secured
    @Path("/my-accounts")
    public Response getMyAccounts(@Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");

            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            // Fetch ALL accounts
            List<Account> accounts = accountService.getAllAccountsByUserId(userId);

            List<Map<String, Object>> safeAccountsList = new ArrayList<>();
            for(Account acc : accounts) {
                Map<String, Object> safeAccount = new HashMap<>();
                safeAccount.put("id", acc.getId());
                safeAccount.put("accountNumber", acc.getAccountNumber());
                safeAccount.put("accountType", acc.getAccountType());
                safeAccount.put("balance", acc.getBalance());
                safeAccount.put("status", acc.getStatus());

                safeAccount.put("maturityStatus", acc.getMaturityStatus());

                safeAccountsList.add(safeAccount);
            }

            return Response.ok(safeAccountsList).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Could not fetch accounts\"}").build();
        }
    }
}