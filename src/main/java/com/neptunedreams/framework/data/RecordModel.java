package com.neptunedreams.framework.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 3:27 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("WeakerAccess")
public class RecordModel<R> implements Serializable {
  private final transient List<RecordModelListener> listenerList = new LinkedList<>();
  
  // foundItems should be a RandomAccess list
  private List<R> foundItems = new ArrayList<>();
  private transient int recordIndex = 0;
  private final transient Function<Void, R> constructor;
  
  @SuppressWarnings("JavaDoc")
  public RecordModel(Function<Void, R> theConstructor) {
    constructor = theConstructor;
  }

  public int getRecordIndex() {
    return recordIndex;
  }
  
  public int getSize() { return foundItems.size(); }

  @SuppressWarnings("JavaDoc")
  public void addModelListener(RecordModelListener listener) {
    listenerList.add(listener);
  }
  
  @SuppressWarnings({"unused", "JavaDoc"})
  public void removeModelListener(RecordModelListener listener) {
    listenerList.remove(listener);
  }
 
  public void setNewList(Collection<R> records) {
    foundItems = new ArrayList<>(records);
    if (foundItems.isEmpty()) {
      final R record;
      record = createNewEmptyRecord();
      foundItems.add(record);
    }
    setRecordIndex(0);
    fireModelListChanged();
  }

  @SuppressWarnings("JavaDoc")
  public @NonNull R createNewEmptyRecord() {
    final R emptyRecord = constructor.apply(null);
    assert emptyRecord != null;
    return emptyRecord;
  }

  @SuppressWarnings("JavaDoc")
  public void goNext() {
    assert !foundItems.isEmpty();
    int size = foundItems.size();
    int nextRecord = recordIndex + 1;
    if (nextRecord >= size) {
      nextRecord = 0;
    }
    setRecordIndex(nextRecord);
  }

  @SuppressWarnings("JavaDoc")
  public void goPrev() {
    assert !foundItems.isEmpty();
    int nextRecord = recordIndex - 1;
    if (nextRecord < 0) {
      nextRecord = foundItems.size() - 1;
    }
    setRecordIndex(nextRecord);
  }
  
  @SuppressWarnings("JavaDoc")
  public void goFirst() {
    assert !foundItems.isEmpty();
    setRecordIndex(0);
  }

  @SuppressWarnings("JavaDoc")
  public void goLast() {
    assert !foundItems.isEmpty();
    setRecordIndex(foundItems.size()-1);
  }

  private void setRecordIndex(final int i) {
    if (i != recordIndex) {
      int prior = recordIndex;
      recordIndex = i;
      fireIndexChanged(i, prior);
    }
  }

  private void fireIndexChanged(final int i, int prior) {
    for (RecordModelListener modelListener: listenerList) {
      modelListener.indexChanged(i, prior);
    }
  }

  @SuppressWarnings("JavaDoc")
  public void append(R insertedRecord) {
    final int newIndex = foundItems.size();
    foundItems.add(insertedRecord);
    setRecordIndex(newIndex);
    fireModelListChanged();
  }

  public @NonNull R getFoundRecord() {
    if (!foundItems.isEmpty()) {
      final R foundRecord = foundItems.get(recordIndex);
      assert foundRecord != null;
      return foundRecord;
    }
    R emptyRecord = createNewEmptyRecord();
    foundItems.add(emptyRecord);
    fireModelListChanged(); // Is it dangerous to fire the listener before returning the record?
    return emptyRecord;
  }
  
  @SuppressWarnings("JavaDoc")
  public R getRecordAt(int index) {
    return foundItems.get(index);
  }

  /**
   * Delete the selected item, conditionally, from the model only. This doesn't delete anything from the database.
   * @param notify Fire appropriate listeners after deleting
   * @param index The index of the record to delete. This method does nothing if index is < 0,
   */
  public void deleteSelected(boolean notify, int index) {
    if (index >= 0) {
      foundItems.remove(index);
      if (foundItems.isEmpty()) {
        foundItems.add(createNewEmptyRecord());
      }
      if (recordIndex >= foundItems.size()) {
        recordIndex--; // Should we call setRecordIndex() here?
        assert recordIndex >= 0;
  //      if (recordIndex < 0) {
  //        recordIndex = 0;
  //      }
        if (notify) {
          fireIndexChanged(recordIndex, index);
        }
      }
      if (notify) {
        fireModelListChanged();
      }
    }
  }
  
  private void fireModelListChanged() {
    int size = foundItems.size();
    for (RecordModelListener listener : listenerList) {
      listener.modelListChanged(size);
    }
  }
}