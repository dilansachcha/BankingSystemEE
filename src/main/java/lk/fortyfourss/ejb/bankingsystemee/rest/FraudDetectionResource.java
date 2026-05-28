package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import lk.fortyfourss.ejb.bankingsystemee.annotation.Secured;
import lk.fortyfourss.ejb.bankingsystemee.model.Transaction;
import lk.fortyfourss.ejb.bankingsystemee.service.UserTransactionHistoryServiceBean;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import lk.fortyfourss.ejb.bankingsystemee.service.UserService;
import lk.fortyfourss.ejb.bankingsystemee.websocket.AdminNotificationWebSocket;

import javax.net.ssl.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Logger;

@Path("/admin/ai")
@Secured
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FraudDetectionResource {
    private static final Logger LOGGER = Logger.getLogger(FraudDetectionResource.class.getName());

    @EJB private UserTransactionHistoryServiceBean historyService;
    @EJB private UserService userService;

    private HttpClient getTrustingClient() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{ new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
        }}, new SecureRandom());
        return HttpClient.newBuilder().sslContext(sslContext).build();
    }

    private void requireAdmin(ContainerRequestContext requestContext) {
        String role = (String) requestContext.getProperty("role");
        if (!"ADMIN".equalsIgnoreCase(role)) throw new WebApplicationException(403);
    }

    @GET
    @Path("/analyze-risk/{userId}")
    public Response analyzeRisk(@PathParam("userId") int targetUserId, @Context ContainerRequestContext requestContext) {
        requireAdmin(requestContext);
        String apiKey = System.getenv("GEMINI_API_KEY");
        String xaiKey = System.getenv("XAI_API_KEY");

        String prompt = buildPrompt(targetUserId);

        //Gemini
        String[] modelChain = {"gemini-2.5-flash", "gemini-2.0-flash", "gemini-flash-latest"};

        for (String model : modelChain) {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey.trim();
                String payload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "'") + "\"}]}]}";

                HttpClient client = getTrustingClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    try (JsonReader jsonReader = Json.createReader(new StringReader(response.body()))) {
                        JsonObject jsonObject = jsonReader.readObject();
                        String extractedText = jsonObject.getJsonArray("candidates")
                                .getJsonObject(0)
                                .getJsonObject("content")
                                .getJsonArray("parts")
                                .getJsonObject(0)
                                .getString("text");

                        String cleanText = extractedText.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
                        return Response.ok("{\"analysis\":\"" + cleanText + "\"}").build();
                    }
                } else {
                    AdminNotificationWebSocket.broadcast("{\"type\":\"AI_STATUS\", \"message\":\"Model " + model + " failed (" + response.statusCode() + ")... trying fallback.\"}");
                    LOGGER.warning("Model " + model + " failed with status " + response.statusCode());
                }
            } catch (Exception e) {
                LOGGER.warning("Model " + model + " error: " + e.getMessage());
            }
        }

        //xAI
        AdminNotificationWebSocket.broadcast("{\"type\":\"AI_STATUS\", \"message\":\"Gemini unavailable. Trying Grok (xAI)...\"}");
        return callXAI(prompt, xaiKey);
    }

    private String buildPrompt(int userId) {
        List<Transaction> recent = historyService.getTransactionsByUserId(userId);
        StringBuilder sb = new StringBuilder("Analyze for fraud: ");
        for (Transaction t : recent) sb.append(t.getAmount()).append(" ").append(t.getDescription()).append("; ");
        return sb.toString();
    }

    private Response callXAI(String prompt, String apiKey) {
        try {
            if (apiKey == null || apiKey.isEmpty()) return Response.status(502).build();

            String payload = "{\"model\": \"grok-beta\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "'") + "\"}]}";
            HttpClient client = getTrustingClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.x.ai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                try (JsonReader jsonReader = Json.createReader(new StringReader(response.body()))) {
                    JsonObject jsonObject = jsonReader.readObject();
                    String extractedText = jsonObject.getJsonArray("choices")
                            .getJsonObject(0)
                            .getJsonObject("message")
                            .getString("content");

                    String cleanText = extractedText.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
                    return Response.ok("{\"analysis\":\"" + cleanText + "\"}").build();
                }
            } else {
                LOGGER.severe("xAI Critical Failure: " + response.statusCode() + " | Body: " + response.body());
            }
        } catch (Exception e) { LOGGER.severe("xAI Exception: " + e.getMessage()); }
        return Response.status(502).entity("{\"error\":\"All AI Providers Down\"}").build();
    }
}