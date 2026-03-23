package dev.dailylab.payments;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PixChargeResourceTest {

    @Inject
    PixChargeService service;

    @BeforeEach
    void setUp() {
        service.resetAttempts();
    }

    @Test
    void shouldApproveAfterRetries() {
        String payload = """
                {
                  \"customerId\": \"cust-01\",
                  \"amount\": 1200.00,
                  \"correlationId\": \"pix-001\"
                }
                """;

        given()
                .contentType("application/json")
                .body(payload)
                .when().post("/pix-charges")
                .then()
                .statusCode(200)
                .body("status", equalTo("APPROVED"))
                .body("provider", equalTo("primary-pix-provider"))
                .body("correlationId", equalTo("pix-001"));
    }

    @Test
    void shouldFallbackForHighValueCharge() {
        String payload = """
                {
                  \"customerId\": \"cust-02\",
                  \"amount\": 2500.00,
                  \"correlationId\": \"pix-002\"
                }
                """;

        given()
                .contentType("application/json")
                .body(payload)
                .when().post("/pix-charges")
                .then()
                .statusCode(200)
                .body("status", equalTo("MANUAL_REVIEW"))
                .body("provider", equalTo("fallback-workflow"))
                .body("correlationId", equalTo("pix-002"));
    }
    @Test
    void shouldRejectInvalidChargePayload() {
        String payload = """
                {
                  "customerId": "cust-03",
                  "amount": 0,
                  "correlationId": "pix-003"
                }
                """;

        given()
                .contentType("application/json")
                .body(payload)
                .when().post("/pix-charges")
                .then()
                .statusCode(400);
    }

}
