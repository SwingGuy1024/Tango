package com.neptunedreams.framework.data;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/2/18
 * <p>Time: 12:33 AM
 *
 * @author Miguel Muñoz
 */
public interface RecordSelectionModel<R> {
  
  boolean isRecordDataModified();
  @NotNull R getCurrentRecord();
}
