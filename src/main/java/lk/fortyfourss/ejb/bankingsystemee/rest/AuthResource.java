package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @EJB
    private UserService userService;

    public static final Key SECRET_KEY = getSecretKey();

    public static Key getSecretKey() {
        String envKey = System.getenv("JWT_SECRET");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return Keys.hmacShaKeyFor(envKey.getBytes());
        }
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {

        boolean isValid = userService.validate(request.email, request.password);

        if (!isValid) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid credentials\"}")
                    .build();
        }

        User user = userService.findByEmail(request.email);

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            userService.assignAdminVerificationCode(request.email);
            return Response.ok("{\"status\":\"pending_verification\", \"email\":\"" + request.email + "\"}").build();
        }

        String token = generateJwtToken(user);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("role", user.getRole());
        responseData.put("fullName", user.getFullName());

        return Response.ok(responseData).build();
    }

    private String generateJwtToken(User user) {
        long expirationTime = 1000 * 60 * 60; // 1 Hour

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }
}