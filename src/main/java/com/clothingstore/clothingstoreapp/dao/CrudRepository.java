package com.clothingstore.clothingstoreapp.dao;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<T, ID> {
    T create(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean update(T entity);

    boolean deleteById(ID id);
}

