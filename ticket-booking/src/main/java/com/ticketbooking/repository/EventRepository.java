package com.ticketbooking.repository;

import com.ticketbooking.model.entity.Event;
import com.ticketbooking.model.entity.Event.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByDepartmentAndStatus(String department, EventStatus status, Pageable pageable);

    Page<Event> findByCategoryAndStatus(String category, EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = :status AND e.eventDate >= :from AND e.eventDate <= :to")
    Page<Event> findByStatusAndDateRange(EventStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.venue) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND e.status != 'CANCELLED'")
    Page<Event> searchEvents(String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE Event e SET e.availableSeats = e.availableSeats - :count WHERE e.id = :eventId AND e.availableSeats >= :count")
    int decrementAvailableSeats(String eventId, int count);

    @Modifying
    @Query("UPDATE Event e SET e.availableSeats = e.availableSeats + :count WHERE e.id = :eventId")
    void incrementAvailableSeats(String eventId, int count);

    long countByStatus(EventStatus status);

    @Query("SELECT e.category, COUNT(b) FROM Event e JOIN e.bookings b WHERE b.status = 'CONFIRMED' GROUP BY e.category")
    List<Object[]> countBookingsByCategory();

    @Query("SELECT e.department, COUNT(b) FROM Event e JOIN e.bookings b WHERE b.status = 'CONFIRMED' GROUP BY e.department")
    List<Object[]> countBookingsByDepartment();

    @Query("SELECT e FROM Event e JOIN e.bookings b WHERE b.status = 'CONFIRMED' GROUP BY e ORDER BY COUNT(b) DESC")
    List<Event> findTopEventsByBookings(Pageable pageable);
}
