package com.ticketbooking.repository;

import com.ticketbooking.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.email = :email")
    void incrementFailedAttempts(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.accountNonLocked = true, u.lockTime = null WHERE u.email = :email")
    void resetFailedAttempts(String email);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false, u.lockTime = :lockTime WHERE u.email = :email")
    void lockAccount(String email, LocalDateTime lockTime);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.email = :email")
    void updateLastLogin(String email, LocalDateTime loginTime);

    long countByRole(User.Role role);
}
