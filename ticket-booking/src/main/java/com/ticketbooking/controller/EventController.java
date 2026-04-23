package com.ticketbooking.controller;

import com.ticketbooking.dto.request.EventRequest;
import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.response.EventResponse;
import com.ticketbooking.model.entity.Event.EventStatus;
import com.ticketbooking.model.entity.User;
import com.ticketbooking.service.EventService;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management APIs")
public class EventController {

    private final EventService eventService;

    // ---- Public endpoints ----

    @GetMapping("/events/public")
    @Operation(summary = "Get all upcoming events (public)")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getPublicEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "eventDate") String sortBy) {
        Page<EventResponse> events = eventService.getEventsByStatus(
                EventStatus.UPCOMING, PageRequest.of(page, size, Sort.by(sortBy)));
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/public/{id}")
    @Operation(summary = "Get event details by ID (public)")
    public ResponseEntity<ApiResponse<EventResponse>> getPublicEvent(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEventById(id)));
    }

    @GetMapping("/events/public/search")
    @Operation(summary = "Search events by keyword")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<EventResponse> events = eventService.searchEvents(keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    // ---- Authenticated endpoints ----

    @GetMapping("/events")
    @Operation(summary = "Get all events (authenticated)")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) EventStatus status) {
        Page<EventResponse> events = status != null
                ? eventService.getEventsByStatus(status, PageRequest.of(page, size, Sort.by("eventDate")))
                : eventService.getAllEvents(PageRequest.of(page, size, Sort.by("eventDate")));
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/events/{id}")
    @Operation(summary = "Get event by ID")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEventById(id)));
    }

    // ---- Organizer / Admin endpoints ----

    @PostMapping("/organizer/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Create a new event")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal User user) {
        EventResponse event = eventService.createEvent(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(event, "Event created successfully"));
    }

    @PutMapping("/organizer/events/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Update an event")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable String id,
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(eventService.updateEvent(id, request, user), "Event updated"));
    }

    @PatchMapping("/organizer/events/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Operation(summary = "Cancel an event")
    public ResponseEntity<ApiResponse<Void>> cancelEvent(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        eventService.cancelEvent(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Event cancelled"));
    }

    @DeleteMapping("/admin/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an event (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Event deleted"));
    }
}
