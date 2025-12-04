package com.neptunedreams.framework.data;

import java.sql.SQLException;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/28/17
 * <p>Time: 6:52 PM
 *
 * @author Miguel Muñoz
 */
public interface Dao<E, PK, F extends DBField> {
  boolean createTableIfNeeded() throws SQLException;
  
  Collection<@NotNull E> getAll(@Nullable F orderBy) throws SQLException;
  
  Collection<@NotNull E> find(String text, @Nullable F orderBy) throws SQLException;
  Collection<@NotNull E> findAny(@Nullable F orderBy, String... text) throws SQLException;
  Collection<@NotNull E> findAll(@Nullable F orderBy, String... text) throws SQLException;

  Collection<@NotNull E> findInField(String text, @NotNull F findBy, @Nullable F orderBy) throws SQLException;
  Collection<@NotNull E> findAnyInField(@NotNull F findBy, @Nullable F orderBy, String... text) throws SQLException;
  Collection<@NotNull E> findAllInField(@NotNull F findBy, @Nullable F orderBy, String... text) throws SQLException;

  /**
   * insert or update the entity.
   * @param entity The entity
   * @throws SQLException Yeah, you know.
   */
  void update(@NotNull E entity) throws SQLException;

  void insert(@NotNull E entity) throws SQLException;

  /**
   * insert or update the provided entity. If the id is 0 or null, it inserts the record. Otherwise it updates it.
   * @param entity The entity to save
   * @throws SQLException Sql exception
   */
  void insertOrUpdate(@NotNull E entity) throws SQLException;

  void delete(@NotNull E entity) throws SQLException;
  
  PK getNextId() throws SQLException;
  
  PK getPrimaryKey(@NotNull E entity);
  
  int getTotal() throws SQLException;

  void setPrimaryKey(@NotNull E entity, PK primaryKey);
}
