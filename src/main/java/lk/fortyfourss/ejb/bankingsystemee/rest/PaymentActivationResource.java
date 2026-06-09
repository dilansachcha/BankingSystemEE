package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.fortyfourss.ejb.bankingsystemee.annotation.Secured;
import lk.fortyfourss.ejb.bankingsystemee.model.Account;

@Path("/payment")
@Stateless
public class PaymentActivationResource {

    @PersistenceContext(unitName = "bankingPU")
    private EntityManager em;

    @POST
    @Path("/activate/{accNo}")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateAccount(@PathParam("accNo") String accNo) {
        Account account = em.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accNo", Account.class)
                .setParameter("accNo", accNo)
                .getResultStream().findFirst().orElse(null);

        if (account != null && "PENDING".equals(account.getStatus())) {
            account.setStatus("ACTIVE");
            em.merge(account);
            return Response.ok("{\"message\":\"Account successfully activated!\"}").build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Account not found or already active.\"}").build();
    }
}