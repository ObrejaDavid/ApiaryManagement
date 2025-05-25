package org.apiary.utils.observer;

import com.sun.javafx.logging.PlatformLogger;
import org.apiary.utils.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

/**
 * Base class for implementing the Observable interface
 * @param <E> The type of event to notify observers of
 */
public class EventManager<E extends Event> implements Observable<E> {
    // Using CopyOnWriteArrayList for thread safety when notifying observers
    private final List<Observer<E>> observers = new CopyOnWriteArrayList<>();

    @Override
    public void addObserver(Observer<E> observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer<E> observer) {
        if (observer != null) {
            observers.remove(observer);
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
                LOGGER.error("Error notifying observer: " + observer.getClass().getSimpleName(), e);
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