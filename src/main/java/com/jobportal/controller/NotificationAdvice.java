
package com.jobportal.controller;

import java.security.Principal;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.jobportal.service.NotificationService;

@ControllerAdvice
public class NotificationAdvice {

    private final NotificationService notificationService;

    public NotificationAdvice(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addUnreadNotificationCount(
            Principal principal,
            Model model) {

        if (principal == null) {
            model.addAttribute("globalUnreadCount", 0L);
            return;
        }

        long unreadCount =
                notificationService.getUnreadCount(
                        principal.getName()
                );

        model.addAttribute(
                "globalUnreadCount",
                unreadCount
        );
    }
}
