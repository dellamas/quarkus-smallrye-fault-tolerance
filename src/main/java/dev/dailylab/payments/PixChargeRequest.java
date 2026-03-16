package dev.dailylab.payments;

import java.math.BigDecimal;

public record PixChargeRequest(String customerId, BigDecimal amount, String correlationId) {
}
