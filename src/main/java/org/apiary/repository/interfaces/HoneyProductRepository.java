package org.apiary.repository.interfaces;

import org.apiary.model.Apiary;
import org.apiary.model.Hive;
import org.apiary.model.HoneyProduct;

import java.math.BigDecimal;
import java.util.List;

public interface HoneyProductRepository extends Repository<Integer, HoneyProduct> {
    /**
     * Find all honey products from a specific apiary
     * @param apiary The apiary
     * @return A list of honey products from the apiary
     */
    List<HoneyProduct> findByApiary(Apiary apiary);

    /**
     * Find all honey products from a specific hive
     * @param hive The hive
     * @return A list of honey products from the hive
     */
    List<HoneyProduct> findByHive(Hive hive);

    /**
     * Find honey products by name (partial match)
     * @param name The name to search for
     * @return A list of honey products with matching names
     */
    List<HoneyProduct> findByNameContaining(String name);

    /**
     * Find honey products with a price less than the specified amount
     * @param price The maximum price
     * @return A list of honey products with a price less than the specified amount
     */
    List<HoneyProduct> findByPriceLessThan(BigDecimal price);

    /**
     * Find honey products with a price greater than the specified amount
     * @param price The minimum price
     * @return A list of honey products with a price greater than the specified amount
     */
    List<HoneyProduct> findByPriceGreaterThan(BigDecimal price);

    /**
     * Find honey products with a price between the specified amounts
     * @param minPrice The minimum price
     * @param maxPrice The maximum price
     * @return A list of honey products with a price between the specified amounts
     */
    List<HoneyProduct> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find honey products with available quantity greater than zero
     * @return A list of honey products with available quantity
     */
    List<HoneyProduct> findAvailableProducts();
}