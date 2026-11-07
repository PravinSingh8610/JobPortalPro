
package com.jobportal.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jobportal.entity.Notification;
import com.jobportal.entity.User;
import com.jobportal.repository.NotificationRepository;
import com.jobportal.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Create a new notification
    @Transactional
    public void createNotification(
            String email,
            String title,
            String message,
            String type,
            String link) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setLink(link);
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    // Get all notifications of a user
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(String email) {

        return notificationRepository
                .findByUserEmailOrderByCreatedAtDesc(email);
    }

    // Get unread notification count
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {

        return notificationRepository
                .countUnreadNotifications(email);
    }

    // Mark one notification as read
    @Transactional
    public void markAsRead(Long notificationId, String email) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    // Mark all notifications as read
    @Transactional
    public void markAllAsRead(String email) {

        List<Notification> notifications =
                notificationRepository
                .findByUserEmailOrderByCreatedAtDesc(email);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);
    }
}
