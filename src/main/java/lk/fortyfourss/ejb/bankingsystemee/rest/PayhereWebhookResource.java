package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;
import java.security.MessageDigest;

@Path("/payhere")
@Stateless
public class PayhereWebhookResource {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    @POST
    @Path("/notify")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response handleNotification(
            @FormParam("merchant_id") String merchantId,
            @FormParam("order_id") String orderId,
            @FormParam("payhere_amount") String payhereAmount,
            @FormParam("payhere_currency") String payhereCurrency,
            @FormParam("status_code") String statusCode,
            @FormParam("md5sig") String md5sig) {

        String merchantSecret = System.getenv("PAYHERE_MERCHANT_SECRET");

        // 1. Verify the Signature to ensure this actually came from PayHere
        String hashedSecret = getMd5(merchantSecret).toUpperCase();
        String hashString = merchantId + orderId + payhereAmount + payhereCurrency + statusCode + hashedSecret;
        String generatedSig = getMd5(hashString).toUpperCase();

        if (!generatedSig.equals(md5sig)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Signature").build();
        }

        // 2. If Payment is Successful (Status Code 2)
        if ("2".equals(statusCode)) {
            Account account = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :orderId", Account.class)
                    .setParameter("orderId", orderId)
                    .getResultStream().findFirst().orElse(null);

            if (account != null && "PENDING".equals(account.getStatus())) {
                account.setStatus("ACTIVE");
                em.merge(account);
            }
        }

        return Response.ok().build();
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
}