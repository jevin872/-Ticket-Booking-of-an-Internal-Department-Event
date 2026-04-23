package com.ticketbooking.service;

import com.ticketbooking.dto.request.BookingRequest;
import com.ticketbooking.dto.response.BookingResponse;
import com.ticketbooking.dto.response.TicketResponse;
import com.ticketbooking.exception.BadRequestException;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.model.entity.*;
import com.ticketbooking.model.entity.Booking.BookingStatus;
import com.ticketbooking.model.entity.Booking.PaymentStatus;
import com.ticketbooking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;
    private final QrCodeService qrCodeService;
    private final AuditService auditService;

    @Value("${app.max-tickets-per-user}")
    private int maxTicketsPerUser;

    @Value("${app.ticket-cancellation-hours}")
    private int cancellationHours;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, User user) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        validateBooking(event, request, user);

        TicketType ticketType = null;
        if (request.getTicketTypeId() != null) {
            ticketType = ticketTypeRepository.findById(request.getTicketTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
            validateTicketType(ticketType, request.getQuantity(), user);
        }

        // Atomic seat decrement
        int updated = eventRepository.decrementAvailableSeats(event.getId(), request.getQuantity());
        if (updated == 0) {
            throw new BadRequestException("Not enough seats available");
        }

        if (ticketType != null) {
            int ttUpdated = ticketTypeRepository.decrementAvailableSeats(ticketType.getId(), request.getQuantity());
            if (ttUpdated == 0) {
                eventRepository.incrementAvailableSeats(event.getId(), request.getQuantity());
                throw new BadRequestException("Not enough seats in selected ticket type");
            }
        }

        BigDecimal price = ticketType != null ? ticketType.getPrice() : event.getTicketPrice();
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(request.getQuantity()));

        String bookingRef = generateBookingReference();
        String qrData = generateQrData(bookingRef, user, event);

        Booking booking = Booking.builder()
                .bookingReference(bookingRef)
                .user(user)
                .event(event)
                .ticketType(ticketType)
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .status(BookingStatus.CONFIRMED)
                .paymentStatus(event.isFree() ? PaymentStatus.NOT_REQUIRED : PaymentStatus.PENDING)
                .qrCodeData(qrData)
                .build();

        Booking saved = bookingRepository.save(booking);

        // Generate individual tickets
        List<Ticket> tickets = generateTickets(saved, request.getQuantity());
        saved.setTickets(tickets);

        // Generate QR code image
        try {
            String qrImagePath = qrCodeService.generateQrCode(qrData, bookingRef);
            saved.setQrCodeImagePath(qrImagePath);
            bookingRepository.save(saved);
        } catch (Exception e) {
            log.error("QR code generation failed for booking: {}", bookingRef, e);
        }

        // Send confirmation email async
        emailService.sendBookingConfirmation(user.getEmail(), user.getFullName(), saved);

        auditService.log("BOOKING_CREATED", "BOOKING", saved.getId(),
                "Booking created: " + bookingRef + " for event: " + event.getTitle(), null, null);

        return mapToBookingResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(String id, User user) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new BadRequestException("Access denied");
        }
        return mapToBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + reference));
        return mapToBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(String userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(this::mapToBookingResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getEventBookings(String eventId, Pageable pageable) {
        return bookingRepository.findByEventId(eventId, pageable).map(this::mapToBookingResponse);
    }

    @Transactional
    public BookingResponse cancelBooking(String id, String reason, User user) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new BadRequestException("Access denied");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        LocalDateTime cancellationDeadline = booking.getEvent().getEventDate().minusHours(cancellationHours);
        if (LocalDateTime.now().isAfter(cancellationDeadline) && user.getRole() != User.Role.ADMIN) {
            throw new BadRequestException("Cancellation window has passed. Cancellations must be made " + cancellationHours + " hours before the event.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancelledAt(LocalDateTime.now());

        // Restore seats
        eventRepository.incrementAvailableSeats(booking.getEvent().getId(), booking.getQuantity());
        if (booking.getTicketType() != null) {
            ticketTypeRepository.incrementAvailableSeats(booking.getTicketType().getId(), booking.getQuantity());
        }

        // Cancel individual tickets
        booking.getTickets().forEach(t -> t.setStatus(Ticket.TicketStatus.CANCELLED));

        Booking saved = bookingRepository.save(booking);

        emailService.sendCancellationEmail(user.getEmail(), user.getFullName(), saved);
        auditService.log("BOOKING_CANCELLED", "BOOKING", id, "Booking cancelled: " + booking.getBookingReference(), null, null);

        return mapToBookingResponse(saved);
    }

    @Transactional
    public BookingResponse checkIn(String bookingReference, User admin) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking is not in confirmed status");
        }
        if (booking.isCheckedIn()) {
            throw new BadRequestException("Already checked in at: " + booking.getCheckInTime());
        }

        booking.setCheckedIn(true);
        booking.setCheckInTime(LocalDateTime.now());
        booking.getTickets().forEach(t -> {
            t.setCheckedIn(true);
            t.setCheckInTime(LocalDateTime.now());
            t.setStatus(Ticket.TicketStatus.USED);
        });

        Booking saved = bookingRepository.save(booking);
        auditService.log("CHECK_IN", "BOOKING", booking.getId(),
                "Check-in by: " + admin.getEmail() + " for booking: " + bookingReference, null, null);

        return mapToBookingResponse(saved);
    }

    private void validateBooking(Event event, BookingRequest request, User user) {
        if (event.getStatus() != Event.EventStatus.UPCOMING && event.getStatus() != Event.EventStatus.ONGOING) {
            throw new BadRequestException("Event is not available for booking");
        }
        if (event.getRegistrationDeadline() != null && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new BadRequestException("Registration deadline has passed");
        }
        if (event.getAvailableSeats() < request.getQuantity()) {
            throw new BadRequestException("Only " + event.getAvailableSeats() + " seats available");
        }

        Integer existingTickets = bookingRepository.sumTicketsByUserAndEvent(user.getId(), event.getId());
        int currentTickets = existingTickets != null ? existingTickets : 0;
        if (currentTickets + request.getQuantity() > event.getMaxTicketsPerUser()) {
            throw new BadRequestException("You can book maximum " + event.getMaxTicketsPerUser() + " tickets for this event. You already have " + currentTickets + ".");
        }
    }

    private void validateTicketType(TicketType ticketType, int quantity, User user) {
        if (ticketType.getAvailableSeats() < quantity) {
            throw new BadRequestException("Not enough seats in selected ticket category");
        }
    }

    private List<Ticket> generateTickets(Booking booking, int quantity) {
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Ticket ticket = Ticket.builder()
                    .ticketNumber(generateTicketNumber(booking.getBookingReference(), i + 1))
                    .booking(booking)
                    .status(Ticket.TicketStatus.ACTIVE)
                    .build();
            tickets.add(ticketRepository.save(ticket));
        }
        return tickets;
    }

    private String generateBookingReference() {
        String ref;
        do {
            ref = "TKT" + System.currentTimeMillis() % 1000000 + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        } while (bookingRepository.existsByBookingReference(ref));
        return ref;
    }

    private String generateTicketNumber(String bookingRef, int index) {
        return bookingRef + "-" + String.format("%02d", index);
    }

    private String generateQrData(String bookingRef, User user, Event event) {
        return String.format("REF:%s|USER:%s|EVENT:%s|EMP:%s", bookingRef, user.getId(), event.getId(), user.getEmployeeId());
    }

    public BookingResponse mapToBookingResponse(Booking booking) {
        List<TicketResponse> ticketResponses = booking.getTickets() != null
                ? booking.getTickets().stream().map(this::mapToTicketResponse).collect(Collectors.toList())
                : List.of();

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .eventVenue(booking.getEvent().getVenue())
                .eventDate(booking.getEvent().getEventDate())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getFullName())
                .userEmail(booking.getUser().getEmail())
                .ticketTypeName(booking.getTicketType() != null ? booking.getTicketType().getName() : "General")
                .quantity(booking.getQuantity())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .qrCodeData(booking.getQrCodeData())
                .qrCodeImagePath(booking.getQrCodeImagePath())
                .checkedIn(booking.isCheckedIn())
                .checkInTime(booking.getCheckInTime())
                .bookedAt(booking.getBookedAt())
                .tickets(ticketResponses)
                .build();
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .seatNumber(ticket.getSeatNumber())
                .status(ticket.getStatus())
                .checkedIn(ticket.isCheckedIn())
                .checkInTime(ticket.getCheckInTime())
                .issuedAt(ticket.getIssuedAt())
                .build();
    }
}
