package com.example.busstationmanagementsystem.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {
    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean deleteById(ID id);

    default T save(T entity, StorageMode mode) {
        return save(entity);
    }

    default Optional<T> findById(ID id, StorageMode mode) {
        return findById(id);
    }

    default List<T> findAll(StorageMode mode) {
        return findAll();
    }

    default boolean deleteById(ID id, StorageMode mode) {
        return deleteById(id);
    }

    default CrudRepository<T, ID> forMode(StorageMode mode) {
        return this;
    }
}
