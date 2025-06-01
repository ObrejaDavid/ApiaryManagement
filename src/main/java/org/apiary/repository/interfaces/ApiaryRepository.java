package org.apiary.repository.interfaces;

import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.model.Hive;

import java.util.List;

public interface ApiaryRepository extends Repository<Integer, Apiary> {
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
     * Find all hives in a specific apiary
     * @param apiary The apiary
     * @return A list of hives in the apiary
     */
    List<Hive> findHivesByApiary(Apiary apiary);

    /**
     * Find all hives in a specific apiary by apiary ID
     * @param apiaryId The apiary ID
     * @return A list of hives in the apiary
     */
    List<Hive> findHivesByApiaryId(Integer apiaryId);

    /**
     * Count the number of hives in an apiary
     * @param apiary The apiary
     * @return The number of hives in the apiary
     */
    long countHivesByApiary(Apiary apiary);

    /**
     * Count the number of hives in an apiary by apiary ID
     * @param apiaryId The apiary ID
     * @return The number of hives in the apiary
     */
    long countHivesByApiaryId(Integer apiaryId);
}