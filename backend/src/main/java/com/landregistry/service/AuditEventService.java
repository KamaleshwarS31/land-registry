package com.landregistry.service;

import com.landregistry.model.AuditEvent;
import com.landregistry.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditEventService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventService.class);
    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void recordEvent(String actorId, String action, String entityType, String entityId, String detailsJson) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .actorId(actorId != null ? actorId : "SYSTEM")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .detailsJson(detailsJson)
                    .timestamp(Instant.now())
                    .build();
            auditEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to record audit event: {}", e.getMessage());
        }
    }
}
