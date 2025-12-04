package com.neptunedreams.framework.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 3:27 PM
 *
 * @author Miguel Muñoz
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class RecordModel<R> implements Serializable {
  private final transient List<RecordModelListener> listenerList = new LinkedList<>();

  // foundItems should be a RandomAccess list
  private List<@NotNull R> foundItems = new ArrayList<>();
  private int recordIndex = 0;
  private final Supplier<@NotNull R> recordConstructor;
  private final @NotNull Function<R, Integer> getIdFunction;

  /**
   * Instantiate a RecordModel
   * @param recordConstructor The constructor that provides a new empty record.
   * @param getIdFunction The ID function to be used to set an ID in the new record.
   */
  public RecordModel(Supplier<@NotNull R> recordConstructor, @NotNull Function<R, Integer> getIdFunction) {
    this.recordConstructor = recordConstructor;
    this.getIdFunction = getIdFunction; 
  }

  public int getRecordIndex() {
    return recordIndex;
  }

  public int getSize() { return foundItems.size(); }

  /**
   * Adds the listener to the list of RecordModelListeners
   * @param listener The listener
   */
  public void addModelListener(RecordModelListener listener) {
    listenerList.add(listener);
  }

  /**
   * Removes the listener from the list of RecordModelListeners.
   * @param listener the listener
   */
  public void removeModelListener(RecordModelListener listener) {
    listenerList.remove(listener);
  }

  public void setNewList(Collection<? extends @NotNull R> records) {
    int priorSelectionId = (foundItems.size() > recordIndex) ? getIdFunction.apply(foundItems.get(recordIndex)) : 0;
    foundItems = new ArrayList<>(records);
    // Not sure if this test is needed, or if we can just always set the record index to zero.
    if (recordIndex >= foundItems.size()) {
      setRecordIndex(0);
    }
    if (foundItems.isEmpty()) {
      final R record;
      record = createNewEmptyRecord();
      foundItems.add(record);
    } else {
      if (priorSelectionId != 0) {
        setRecordById(priorSelectionId); // sets recordIndex to same record, or 0 if not found
      }
    }
    fireModelListChanged();
  }

  /**
   * Construct a new empty record of type R
   * @return a new empty record of type R
   */
  public @NotNull R createNewEmptyRecord() {
    return recordConstructor.get();
  }

  /**
   * Go to the next record in {@code foundItems}, wrapping around to the beginning if necessary
   */
  public void goNext() {
    assert !foundItems.isEmpty();
    int size = foundItems.size();
    int nextRecord = recordIndex + 1;
    if (nextRecord >= size) {
      nextRecord = 0;
    }
    setRecordIndex(nextRecord);
  }

  /**
   * Go to the previous record in {@code foundItems}, wrapping around to the end if necessary
   */
  public void goPrev() {
    assert !foundItems.isEmpty();
    int nextRecord = recordIndex - 1;
    if (nextRecord < 0) {
      nextRecord = foundItems.size() - 1;
    }
    setRecordIndex(nextRecord);
  }

  /**
   * Go to the first record of {@code foundItems}
   */
  public void goFirst() {
    assert !foundItems.isEmpty();
    setRecordIndex(0);
  }

  /**
   * Go to the last record of {@code foundItems}
   */
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

  /**
   * Append the record to the end of {@code foundItems}, which puts it into the database.
   * @param insertedRecord The record to append.
   */
  public void append(@NotNull R insertedRecord) {
    final int newIndex = foundItems.size();
    foundItems.add(insertedRecord);
    setRecordIndex(newIndex);
    fireModelListChanged();
  }

  public @NotNull R getFoundRecord() { // TODO: rename to getCurrentRecord()? 
    if (!foundItems.isEmpty()) {
      return foundItems.get(recordIndex);
    }
    R emptyRecord = createNewEmptyRecord();
    foundItems.add(emptyRecord);
    fireModelListChanged(); // Is it dangerous to fire the listener before returning the record?
    return emptyRecord;
  }

  /**
   * Sets the record index to point to the provided record, if it's in the found set. This is to preserve the current
   * record if it's in the found set. If it's not, leaves the record index unchanged.
   * @param recordId The ID of the record to set
   */
  private void setRecordById(int recordId) {
    int index = 0;
    for (R r : foundItems) {
      if (recordId == getIdFunction.apply(r)) {
        setRecordIndex(index);
        return;
      }
      index++;
    }
  }

  /**
   * Gets the record at the specified index in {@code foundItems}.
   * @param index The index in {@code foundItems}
   * @return The record at the specified index
   */
  public @NotNull R getRecordAt(int index) {
    return foundItems.get(index);
  }

  /**
   * Delete the selected item, conditionally, from the model only. This doesn't delete anything from the database.
   * @param notify Fire appropriate listeners after deleting
   * @param index The index of the record to delete. This method does nothing if index is < 0,
   */
  @SuppressWarnings("BooleanParameter")
  public void deleteSelected(boolean notify, int index) {
    if (index >= 0) {
      foundItems.remove(index);
      if (foundItems.isEmpty()) {
        foundItems.add(createNewEmptyRecord());
      }
      if (recordIndex >= foundItems.size()) {
        recordIndex--; // Should we call setRecordIndex() here?
        assert recordIndex >= 0;
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
