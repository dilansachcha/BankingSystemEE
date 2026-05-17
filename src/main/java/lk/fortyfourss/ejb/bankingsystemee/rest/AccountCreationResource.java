package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.fortyfourss.ejb.bankingsystemee.annotation.Secured;
import lk.fortyfourss.ejb.bankingsystemee.exception.AccountCreationException;
import lk.fortyfourss.ejb.bankingsystemee.model.AccountType;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.AccountCreationServiceBean;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Path("/account-create")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountCreationResource {

    @EJB
    private AccountCreationServiceBean accountCreationService;

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    public static class AccountCreationRequest {
        public String accountType;
        public double initialDeposit;
        public Integer maturityMonths;
    }

    @POST
    @Secured
    public Response createAccount(AccountCreationRequest request, @Context ContainerRequestContext requestContext) {
        try {
            Integer userId = (Integer) requestContext.getProperty("userId");
            User user = em.find(User.class, userId);

            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"User not found.\"}").build();
            }

            AccountType type;
            try {
                type = AccountType.valueOf(request.accountType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid Account Type.\"}").build();
            }

            accountCreationService.createAccount(user, type, request.initialDeposit, request.maturityMonths);

            return Response.ok("{\"message\":\"" + type.name() + " Account created successfully!\"}").build();

        } catch (AccountCreationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An unexpected error occurred.\"}").build();
        }
    }
}