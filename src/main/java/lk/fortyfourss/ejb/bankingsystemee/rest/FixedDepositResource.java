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

@Path("/fixed-deposits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FixedDepositResource {

    @EJB
    private AccountService accountService;

    @EJB
    private TransactionServiceBean transactionService;

    // DTO
    public static class FixedActionRequest {
        public int fixedId;
        public int targetId;
    }

    @POST
    @Path("/withdraw-matured")
    @Secured
    public Response withdrawMatured(FixedActionRequest req, @Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");
            Account fixed = accountService.getAccountById(req.fixedId);
            Account target = accountService.getAccountById(req.targetId);

            if (fixed == null || target == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid accounts provided.\"}").build();
            }
            if (fixed.getUser().getId() != userId || target.getUser().getId() != userId) {
                return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Unauthorized action. You do not own these accounts.\"}").build();
            }
            if (!"FIXED".equalsIgnoreCase(fixed.getAccountType()) || !"MATURED".equalsIgnoreCase(fixed.getMaturityStatus())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Account is not a matured fixed deposit.\"}").build();
            }
            if ("FIXED".equalsIgnoreCase(target.getAccountType())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Cannot transfer funds to another Fixed Deposit.\"}").build();
            }

            transactionService.withdrawMaturedFixedDeposit(fixed, target);

            return Response.ok("{\"message\":\"Matured Fixed Deposit Withdrawn Successfully!\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/close")
    @Secured
    public Response closeFixedDeposit(FixedActionRequest req, @Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");
            Account fixed = accountService.getAccountById(req.fixedId);
            Account target = accountService.getAccountById(req.targetId);

            if (fixed == null || target == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid accounts provided.\"}").build();
            }
            if (fixed.getUser().getId() != userId || target.getUser().getId() != userId) {
                return Response.status(Response.Status.FORBIDDEN).entity("{\"error\":\"Unauthorized action. You do not own these accounts.\"}").build();
            }
            if (!"FIXED".equalsIgnoreCase(fixed.getAccountType())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Account is not a fixed deposit.\"}").build();
            }
            if ("FIXED".equalsIgnoreCase(target.getAccountType())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Cannot transfer funds to another Fixed Deposit.\"}").build();
            }

            double amount = fixed.getBalance();
            transactionService.closeFixedDeposit(fixed.getAccountNumber(), target.getAccountNumber(), amount);

            fixed.setStatus("CLOSED");
            fixed.setBalance(0);
            accountService.updateAccount(fixed);

            return Response.ok("{\"message\":\"Fixed deposit closed and funds transferred!\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}