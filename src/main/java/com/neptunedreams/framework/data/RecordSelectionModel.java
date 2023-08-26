package com.neptunedreams.framework.data;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/2/18
 * <p>Time: 12:33 AM
 *
 * @author Miguel Muñoz
 */
public interface RecordSelectionModel<R> {
  
  boolean isRecordDataModified();
  @NonNull R getCurrentRecord();
}
