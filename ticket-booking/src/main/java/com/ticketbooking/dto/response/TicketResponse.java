package com.ticketbooking.dto.response;

import com.ticketbooking.model.entity.Ticket.TicketStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TicketResponse {
    private String id;
    private String ticketNumber;
    private String seatNumber;
    private TicketStatus status;
    private boolean checkedIn;
    private LocalDateTime checkInTime;
    private LocalDateTime issuedAt;
}
