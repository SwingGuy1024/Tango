package com.neptunedreams.framework.data;

import java.sql.SQLException;
import java.util.Collection;
//import com.neptunedreams.jobs.data.LeadField;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/28/17
 * <p>Time: 6:52 PM
 *
 * @author Miguel Muñoz
 */
public interface Dao<E, PK, F extends DBField> {
  boolean createTableIfNeeded() throws SQLException;
  
  Collection<@NonNull E> getAll(@Nullable F orderBy) throws SQLException;
  
  Collection<@NonNull E> find(String text, @Nullable F orderBy) throws SQLException;
  Collection<@NonNull E> findAny(@Nullable F orderBy, String... text) throws SQLException;
  Collection<@NonNull E> findAll(@Nullable F orderBy, String... text) throws SQLException;

  Collection<@NonNull E> findInField(String text, @NonNull F findBy, @Nullable F orderBy) throws SQLException;
  Collection<@NonNull E> findAnyInField(@NonNull F findBy, @Nullable F orderBy, String... text) throws SQLException;
  Collection<@NonNull E> findAllInField(@NonNull F findBy, @Nullable F orderBy, String... text) throws SQLException;

//  E newEmptyRecord();

  /**
   * insert or update the entity.
   * @param entity The entity
//   * @return True if the entity was new and was inserted, false if it updated an existing entry.
   * @throws SQLException Yeah, you know.
   */
  void update(@NonNull E entity) throws SQLException;

  void insert(@NonNull E entity) throws SQLException;

  /**
   * insert or update the provided entity. If the id is 0 or null, it inserts the record. Otherwise it updates it.
   * @param entity The entity to save
   * @throws SQLException Sql exception
   */
  void insertOrUpdate(@NonNull E entity) throws SQLException;

  void delete(@NonNull E entity) throws SQLException;
  
  PK getNextId() throws SQLException;
  
  PK getPrimaryKey(@NonNull E entity);
  
  int getTotal() throws SQLException;

  void setPrimaryKey(@NonNull E entity, PK primaryKey);
  
//  <T> Collection<T> getTableInfo() throws SQLException;
}
