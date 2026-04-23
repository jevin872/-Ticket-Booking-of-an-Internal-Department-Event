package com.ticketbooking.controller;

import com.ticketbooking.dto.request.BookingRequest;
import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.response.BookingResponse;
import com.ticketbooking.model.entity.User;
import com.ticketbooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Ticket booking management APIs")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal User user) {
        BookingResponse booking = bookingService.createBooking(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(booking, "Booking confirmed! Reference: " + booking.getBookingReference()));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's bookings")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookingResponse> bookings = bookingService.getUserBookings(
                user.getId(), PageRequest.of(page, size, Sort.by("bookedAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(id, user)));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get booking by reference number")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingByReference(reference)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal User user) {
        String reason = body != null ? body.getOrDefault("reason", "Cancelled by user") : "Cancelled by user";
        BookingResponse booking = bookingService.cancelBooking(id, reason, user);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled successfully"));
    }

    // ---- Organizer / Admin ----

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Get all bookings for an event")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getEventBookings(
            @PathVariable String eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BookingResponse> bookings = bookingService.getEventBookings(
                eventId, PageRequest.of(page, size, Sort.by("bookedAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @PostMapping("/check-in/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Check-in attendee by booking reference (QR scan)")
    public ResponseEntity<ApiResponse<BookingResponse>> checkIn(
            @PathVariable String reference,
            @AuthenticationPrincipal User admin) {
        BookingResponse booking = bookingService.checkIn(reference, admin);
        return ResponseEntity.ok(ApiResponse.success(booking, "Check-in successful for: " + booking.getUserName()));
    }
}
