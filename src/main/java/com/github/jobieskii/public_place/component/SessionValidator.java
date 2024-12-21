package com.github.jobieskii.public_place.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
public class SessionValidator {
    static Logger logger = LoggerFactory.getLogger(SessionValidator.class);

    @Value("${AUTH_ENDPOINT:}")
    private String authEndpoint;

    public SessionValidator() {
    }

    public record UserData(String username, int id) {
    }

    private final Map<String, UserData> sessions = new ConcurrentHashMap<>();

    public UserData checkSession(String sessionId) {
        if (authEndpoint.isEmpty()) {
            return new UserData("anonymous", -1);
        }
        if (sessionId == null) {
            return null;
        }

        if (sessions.containsKey(sessionId)) {
            return sessions.get(sessionId);
        }
        UserData res = askAuthority(sessionId);
        if (res != null) {
            logger.info("Storing session token for user {}", res.id);
            sessions.put(sessionId, res);
        }
        return res;
    }

    UserData askAuthority(String sessionId) {
        URI uri = URI.create(authEndpoint);
        try {
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Cookie", "sessionid=" + sessionId);

            if (conn.getResponseCode() != 200) {
                return null;
            }

            byte[] json = conn.getInputStream().readAllBytes();

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, UserData.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void clearSessions() {
        sessions.clear();
    }
}
