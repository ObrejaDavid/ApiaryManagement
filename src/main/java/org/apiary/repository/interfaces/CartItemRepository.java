package org.apiary.repository.interfaces;

import org.apiary.model.CartItem;
import org.apiary.model.HoneyProduct;
import org.apiary.model.ShoppingCart;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends Repository<Integer, CartItem> {
    /**
     * Find all cart items in a specific shopping cart
     * @param cart The shopping cart
     * @return A list of cart items in the shopping cart
     */
    List<CartItem> findByCart(ShoppingCart cart);

    /**
     * Find a cart item for a specific product in a specific shopping cart
     * @param cart The shopping cart
     * @param product The product
     * @return An Optional containing the cart item, or empty if not found
     */
    Optional<CartItem> findByCartAndProduct(ShoppingCart cart, HoneyProduct product);

    /**
     * Delete all cart items in a specific shopping cart
     * @param cart The shopping cart
     */
    void deleteByCart(ShoppingCart cart);
}