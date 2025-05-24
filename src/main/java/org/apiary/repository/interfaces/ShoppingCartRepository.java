package org.apiary.repository.interfaces;

import org.apiary.model.Client;
import org.apiary.model.ShoppingCart;

import java.util.Optional;

public interface ShoppingCartRepository extends Repository<Integer, ShoppingCart> {
    /**
     * Find the shopping cart for a specific client
     * @param client The client
     * @return An Optional containing the shopping cart, or empty if not found
     */
    Optional<ShoppingCart> findByClient(Client client);

    /**
     * Delete the shopping cart for a specific client
     * @param client The client
     */
    void deleteByClient(Client client);
}