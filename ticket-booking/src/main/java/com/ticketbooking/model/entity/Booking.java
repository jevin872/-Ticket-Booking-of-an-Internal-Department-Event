package com.ticketbooking.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_reference", columnList = "bookingReference"),
    @Index(name = "idx_booking_user", columnList = "user_id"),
    @Index(name = "idx_booking_event", columnList = "event_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(columnDefinition = "TEXT")
    private String qrCodeData;

    private String qrCodeImagePath;

    private String paymentTransactionId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String cancellationReason;

    private LocalDateTime cancelledAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    private LocalDateTime updatedAt;

    private boolean checkedIn = false;

    private LocalDateTime checkInTime;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        bookedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, WAITLISTED, EXPIRED
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED, NOT_REQUIRED
    }
}
