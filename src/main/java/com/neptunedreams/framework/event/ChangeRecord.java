package com.neptunedreams.framework.event;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * We wrap the CurrentRecord in this event class rather than passing it directly because it's a generic type, 
 * so the EventBus registers it as an Object, and it gets called for any event type.
 * @param <R> The type of the record.
 */
public class ChangeRecord<R> {
  private final @NonNull R newRecord;
  ChangeRecord(@NonNull R record) {
    newRecord = record;
  }
  
  @SuppressWarnings("WeakerAccess")
  public @NonNull R getNewRecord() { return newRecord; }
}
