package dev.dailylab.payments;

import java.math.BigDecimal;

public record PixChargeResult(String status,
                              String provider,
                              BigDecimal approvedAmount,
                              String message,
                              String correlationId) {
}
