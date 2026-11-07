
package com.jobportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jobportal.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // Fetch all notifications of a particular user
    List<Notification> findByUserEmailOrderByCreatedAtDesc(
            String email);

    // Count unread notifications of a particular user
    @Query("""
            SELECT COUNT(n)
            FROM Notification n
            WHERE n.user.email = :email
            AND n.isRead = false
            """)
    long countUnreadNotifications(
            @Param("email") String email);

}
