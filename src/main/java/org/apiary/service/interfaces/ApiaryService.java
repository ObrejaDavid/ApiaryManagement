package org.apiary.service.interfaces;

import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.utils.events.EntityChangeEvent;
import org.apiary.utils.observer.Observable;

import java.util.List;
import java.util.Optional;

public interface ApiaryService extends Observable<EntityChangeEvent<?>> {
    /**
     * Create a new apiary
     * @param name The name of the apiary
     * @param location The location of the apiary
     * @param beekeeper The beekeeper who owns the apiary
     * @return The created apiary, or null if creation failed
     */
    Apiary createApiary(String name, String location, Beekeeper beekeeper);

    /**
     * Find an apiary by ID
     * @param apiaryId The ID of the apiary
     * @return An Optional containing the apiary if found, empty otherwise
     */
    Optional<Apiary> findById(Integer apiaryId);

    /**
     * Find all apiaries owned by a specific beekeeper
     * @param beekeeper The beekeeper
     * @return A list of apiaries owned by the beekeeper
     */
    List<Apiary> findByBeekeeper(Beekeeper beekeeper);

    /**
     * Find apiaries by name (partial match)
     * @param name The name to search for
     * @return A list of apiaries with matching names
     */
    List<Apiary> findByNameContaining(String name);

    /**
     * Find apiaries by location (partial match)
     * @param location The location to search for
     * @return A list of apiaries with matching locations
     */
    List<Apiary> findByLocationContaining(String location);

    /**
     * Update an apiary
     * @param apiaryId The ID of the apiary to update
     * @param name The new name
     * @param location The new location
     * @param beekeeper The beekeeper who owns the apiary
     * @return The updated apiary, or null if update failed
     */
    Apiary updateApiary(Integer apiaryId, String name, String location, Beekeeper beekeeper);

    /**
     * Delete an apiary
     * @param apiaryId The ID of the apiary to delete
     * @return true if the apiary was deleted successfully, false otherwise
     */
    boolean deleteApiary(Integer apiaryId);

    /**
     * Check if a beekeeper owns an apiary
     * @param beekeeper The beekeeper
     * @param apiaryId The ID of the apiary
     * @return true if the beekeeper owns the apiary, false otherwise
     */
    boolean isApiaryOwnedByBeekeeper(Beekeeper beekeeper, Integer apiaryId);


    /**
     * Find all apiaries
     * @return A list of all apiaries
     */
    List<Apiary> findAll();

    /**
     * Find all unique locations
     * @return A list of unique apiary locations
     */
    List<String> findAllLocations();
}