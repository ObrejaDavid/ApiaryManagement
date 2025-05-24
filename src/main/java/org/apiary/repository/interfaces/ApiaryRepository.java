package org.apiary.repository.interfaces;

import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;

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
}