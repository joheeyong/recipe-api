package com.luxrobo.recipeapi.controller;

import com.luxrobo.recipeapi.entity.Notification;
import com.luxrobo.recipeapi.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** 내 알림 목록 */
    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        Long userId = (Long) auth.getPrincipal();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(notifications);
    }

    /** 읽지 않은 알림 수 */
    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(Authentication auth) {
        if (auth == null) return ResponseEntity.ok(Map.of("count", 0));
        Long userId = (Long) auth.getPrincipal();
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** 전체 읽음 처리 */
    @PutMapping("/read-all")
    @Transactional
    public ResponseEntity<?> readAll(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        Long userId = (Long) auth.getPrincipal();
        notificationRepository.markAllReadByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 알림 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        Long userId = (Long) auth.getPrincipal();
        return notificationRepository.findById(id)
            .filter(n -> n.getUserId().equals(userId))
            .map(n -> { notificationRepository.delete(n); return ResponseEntity.ok(Map.of("success", true)); })
            .orElse(ResponseEntity.notFound().build());
    }
}
