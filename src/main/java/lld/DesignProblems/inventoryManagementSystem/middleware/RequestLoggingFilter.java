package lld.DesignProblems.inventoryManagementSystem.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Cross-cutting "middleware" for the Inventory Management API.
 * <p>
 * Servlet {@link jakarta.servlet.Filter}s are Spring's equivalent of the
 * middleware layer found in frameworks like Express/Koa: they run for every
 * request/response, before/after any controller, and are the right place for
 * concerns like logging, auth, or correlation ids that shouldn't leak into
 * the service layer.
 */
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            log.info("{} {} -> {} ({} ms)",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only instrument the inventory API; leave other paths (actuator, etc.) alone.
        return !request.getRequestURI().startsWith("/api/v1/inventory-items");
    }
}
