package org.apiary.utils.observer;

import org.apiary.utils.events.Event;

/**
 * Interface for observer classes
 * @param <E> The type of event to observe
 */
public interface Observer<E extends Event> {
    /**
     * Handle an event
     * @param event The event to handle
     */
    void update(E event);
}