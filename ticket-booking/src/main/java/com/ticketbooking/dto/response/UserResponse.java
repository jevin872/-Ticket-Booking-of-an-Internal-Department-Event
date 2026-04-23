package com.ticketbooking.dto.response;

import com.ticketbooking.model.entity.User.Role;
import lombok.*;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private String id;
    private String employeeId;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
