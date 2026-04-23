package com.ticketbooking.dto.response;

import com.ticketbooking.model.entity.Booking.BookingStatus;
import com.ticketbooking.model.entity.Booking.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingResponse {
    private String id;
    private String bookingReference;
    private String eventId;
    private String eventTitle;
    private String eventVenue;
    private LocalDateTime eventDate;
    private String userId;
    private String userName;
    private String userEmail;
    private String ticketTypeName;
    private int quantity;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String qrCodeData;
    private String qrCodeImagePath;
    private boolean checkedIn;
    private LocalDateTime checkInTime;
    private LocalDateTime bookedAt;
    private List<TicketResponse> tickets;
}
