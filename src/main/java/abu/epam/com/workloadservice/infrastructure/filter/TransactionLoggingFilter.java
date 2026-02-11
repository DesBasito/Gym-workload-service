package abu.epam.com.workloadservice.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRANSACTION_ID = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID, transactionId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // request received
            log.info("[TRANSACTION] START - Method: {}, URI: {}, TransactionID: {}",
                    request.getMethod(), request.getRequestURI(), transactionId);

            logRequestDetails(wrappedRequest);

            // Process request
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            long duration = System.currentTimeMillis() - startTime;

            // Transaction level logging - response sent

            log.info("[TRANSACTION] END - Status: {}, Duration: {}ms, TransactionID: {}",
                    response.getStatus(), duration, transactionId);

            logResponseDetails(wrappedResponse);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[TRANSACTION] ERROR - Status: 500, Duration: {}ms, TransactionID: {}, Error: {}",
                    duration, transactionId, e.getMessage(), e);
            throw e;
        } finally {
            wrappedResponse.copyBodyToResponse();
            MDC.remove(TRANSACTION_ID);
        }
    }

    private void logRequestDetails(ContentCachingRequestWrapper request) {
        String body = getContentAsString(request.getContentAsByteArray());
        if (!body.isEmpty()) {
            log.debug("[OPERATION] Request Body: {}", body);
        }
    }

    private void logResponseDetails(ContentCachingResponseWrapper response) {
        String body = getContentAsString(response.getContentAsByteArray());
        if (!body.isEmpty()) {
            log.debug("[OPERATION] Response Body: {}", body);
        }
    }

    private String getContentAsString(byte[] content) {
        if (content == null || content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }
}
