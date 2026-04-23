package com.ticketbooking.dto.response;

import com.ticketbooking.model.entity.Event.EventStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EventResponse {
    private String id;
    private String title;
    private String description;
    private String venue;
    private LocalDateTime eventDate;
    private LocalDateTime registrationDeadline;
    private int totalCapacity;
    private int availableSeats;
    private BigDecimal ticketPrice;
    private String organizer;
    private String department;
    private String category;
    private String bannerImageUrl;
    private EventStatus status;
    private boolean isFree;
    private int maxTicketsPerUser;
    private LocalDateTime createdAt;
    private String createdByName;
    private List<TicketTypeResponse> ticketTypes;
    private long totalBookings;
    private double occupancyPercentage;
}
