package lld.rateLimiter.interfaces;

public interface RateLimiter {
    boolean allowRequest(String clientId);
}
