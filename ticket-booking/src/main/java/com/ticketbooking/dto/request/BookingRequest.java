package com.ticketbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingRequest {

    @NotBlank(message = "Event ID is required")
    private String eventId;

    private String ticketTypeId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Minimum 1 ticket required")
    @Max(value = 10, message = "Maximum 10 tickets per booking")
    private Integer quantity;
}
