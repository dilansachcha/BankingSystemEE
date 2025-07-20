package lk.fortyfourss.ejb.bankingsystemee.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/admin-notify")
@ApplicationScoped
public class AdminNotificationWebSocket {

    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("[WebSocket] Admin Connected: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("[WebSocket] Admin Disconnected: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        System.err.println("[WebSocket] Error: " + throwable.getMessage());
    }

    public void broadcast(String message) {
        sessions.forEach(session -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.err.println("[WebSocket] Send Error: " + e.getMessage());
            }
        });
    }
}
