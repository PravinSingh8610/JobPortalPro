
package com.jobportal.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jobportal.entity.Notification;
import com.jobportal.service.NotificationService;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    // Display all notifications
    @GetMapping
    public String showNotifications(
            Principal principal,
            Model model) {

        String email = principal.getName();

        List<Notification> notifications =
                notificationService.getUserNotifications(email);

        long unreadCount =
                notificationService.getUnreadCount(email);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "notifications";
    }

    // Mark a single notification as read
    @PostMapping("/{id}/read")
    public String markAsRead(
            @PathVariable Long id,
            Principal principal) {

        notificationService.markAsRead(
                id, principal.getName());

        return "redirect:/notifications";
    }

    // Mark all notifications as read
    @PostMapping("/read-all")
    public String markAllAsRead(Principal principal) {

        notificationService.markAllAsRead(
                principal.getName());

        return "redirect:/notifications";
    }
}
