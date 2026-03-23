package dev.dailylab.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PixChargeServiceTest {

    @Test
    void shouldRejectBlankCorrelationIdBeforeCallingGateway() {
        PixGatewayClient gatewayClient = new PixGatewayClient();
        PixChargeService service = new PixChargeService(gatewayClient);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.process(new PixChargeRequest("cust-01", new BigDecimal("1200.00"), "   ")));

        assertEquals("correlationId must not be blank", exception.getMessage());
    }
}
