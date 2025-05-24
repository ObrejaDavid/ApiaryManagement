package org.apiary.utils.events;

/**
 * Event class for entity changes
 * @param <T> The type of the entity
 */
public class EntityChangeEvent<T> implements Event {

    public enum Type {
        CREATED,
        UPDATED,
        DELETED
    }

    private final Type type;
    private final T entity;
    private final T oldEntity;
    private final String entityType;

    /**
     * Create a new entity change event
     * @param type The type of change
     * @param entity The entity that changed
     */
    public EntityChangeEvent(Type type, T entity) {
        this(type, entity, null);
    }

    /**
     * Create a new entity change event with the old entity state
     * @param type The type of change
     * @param entity The new entity state
     * @param oldEntity The old entity state (for updates)
     */
    public EntityChangeEvent(Type type, T entity, T oldEntity) {
        this.type = type;
        this.entity = entity;
        this.oldEntity = oldEntity;
        this.entityType = entity != null ? entity.getClass().getSimpleName() : "Unknown";
    }

    /**
     * Get the type of change
     * @return The type of change
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the entity that changed
     * @return The entity that changed
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Get the old entity state (for updates)
     * @return The old entity state, or null if not applicable
     */
    public T getOldEntity() {
        return oldEntity;
    }

    /**
     * Get the entity type as string
     * @return The entity type
     */
    public String getEntityType() {
        return entityType;
    }
}