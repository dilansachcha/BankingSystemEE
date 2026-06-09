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

// --- NEW IMPORTS FOR SSL BYPASS ---
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

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

    private void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            disableSslVerification();

            String apiKey = System.getenv("STRIPE_SECRET_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Server missing Stripe configuration.\"}").build();
            }

            Stripe.apiKey = apiKey.trim();
            String domainUrl = "https://fortressbank.dedyn.io";

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
                                                    .setUnitAmount((long) (amount * 100))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Account Funding - " + accountType)
                                                                    .build())
                                                    .build())
                                    .build())
                    .build();

            Session session = Session.create(params);
            return Response.ok("{\"checkoutUrl\":\"" + session.getUrl() + "\"}").build();

        } catch (com.stripe.exception.StripeException se) {
            System.out.println("STRIPE ERROR: " + se.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Stripe Error: " + se.getMessage() + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Internal Server Error: " + e.getMessage() + "\"}").build();
        }
    }
}