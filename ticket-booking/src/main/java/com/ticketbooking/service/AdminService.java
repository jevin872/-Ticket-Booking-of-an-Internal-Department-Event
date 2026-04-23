package com.ticketbooking.service;

import com.ticketbooking.dto.response.DashboardStatsResponse;
import com.ticketbooking.dto.response.EventResponse;
import com.ticketbooking.dto.response.UserResponse;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.model.entity.Event;
import com.ticketbooking.model.entity.User;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final EventService eventService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalEvents = eventRepository.count();
        long upcomingEvents = eventRepository.countByStatus(Event.EventStatus.UPCOMING);
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(com.ticketbooking.model.entity.Booking.BookingStatus.CONFIRMED);
        long cancelledBookings = bookingRepository.countByStatus(com.ticketbooking.model.entity.Booking.BookingStatus.CANCELLED);
        long totalUsers = userRepository.countByRole(User.Role.USER);
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        Long totalTickets = bookingRepository.sumTotalTicketsIssued();
        long checkedIn = bookingRepository.countCheckedIn();

        // Bookings by category
        Map<String, Long> byCategory = new HashMap<>();
        eventRepository.countBookingsByCategory().forEach(row -> byCategory.put((String) row[0], (Long) row[1]));

        // Bookings by department
        Map<String, Long> byDepartment = new HashMap<>();
        eventRepository.countBookingsByDepartment().forEach(row -> byDepartment.put((String) row[0], (Long) row[1]));

        // Top 5 events
        List<EventResponse> topEvents = eventRepository.findTopEventsByBookings(PageRequest.of(0, 5))
                .stream().map(eventService::mapToEventResponse).collect(Collectors.toList());

        return DashboardStatsResponse.builder()
                .totalEvents(totalEvents)
                .upcomingEvents(upcomingEvents)
                .totalBookings(totalBookings)
                .confirmedBookings(confirmedBookings)
                .cancelledBookings(cancelledBookings)
                .totalUsers(totalUsers)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalTicketsIssued(totalTickets != null ? totalTickets : 0L)
                .checkedInCount(checkedIn)
                .bookingsByCategory(byCategory)
                .bookingsByDepartment(byDepartment)
                .topEvents(topEvents)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(authService::mapToUserResponse);
    }

    @Transactional
    public UserResponse updateUserRole(String userId, User.Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(role);
        return authService.mapToUserResponse(userRepository.save(user));
    }

    @Transactional
    public void toggleUserStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
}
