package com.commercepal.apiservice.shared.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all domain events. Events represent something that happened in the domain.
 */
public interface DomainEvent {

  UUID getEventId();

  LocalDateTime getOccurredAt();

  String getEventType();
}

