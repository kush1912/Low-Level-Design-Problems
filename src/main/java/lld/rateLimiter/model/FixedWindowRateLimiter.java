package lld.rateLimiter.model;

import lld.rateLimiter.interfaces.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter implements RateLimiter {

    int maxRequests;
    long windowSizeMills;
    ConcurrentHashMap<String, Window> clientWindows = new ConcurrentHashMap<>();

    FixedWindowRateLimiter(int maxRequests, long windowSizeMills){
        this.maxRequests = maxRequests;
        this.windowSizeMills = windowSizeMills;
    }

    @Override
    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();

        // Atomically get-or-create this client's window so two threads
        // don't create two different Window objects for the same client.
        Window window = clientWindows.computeIfAbsent(clientId, id -> new Window(now));

        // ConcurrentHashMap makes MAP access thread-safe, but the read-modify-write
        // below on a single Window is a check-then-act sequence. Guard it per-client
        // so two threads can't both read count=99, both pass the check, and both
        // increment (allowing maxRequests + 1). Locking the Window object means
        // different clients don't block each other.
        synchronized (window) {
            // Window expired -> start a fresh window.
            if (now - window.windowStart >= windowSizeMills) {
                window.windowStart = now;
                window.count = 0;
            }


            if (window.count < maxRequests) {
                window.count++;
                return true;
            }
            return false;
        }
    }
}