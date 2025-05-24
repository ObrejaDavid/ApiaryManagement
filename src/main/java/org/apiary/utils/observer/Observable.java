package org.apiary.utils.observer;

import org.apiary.utils.events.Event;

/**
 * Interface for observable classes
 * @param <E> The type of event to notify observers of
 */
public interface Observable<E extends Event> {
    /**
     * Add an observer
     * @param observer The observer to add
     */
    void addObserver(Observer<E> observer);

    /**
     * Remove an observer
     * @param observer The observer to remove
     */
    void removeObserver(Observer<E> observer);

    /**
     * Notify all observers of an event
     * @param event The event to notify observers of
     */
    void notifyObservers(E event);
}