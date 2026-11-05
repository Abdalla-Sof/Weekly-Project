
package filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CorrelationLoggingFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(CorrelationLoggingFilter.class);

    private static final String CORRELATION_HEADER =
            "X-Correlation-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId =
                request.getHeader(CORRELATION_HEADER);

        if (correlationId != null && !correlationId.isBlank()) {
            log.info(
                    "LOANS | Correlation ID: {} | Request: {} {}",
                    correlationId,
                    request.getMethod(),
                    request.getRequestURI()
            );
        } else {
            log.info(
                    "LOANS | No Correlation ID | Request: {} {}",
                    request.getMethod(),
                    request.getRequestURI()
            );
        }

        filterChain.doFilter(request, response);
    }
}
