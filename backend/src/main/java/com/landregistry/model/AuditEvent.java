package com.landregistry.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "actor_id", length = 36, nullable = false)
    private String actorId;

    @Column(length = 50, nullable = false)
    private String action;

    @Column(name = "entity_type", length = 50, nullable = false)
    private String entityType;

    @Column(name = "entity_id", length = 36, nullable = false)
    private String entityId;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Column(nullable = false)
    private Instant timestamp;

    public AuditEvent() {}

    public AuditEvent(String id, String actorId, String action, String entityType, String entityId, String detailsJson, Instant timestamp) {
        this.id = id;
        this.actorId = actorId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.detailsJson = detailsJson;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public static class Builder {
        private String id;
        private String actorId;
        private String action;
        private String entityType;
        private String entityId;
        private String detailsJson;
        private Instant timestamp;

        public Builder id(String id) { this.id = id; return this; }
        public Builder actorId(String actorId) { this.actorId = actorId; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder entityId(String entityId) { this.entityId = entityId; return this; }
        public Builder detailsJson(String detailsJson) { this.detailsJson = detailsJson; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public AuditEvent build() {
            return new AuditEvent(id, actorId, action, entityType, entityId, detailsJson, timestamp);
        }
    }
}
