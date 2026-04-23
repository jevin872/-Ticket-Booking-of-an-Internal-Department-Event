package com.ticketbooking.controller;

import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.response.DashboardStatsResponse;
import com.ticketbooking.dto.response.UserResponse;
import com.ticketbooking.model.entity.User;
import com.ticketbooking.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only management APIs")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> users = adminService.getAllUsers(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Update user role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        User.Role role = User.Role.valueOf(body.get("role").toUpperCase());
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUserRole(userId, role), "Role updated"));
    }

    @PatchMapping("/users/{userId}/toggle-status")
    @Operation(summary = "Enable or disable a user account")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable String userId) {
        adminService.toggleUserStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User status updated"));
    }
}
