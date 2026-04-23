package com.ticketbooking.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardStatsResponse {
    private long totalEvents;
    private long upcomingEvents;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private long totalUsers;
    private BigDecimal totalRevenue;
    private long totalTicketsIssued;
    private long checkedInCount;
    private Map<String, Long> bookingsByCategory;
    private Map<String, Long> bookingsByDepartment;
    private List<EventResponse> topEvents;
}
