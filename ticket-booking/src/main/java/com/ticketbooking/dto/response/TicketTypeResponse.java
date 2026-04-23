package com.ticketbooking.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TicketTypeResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int totalSeats;
    private int availableSeats;
    private int maxPerUser;
    private boolean available;
}
