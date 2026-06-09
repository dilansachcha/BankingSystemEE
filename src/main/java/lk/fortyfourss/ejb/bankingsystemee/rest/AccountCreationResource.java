package lk.fortyfourss.ejb.bankingsystemee.rest;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
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

            AccountType type = AccountType.valueOf(request.accountType.toUpperCase());
            String accNo = accountCreationService.createAccount(user, type, request.initialDeposit, request.maturityMonths);

            return generateStripeSession(accNo, request.initialDeposit, type.name());

        } catch (AccountCreationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An unexpected error occurred.\"}").build();
        }
    }

    @GET
    @Path("/retry/{accNo}")
    @Secured
    public Response getRetryPayload(@PathParam("accNo") String accNo) {
        try {
            lk.fortyfourss.ejb.bankingsystemee.model.Account acc = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accNo", lk.fortyfourss.ejb.bankingsystemee.model.Account.class)
                    .setParameter("accNo", accNo).getSingleResult();

            return generateStripeSession(accNo, acc.getInitialDeposit(), acc.getAccountType());
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Cannot generate payment payload: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    private Response generateStripeSession(String accNo, double amount, String accountType) {
        try {
            Stripe.apiKey = System.getenv("STRIPE_SECRET_KEY").trim();
            String domainUrl = "https://fortressbank.dedyn.io"; // Your secure domain

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(domainUrl + "/dashboard?payment_success=true&accNo=" + accNo)
                    .setCancelUrl(domainUrl + "/dashboard?payment_canceled=true")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("lkr")
                                                    .setUnitAmount((long) (amount * 100)) // Stripe expects amounts in cents
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Account Funding - " + accountType)
                                                                    .build())
                                                    .build())
                                    .build())
                    .build();

            Session session = Session.create(params);

            //Stripe URL -> Angular
            return Response.ok("{\"checkoutUrl\":\"" + session.getUrl() + "\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Stripe session creation failed.\"}").build();
        }
    }
}