package com.ticketbooking.repository;

import com.ticketbooking.model.entity.Booking;
import com.ticketbooking.model.entity.Booking.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByUserId(String userId, Pageable pageable);

    Page<Booking> findByEventId(String eventId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(String userId, BookingStatus status, Pageable pageable);

    List<Booking> findByEventIdAndStatus(String eventId, BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.event.id = :eventId AND b.status != 'CANCELLED'")
    long countActiveBookingsByUserAndEvent(String userId, String eventId);

    @Query("SELECT SUM(b.quantity) FROM Booking b WHERE b.user.id = :userId AND b.event.id = :eventId AND b.status != 'CANCELLED'")
    Integer sumTicketsByUserAndEvent(String userId, String eventId);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'CONFIRMED' AND b.paymentStatus = 'COMPLETED'")
    BigDecimal calculateTotalRevenue();

    long countByStatus(BookingStatus status);

    @Query("SELECT SUM(b.quantity) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Long sumTotalTicketsIssued();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkedIn = true")
    long countCheckedIn();

    boolean existsByBookingReference(String bookingReference);
}
