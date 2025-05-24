package org.apiary.service.interfaces;

import org.apiary.model.CartItem;
import org.apiary.model.Client;
import org.apiary.model.HoneyProduct;
import org.apiary.model.ShoppingCart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ShoppingCartService {
    /**
     * Find the shopping cart for a client
     * @param client The client
     * @return An Optional containing the shopping cart if found, empty otherwise
     */
    Optional<ShoppingCart> findByClient(Client client);

    /**
     * Get all items in a client's shopping cart
     * @param client The client
     * @return A list of items in the client's shopping cart
     */
    List<CartItem> getCartItems(Client client);

    /**
     * Add a product to a client's shopping cart
     * @param client The client
     * @param product The product to add
     * @param quantity The quantity to add
     * @return true if the product was added successfully, false otherwise
     */
    boolean addToCart(Client client, HoneyProduct product, int quantity);

    /**
     * Update the quantity of a product in a client's shopping cart
     * @param client The client
     * @param cartItemId The ID of the cart item
     * @param quantity The new quantity
     * @return true if the quantity was updated successfully, false otherwise
     */
    boolean updateCartItemQuantity(Client client, Integer cartItemId, int quantity);

    /**
     * Remove a product from a client's shopping cart
     * @param client The client
     * @param cartItemId The ID of the cart item
     * @return true if the product was removed successfully, false otherwise
     */
    boolean removeFromCart(Client client, Integer cartItemId);

    /**
     * Clear a client's shopping cart
     * @param client The client
     * @return true if the cart was cleared successfully, false otherwise
     */
    boolean clearCart(Client client);

    /**
     * Calculate the total price of a client's shopping cart
     * @param client The client
     * @return The total price of the client's shopping cart
     */
    BigDecimal calculateCartTotal(Client client);
}