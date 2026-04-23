package com.ticketbooking.service;

import com.ticketbooking.dto.request.EventRequest;
import com.ticketbooking.dto.response.EventResponse;
import com.ticketbooking.dto.response.TicketTypeResponse;
import com.ticketbooking.exception.BadRequestException;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.model.entity.Event;
import com.ticketbooking.model.entity.TicketType;
import com.ticketbooking.model.entity.User;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final BookingRepository bookingRepository;
    private final AuditService auditService;

    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public EventResponse createEvent(EventRequest request, User createdBy) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .venue(request.getVenue())
                .eventDate(request.getEventDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .totalCapacity(request.getTotalCapacity())
                .ticketPrice(request.getTicketPrice())
                .organizer(request.getOrganizer())
                .department(request.getDepartment())
                .category(request.getCategory())
                .bannerImageUrl(request.getBannerImageUrl())
                .status(request.getStatus())
                .isFree(request.isFree())
                .maxTicketsPerUser(request.getMaxTicketsPerUser())
                .createdBy(createdBy)
                .build();

        Event saved = eventRepository.save(event);

        if (request.getTicketTypes() != null && !request.getTicketTypes().isEmpty()) {
            request.getTicketTypes().forEach(ttReq -> {
                TicketType tt = TicketType.builder()
                        .name(ttReq.getName())
                        .description(ttReq.getDescription())
                        .price(ttReq.getPrice())
                        .totalSeats(ttReq.getTotalSeats())
                        .maxPerUser(ttReq.getMaxPerUser())
                        .event(saved)
                        .build();
                ticketTypeRepository.save(tt);
            });
        }

        auditService.log("EVENT_CREATED", "EVENT", saved.getId(), "Event created: " + saved.getTitle(), null, null);
        return mapToEventResponse(saved);
    }

    @Cacheable(value = "events", key = "#id")
    @Transactional(readOnly = true)
    public EventResponse getEventById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return mapToEventResponse(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable).map(this::mapToEventResponse);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByStatus(Event.EventStatus status, Pageable pageable) {
        return eventRepository.findByStatus(status, pageable).map(this::mapToEventResponse);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(String keyword, Pageable pageable) {
        return eventRepository.searchEvents(keyword, pageable).map(this::mapToEventResponse);
    }

    @Transactional
    @CacheEvict(value = "events", key = "#id")
    public EventResponse updateEvent(String id, EventRequest request, User updatedBy) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setVenue(request.getVenue());
        event.setEventDate(request.getEventDate());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setTotalCapacity(request.getTotalCapacity());
        event.setTicketPrice(request.getTicketPrice());
        event.setOrganizer(request.getOrganizer());
        event.setDepartment(request.getDepartment());
        event.setCategory(request.getCategory());
        event.setBannerImageUrl(request.getBannerImageUrl());
        event.setStatus(request.getStatus());
        event.setFree(request.isFree());
        event.setMaxTicketsPerUser(request.getMaxTicketsPerUser());

        Event saved = eventRepository.save(event);
        auditService.log("EVENT_UPDATED", "EVENT", saved.getId(), "Event updated by: " + updatedBy.getEmail(), null, null);
        return mapToEventResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "events", key = "#id")
    public void cancelEvent(String id, User cancelledBy) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (event.getStatus() == Event.EventStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed event");
        }

        event.setStatus(Event.EventStatus.CANCELLED);
        eventRepository.save(event);
        auditService.log("EVENT_CANCELLED", "EVENT", id, "Event cancelled by: " + cancelledBy.getEmail(), null, null);
    }

    @Transactional
    @CacheEvict(value = "events", key = "#id")
    public void deleteEvent(String id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
        auditService.log("EVENT_DELETED", "EVENT", id, "Event deleted", null, null);
    }

    public EventResponse mapToEventResponse(Event event) {
        List<TicketType> ticketTypes = ticketTypeRepository.findByEventId(event.getId());
        long totalBookings = bookingRepository.countByStatus(com.ticketbooking.model.entity.Booking.BookingStatus.CONFIRMED);

        double occupancy = event.getTotalCapacity() > 0
                ? ((double)(event.getTotalCapacity() - event.getAvailableSeats()) / event.getTotalCapacity()) * 100
                : 0;

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .eventDate(event.getEventDate())
                .registrationDeadline(event.getRegistrationDeadline())
                .totalCapacity(event.getTotalCapacity())
                .availableSeats(event.getAvailableSeats())
                .ticketPrice(event.getTicketPrice())
                .organizer(event.getOrganizer())
                .department(event.getDepartment())
                .category(event.getCategory())
                .bannerImageUrl(event.getBannerImageUrl())
                .status(event.getStatus())
                .isFree(event.isFree())
                .maxTicketsPerUser(event.getMaxTicketsPerUser())
                .createdAt(event.getCreatedAt())
                .createdByName(event.getCreatedBy() != null ? event.getCreatedBy().getFullName() : null)
                .ticketTypes(ticketTypes.stream().map(this::mapToTicketTypeResponse).collect(Collectors.toList()))
                .occupancyPercentage(Math.round(occupancy * 100.0) / 100.0)
                .build();
    }

    private TicketTypeResponse mapToTicketTypeResponse(TicketType tt) {
        return TicketTypeResponse.builder()
                .id(tt.getId())
                .name(tt.getName())
                .description(tt.getDescription())
                .price(tt.getPrice())
                .totalSeats(tt.getTotalSeats())
                .availableSeats(tt.getAvailableSeats())
                .maxPerUser(tt.getMaxPerUser())
                .available(tt.getAvailableSeats() > 0)
                .build();
    }
}
