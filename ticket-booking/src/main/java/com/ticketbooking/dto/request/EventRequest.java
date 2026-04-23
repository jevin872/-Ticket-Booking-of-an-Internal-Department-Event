package com.ticketbooking.dto.request;

import com.ticketbooking.model.entity.Event.EventStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventRequest {

    @NotBlank(message = "Event title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 5000, message = "Description too long")
    private String description;

    @NotBlank(message = "Venue is required")
    @Size(max = 200)
    private String venue;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;

    private LocalDateTime registrationDeadline;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity cannot exceed 10000")
    private Integer totalCapacity;

    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private BigDecimal ticketPrice = BigDecimal.ZERO;

    @NotBlank(message = "Organizer is required")
    private String organizer;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Category is required")
    private String category;

    private String bannerImageUrl;

    private EventStatus status = EventStatus.UPCOMING;

    private boolean isFree = true;

    @Min(1) @Max(10)
    private int maxTicketsPerUser = 2;

    private List<TicketTypeRequest> ticketTypes;
}
