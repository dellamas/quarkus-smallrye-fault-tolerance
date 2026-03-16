package dev.dailylab.payments;

import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

@ApplicationScoped
public class PixChargeService {

    private final PixGatewayClient gatewayClient;

    public PixChargeService(PixGatewayClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }

    @Retry(maxRetries = 2, delay = 200, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 400, unit = ChronoUnit.MILLIS)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.75, delay = 2, delayUnit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "reserveForManualReview")
    public PixChargeResult process(PixChargeRequest request) {
        int attempt = gatewayClient.nextAttempt();
        if (request.amount().compareTo(new BigDecimal("1500.00")) > 0) {
            throw new IllegalStateException("provider timeout on high-value pix charge");
        }
        if (attempt < 3) {
            throw new IllegalStateException("temporary provider instability on attempt " + attempt);
        }
        return new PixChargeResult(
                "APPROVED",
                "primary-pix-provider",
                request.amount(),
                "Charge approved after retry strategy stabilized the provider call",
                request.correlationId());
    }

    public PixChargeResult reserveForManualReview(PixChargeRequest request) {
        return new PixChargeResult(
                "MANUAL_REVIEW",
                "fallback-workflow",
                request.amount(),
                "Charge routed to manual review after retry, timeout and circuit breaker protections",
                request.correlationId());
    }

    public void resetAttempts() {
        gatewayClient.reset();
    }
}
