package com.github.jobieskii.public_place.component;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserRateLimiter {
    private final Map<Integer, LocalDateTime> lastAccessOfUser = new HashMap<>();

    public boolean tryAccess(int userId) {
        LocalDateTime now = LocalDateTime.now();
        if (lastAccessOfUser.containsKey(userId)) {
            if (now.isAfter(lastAccessOfUser.get(userId).plusSeconds(4))) {
                lastAccessOfUser.put(userId, now);
                return true;
            }
            return false;
        }
        lastAccessOfUser.put(userId, now);
        return true;
    }

}
