package org.apiary.repository.interfaces;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface
 * @param <ID> The type of the entity's identifier
 * @param <T> The type of the entity
 */
public interface Repository<ID, T> {
    /**
     * Find an entity by its ID
     * @param id The entity's ID
     * @return An Optional containing the entity, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Find all entities
     * @return A list of all entities
     */
    List<T> findAll();

    /**
     * Save an entity (create or update)
     * @param entity The entity to save
     * @return The saved entity
     */
    T save(T entity);

    /**
     * Delete an entity
     * @param entity The entity to delete
     */
    void delete(T entity);

    /**
     * Delete an entity by its ID
     * @param id The ID of the entity to delete
     */
    void deleteById(ID id);
}