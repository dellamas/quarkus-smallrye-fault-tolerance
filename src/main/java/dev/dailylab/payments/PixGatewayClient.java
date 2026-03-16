package dev.dailylab.payments;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class PixGatewayClient {

    private final AtomicInteger attempts = new AtomicInteger();

    public int nextAttempt() {
        return attempts.incrementAndGet();
    }

    public void reset() {
        attempts.set(0);
    }
}
