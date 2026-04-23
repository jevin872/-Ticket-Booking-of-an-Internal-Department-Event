package com.ticketbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketTypeRequest {

    @NotBlank(message = "Ticket type name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @DecimalMin("0.0")
    private BigDecimal price = BigDecimal.ZERO;

    @Min(1)
    private int totalSeats;

    @Min(1) @Max(10)
    private int maxPerUser = 2;
}
