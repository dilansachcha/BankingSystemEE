package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.security.MessageDigest;
import java.text.DecimalFormat;
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

            String orderId = accountCreationService.createAccount(user, type, request.initialDeposit, request.maturityMonths);

            String merchantId = System.getenv("PAYHERE_MERCHANT_ID");
            String merchantSecret = System.getenv("PAYHERE_MERCHANT_SECRET");
            String currency = "LKR";

            DecimalFormat df = new DecimalFormat("0.00");
            String formattedAmount = df.format(request.initialDeposit);

            //MD5 Hash
            String hashedSecret = getMd5(merchantSecret).toUpperCase();
            String hashString = merchantId + orderId + formattedAmount + currency + hashedSecret;
            String md5Hash = getMd5(hashString).toUpperCase();

            String jsonResponse = String.format(
                    "{\"message\":\"Pending Account Created\", \"orderId\":\"%s\", \"hash\":\"%s\", \"merchantId\":\"%s\", \"amount\":\"%s\"}",
                    orderId, md5Hash, merchantId, formattedAmount
            );

            return Response.ok(jsonResponse).build();

        } catch (AccountCreationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An unexpected error occurred.\"}").build();
        }
    }

    private String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 conversion failed", e);
        }
    }

    @GET
    @Path("/retry/{accNo}")
    @Secured
    public Response getRetryPayload(@PathParam("accNo") String accNo) {
        try {
            lk.fortyfourss.ejb.bankingsystemee.model.Account acc = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accNo", lk.fortyfourss.ejb.bankingsystemee.model.Account.class)
                    .setParameter("accNo", accNo).getSingleResult();

            String merchantId = System.getenv("PAYHERE_MERCHANT_ID");
            String merchantSecret = System.getenv("PAYHERE_MERCHANT_SECRET");
            DecimalFormat df = new DecimalFormat("0.00");
            String formattedAmount = df.format(acc.getInitialDeposit());

            String hashedSecret = getMd5(merchantSecret).toUpperCase();
            String hashString = merchantId + accNo + formattedAmount + "LKR" + hashedSecret;
            String md5Hash = getMd5(hashString).toUpperCase();

            String jsonResponse = String.format(
                    "{\"orderId\":\"%s\", \"hash\":\"%s\", \"merchantId\":\"%s\", \"amount\":\"%s\", \"accountType\":\"%s\"}",
                    accNo, md5Hash, merchantId, formattedAmount, acc.getAccountType()
            );
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\":\"Cannot generate payment payload\"}").build();
        }
    }
}