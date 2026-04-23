package com.ticketbooking.repository;

import com.ticketbooking.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    List<Ticket> findByBookingId(String bookingId);
    boolean existsByTicketNumber(String ticketNumber);
}
