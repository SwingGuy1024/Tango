package com.neptunedreams.framework.event;

import org.jetbrains.annotations.NotNull;

/**
 * We wrap the CurrentRecord in this event class rather than passing it directly because it's a generic type, 
 * so the EventBus registers it as an Object, and it gets called for any event type.
 * @param <R> The type of the record.
 */
public class ChangeRecord<R> {
  private final @NotNull R newRecord;
  ChangeRecord(@NotNull R record) {
    newRecord = record;
  }
  
  @SuppressWarnings("WeakerAccess")
  public @NotNull R getNewRecord() { return newRecord; }
}
