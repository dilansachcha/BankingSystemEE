package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.fortyfourss.ejb.bankingsystemee.annotation.Secured;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountService;
import lk.fortyfourss.ejb.bankingsystemee.service.TransactionServiceBean;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    @EJB
    private TransactionServiceBean transactionService;

    @EJB
    private AccountService accountService;

    public static class TransferRequest {
        public String fromAccNo;
        public String toAccNo;
        public double amount;
    }

    @POST
    @Secured
    @Path("/transfer")
    public Response makeTransfer(TransferRequest request, @Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");

            Account sourceAccount = accountService.getAccountByNumber(request.fromAccNo);
            if (sourceAccount.getUser().getId() != userId) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"You do not own the source account.\"}").build();
            }

            transactionService.transfer(request.fromAccNo, request.toAccNo, request.amount);

            return Response.ok("{\"message\":\"Transfer Successful!\"}").build();

        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An unexpected error occurred during transfer.\"}").build();
        }
    }
}