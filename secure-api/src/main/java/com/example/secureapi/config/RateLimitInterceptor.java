package com.example.secureapi.config; // A new package for configuration

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // A map to store a bucket for each unique IP address
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get the IP address of the user making the request
        String ip = request.getRemoteAddr();

        // Get or create a bucket for this IP address
        Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());

        // Try to consume one token from the bucket
        if (bucket.tryConsume(1)) {
            return true; // Request is allowed, continue to the controller
        } else {
            // Limit exceeded, block the request
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return false; // Stop the request from reaching the controller
        }
    }

    private Bucket createNewBucket() {
        // Define the rate limit: 10 requests per minute
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}