// src/main/java/org/apiary/utils/observer/EventManager.java
package org.apiary.utils.observer;

import org.apiary.utils.events.Event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for implementing the Observable interface
 * @param <E> The type of event to notify observers of
 */
public class EventManager<E extends Event> implements Observable<E> {
    private static final Logger LOGGER = Logger.getLogger(EventManager.class.getName());

    // Using CopyOnWriteArrayList for thread safety when notifying observers
    private final List<Observer<E>> observers = new CopyOnWriteArrayList<>();

    @Override
    public void addObserver(Observer<E> observer) {
        if (observer != null) {
            // Log the registration attempt
            LOGGER.info("=== ATTEMPTING TO ADD OBSERVER ===");
            LOGGER.info("Observer: " + observer.getClass().getSimpleName() + "@" +
                    Integer.toHexString(observer.hashCode()));
            LOGGER.info("Current observer count BEFORE adding: " + observers.size());

            // Check if already exists
            boolean alreadyExists = observers.contains(observer);
            LOGGER.info("Observer already exists: " + alreadyExists);

            if (!alreadyExists) {
                observers.add(observer);
                LOGGER.info("Successfully added observer. New count: " + observers.size());
            } else {
                LOGGER.warning("Observer already exists, not adding duplicate");
            }
            LOGGER.info("=== ADD OBSERVER COMPLETED ===");
        } else {
            LOGGER.warning("Attempted to add null observer");
        }
    }

    @Override
    public void removeObserver(Observer<E> observer) {
        if (observer != null) {
            observers.remove(observer);
            LOGGER.info("Removed observer: " + observer.getClass().getSimpleName() +
                    ". Total observers: " + observers.size());
        }
    }

    @Override
    public void notifyObservers(E event) {
        LOGGER.info("EventManager notifying " + observers.size() + " observers of event: " +
                event.getClass().getSimpleName());

        for (Observer<E> observer : observers) {
            try {
                LOGGER.info("Notifying observer: " + observer.getClass().getSimpleName());
                observer.update(event);
                LOGGER.info("Observer notification completed for: " + observer.getClass().getSimpleName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error notifying observer: " + observer.getClass().getSimpleName(), e);
            }
        }

        LOGGER.info("All observer notifications completed");
    }



    /**
     * Get the number of observers
     * @return The number of observers
     */
    public int countObservers() {
        return observers.size();
    }

    /**
     * Check if there are any observers
     * @return true if there are observers, false otherwise
     */
    public boolean hasObservers() {
        return !observers.isEmpty();
    }

    /**
     * Remove all observers
     */
    public void clearObservers() {
        observers.clear();
    }
}