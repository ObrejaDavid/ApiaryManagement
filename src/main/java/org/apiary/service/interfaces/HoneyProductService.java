package org.apiary.service.interfaces;

import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.model.Hive;
import org.apiary.model.HoneyProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HoneyProductService {
    /**
     * Create a new honey product
     * @param name The name of the product
     * @param description The description of the product
     * @param price The price of the product
     * @param quantity The quantity of the product
     * @param apiary The apiary where the product is produced
     * @param hive The hive where the product is produced (optional)
     * @param beekeeper The beekeeper who owns the apiary
     * @return The created honey product, or null if creation failed
     */
    HoneyProduct createHoneyProduct(String name, String description, BigDecimal price,
                                    BigDecimal quantity, Apiary apiary, Hive hive,
                                    Beekeeper beekeeper);

    /**
     * Find a honey product by ID
     * @param productId The ID of the product
     * @return An Optional containing the product if found, empty otherwise
     */
    Optional<HoneyProduct> findById(Integer productId);

    /**
     * Find all honey products
     * @return A list of all honey products
     */
    List<HoneyProduct> findAll();

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
     * Find honey products with available quantity
     * @return A list of honey products with available quantity
     */
    List<HoneyProduct> findAvailableProducts();

    /**
     * Update a honey product
     * @param productId The ID of the product to update
     * @param name The new name
     * @param description The new description
     * @param price The new price
     * @param quantity The new quantity
     * @param beekeeper The beekeeper who owns the product
     * @return The updated honey product, or null if update failed
     */
    HoneyProduct updateHoneyProduct(Integer productId, String name, String description,
                                    BigDecimal price, BigDecimal quantity, Beekeeper beekeeper);

    /**
     * Delete a honey product
     * @param productId The ID of the product to delete
     * @param beekeeper The beekeeper who owns the product
     * @return true if the product was deleted successfully, false otherwise
     */
    boolean deleteHoneyProduct(Integer productId, Beekeeper beekeeper);

    /**
     * Check if a honey product belongs to a beekeeper
     * @param productId The ID of the product
     * @param beekeeper The beekeeper
     * @return true if the product belongs to the beekeeper, false otherwise
     */
    boolean isProductOwnedByBeekeeper(Integer productId, Beekeeper beekeeper);

    /**
     * Update the quantity of a honey product after a purchase
     * @param productId The ID of the product
     * @param quantityToSubtract The quantity to subtract
     * @return true if the quantity was updated successfully, false otherwise
     */
    boolean updateQuantityAfterPurchase(Integer productId, BigDecimal quantityToSubtract);
}