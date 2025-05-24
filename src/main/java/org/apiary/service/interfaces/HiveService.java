package org.apiary.service.interfaces;

import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.model.Hive;
import org.apiary.utils.events.EntityChangeEvent;
import org.apiary.utils.observer.Observable;

import java.util.List;
import java.util.Optional;

public interface HiveService extends Observable<EntityChangeEvent<?>> {
    /**
     * Create a new hive
     * @param hiveNumber The hive number
     * @param queenYear The queen year
     * @param apiary The apiary
     * @param beekeeper The beekeeper
     * @return The created hive, or null if creation failed
     */
    Hive createHive(Integer hiveNumber, Integer queenYear, Apiary apiary, Beekeeper beekeeper);

    /**
     * Find a hive by ID
     * @param hiveId The ID of the hive
     * @return An Optional containing the hive if found, empty otherwise
     */
    Optional<Hive> findById(Integer hiveId);

    /**
     * Find all hives in a specific apiary
     * @param apiary The apiary
     * @return A list of hives in the apiary
     */
    List<Hive> findByApiary(Apiary apiary);

    /**
     * Find a hive by its number in a specific apiary
     * @param apiary The apiary
     * @param hiveNumber The hive number
     * @return A list of hives matching the criteria (should be 0 or 1)
     */
    List<Hive> findByApiaryAndHiveNumber(Apiary apiary, Integer hiveNumber);

    /**
     * Find hives by queen year
     * @param queenYear The queen year
     * @return A list of hives with queens from the specified year
     */
    List<Hive> findByQueenYear(Integer queenYear);

    /**
     * Count the number of hives in an apiary
     * @param apiary The apiary
     * @return The number of hives
     */
    long countByApiary(Apiary apiary);

    /**
     * Update a hive
     * @param hiveId The ID of the hive to update
     * @param hiveNumber The new hive number
     * @param queenYear The new queen year
     * @param beekeeper The beekeeper
     * @return The updated hive, or null if update failed
     */
    Hive updateHive(Integer hiveId, Integer hiveNumber, Integer queenYear, Beekeeper beekeeper);

    /**
     * Delete a hive
     * @param hiveId The ID of the hive to delete
     * @param beekeeper The beekeeper
     * @return true if the hive was deleted successfully, false otherwise
     */
    boolean deleteHive(Integer hiveId, Beekeeper beekeeper);

    /**
     * Check if a hive belongs to a beekeeper
     * @param hiveId The ID of the hive
     * @param beekeeper The beekeeper
     * @return true if the hive belongs to the beekeeper, false otherwise
     */
    boolean isHiveOwnedByBeekeeper(Integer hiveId, Beekeeper beekeeper);
}