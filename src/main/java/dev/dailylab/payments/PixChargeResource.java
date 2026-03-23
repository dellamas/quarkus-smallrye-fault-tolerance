package dev.dailylab.payments;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/pix-charges")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Pix Charges")
public class PixChargeResource {

    private final PixChargeService service;

    public PixChargeResource(PixChargeService service) {
        this.service = service;
    }

    @POST
    @Operation(summary = "Processes a pix charge with retry, timeout, circuit breaker and fallback")
    public PixChargeResult create(@Valid PixChargeRequest request) {
        return service.process(request);
    }
}
