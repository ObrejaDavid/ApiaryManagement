// File: src/main/java/org/apiary/service/interfaces/OrderService.java
// Replace the interface declaration line

package org.apiary.service.interfaces;

import org.apiary.model.Beekeeper;
import org.apiary.model.Client;
import org.apiary.model.Order;
import org.apiary.model.OrderItem;
import org.apiary.utils.events.EntityChangeEvent;
import org.apiary.utils.observer.Observable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService extends Observable<EntityChangeEvent<?>> {
    /**
     * Create a new order from a client's shopping cart
     * @param client The client
     * @return The created order, or null if creation failed
     */
    Order createOrderFromCart(Client client);

    /**
     * Find an order by ID
     * @param orderId The ID of the order
     * @return An Optional containing the order if found, empty otherwise
     */
    Optional<Order> findById(Integer orderId);

    /**
     * Find all orders for a client
     * @param client The client
     * @return A list of orders for the client
     */
    List<Order> findByClient(Client client);

    /**
     * Find orders with a specific status
     * @param status The status to search for
     * @return A list of orders with the specified status
     */
    List<Order> findByStatus(String status);

    /**
     * Find orders created after a specific date
     * @param date The date
     * @return A list of orders created after the specified date
     */
    List<Order> findByDateAfter(LocalDateTime date);

    /**
     * Find orders created before a specific date
     * @param date The date
     * @return A list of orders created before the specified date
     */
    List<Order> findByDateBefore(LocalDateTime date);

    /**
     * Find orders created between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of orders created between the specified dates
     */
    List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get all items in an order
     * @param orderId The ID of the order
     * @return A list of items in the order
     */
    List<OrderItem> getOrderItems(Integer orderId);

    /**
     * Process payment for an order
     * @param orderId The ID of the order
     * @return true if payment was processed successfully, false otherwise
     */
    boolean processPayment(Integer orderId);

    /**
     * Update the status of an order
     * @param orderId The ID of the order
     * @param status The new status
     * @return true if the status was updated successfully, false otherwise
     */
    boolean updateOrderStatus(Integer orderId, String status);

    /**
     * Cancel an order
     * @param orderId The ID of the order
     * @param client The client
     * @return true if the order was canceled successfully, false otherwise
     */
    boolean cancelOrder(Integer orderId, Client client);

    /**
     * Find orders for a beekeeper's products
     * @param beekeeper The beekeeper
     * @return A list of orders containing the beekeeper's products
     */
    List<Order> findOrdersForBeekeeper(Beekeeper beekeeper);

    /**
     * Find orders with filters
     * @param beekeeper The beekeeper
     * @param status The status filter
     * @param startDate The start date filter
     * @param endDate The end date filter
     * @return A list of filtered orders
     */
    List<Order> findOrdersWithFilters(Beekeeper beekeeper, String status, LocalDateTime startDate, LocalDateTime endDate);
}