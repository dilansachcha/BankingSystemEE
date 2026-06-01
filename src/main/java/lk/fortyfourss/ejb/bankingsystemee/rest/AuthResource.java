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

        //for local only
        String devKey = "FortressBankingMegaSecretKeyThatIsAtLeast32Bytes!";
        return Keys.hmacShaKeyFor(devKey.getBytes());
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    // DTO OTP
    public static class VerifyRequest {
        public String email;
        public String otp;
    }

    // DTO Reg
    public static class RegisterRequest {
        public String fullName;
        public String email;
        public String password;
        public String nic;
        public String mobile;
    }

    // DTO PW Reset
    public static class ResetRequest {
        public String email;
        public String code;
        public String newPassword;
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

        if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Your account is pending Admin approval.\"}")
                    .build();
        }

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

    @POST
    @Path("/verify-admin")
    public Response verifyAdmin(VerifyRequest request) {

        //demo backdoor
        if ("admin@fortress.com".equals(request.email) && "000000".equals(request.otp)) {
            User admin = userService.findByEmail(request.email);
            String token = generateJwtToken(admin);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("role", admin.getRole());
            responseData.put("fullName", admin.getFullName());
            return Response.ok(responseData).build();
        }

        if (userService.validateAdminVerificationCode(request.email, request.otp)) {
            userService.clearAdminVerificationCode(request.email);
            User admin = userService.findByEmail(request.email);
            String token = generateJwtToken(admin);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("role", admin.getRole());
            responseData.put("fullName", admin.getFullName());

            return Response.ok(responseData).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid or expired OTP code.\"}")
                    .build();
        }
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest req) {
        if (userService.emailExists(req.email)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Email already registered.\"}").build();
        }
        if (userService.nicExists(req.nic)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"NIC already registered.\"}").build();
        }

        User user = new User();
        user.setFullName(req.fullName);
        user.setEmail(req.email);
        user.setNic(req.nic);
        user.setMobile(req.mobile);
        user.setRole("CUSTOMER");
        user.setStatus("INACTIVE");

        user.setPassword(lk.fortyfourss.ejb.bankingsystemee.util.EncryptionUtil.hashPassword(req.password));
        user.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        userService.register(user);

        return Response.ok("{\"message\":\"Registration successful! Please wait for Admin approval.\"}").build();
    }

    @POST
    @Path("/forgot-password")
    public Response forgotPassword(Map<String, String> payload) {
        String email = payload.get("email");
        boolean sent = userService.assignVerificationCode(email);

        if (sent) {
            return Response.ok("{\"message\":\"Verification code sent to your email!\"}").build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Email not found or is an Admin account.\"}").build();
        }
    }

    @POST
    @Path("/reset-password")
    public Response resetPassword(ResetRequest req) {
        if (userService.validateVerificationCode(req.email, req.code)) {
            userService.resetPassword(req.email, req.newPassword);
            return Response.ok("{\"message\":\"Password reset successfully! You can now log in.\"}").build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid or expired verification code.\"}").build();
        }
    }
}