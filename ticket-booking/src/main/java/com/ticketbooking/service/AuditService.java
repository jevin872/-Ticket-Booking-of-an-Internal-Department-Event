package com.ticketbooking.service;

import com.ticketbooking.model.entity.AuditLog;
import com.ticketbooking.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String action, String entityType, String entityId, String details, String ipAddress, String userAgent) {
        try {
            String userId = null;
            String userEmail = null;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                userEmail = auth.getName();
            }

            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .level(AuditLog.LogLevel.INFO)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            // Never let audit logging break the main flow
        }
    }
}
