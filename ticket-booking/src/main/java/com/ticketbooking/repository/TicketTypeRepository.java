package com.ticketbooking.repository;

import com.ticketbooking.model.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, String> {
    List<TicketType> findByEventId(String eventId);

    @Modifying
    @Query("UPDATE TicketType t SET t.availableSeats = t.availableSeats - :count WHERE t.id = :id AND t.availableSeats >= :count")
    int decrementAvailableSeats(String id, int count);

    @Modifying
    @Query("UPDATE TicketType t SET t.availableSeats = t.availableSeats + :count WHERE t.id = :id")
    void incrementAvailableSeats(String id, int count);
}
