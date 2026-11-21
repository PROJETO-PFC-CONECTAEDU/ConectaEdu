package com.conectaedu.api.shared.security.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPT = 5;
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAttemptCache = new ConcurrentHashMap<>();
    private static final long EXPIRE_TIME = 3 * 60 * 1000; // 3 minutos em milissegundos

    public void loginSucceeded(final String key) {
        attemptsCache.remove(key);
        lastAttemptCache.remove(key);
    }

    public void loginFailed(final String key) {
        long now = System.currentTimeMillis();
        Integer attempts = attemptsCache.getOrDefault(key, 0);
        
        // Se a última tentativa foi há mais de 3 minutos, reseta
        Long lastAttempt = lastAttemptCache.get(key);
        if (lastAttempt != null && (now - lastAttempt > EXPIRE_TIME)) {
            attempts = 0;
        }

        attempts++;
        attemptsCache.put(key, attempts);
        lastAttemptCache.put(key, now);
    }

    public boolean isBlocked(final String key) {
        long now = System.currentTimeMillis();
        Long lastAttempt = lastAttemptCache.get(key);
        
        if (lastAttempt != null && (now - lastAttempt > EXPIRE_TIME)) {
            attemptsCache.remove(key);
            lastAttemptCache.remove(key);
            return false;
        }

        Integer attempts = attemptsCache.get(key);
        return attempts != null && attempts >= MAX_ATTEMPT;
    }

    public String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
