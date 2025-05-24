package org.apiary.repository.interfaces;

import org.apiary.model.Apiary;
import org.apiary.model.Hive;

import java.util.List;

public interface HiveRepository extends Repository<Integer, Hive> {
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
}