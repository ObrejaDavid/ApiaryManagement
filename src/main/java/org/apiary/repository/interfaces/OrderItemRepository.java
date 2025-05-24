package org.apiary.repository.interfaces;

import org.apiary.model.HoneyProduct;
import org.apiary.model.Order;
import org.apiary.model.OrderItem;

import java.util.List;

public interface OrderItemRepository extends Repository<Integer, OrderItem> {
    /**
     * Find all order items for a specific order
     * @param order The order
     * @return A list of order items for the order
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Find all order items for a specific product
     * @param product The product
     * @return A list of order items for the product
     */
    List<OrderItem> findByProduct(HoneyProduct product);

    /**
     * Delete all order items for a specific order
     * @param order The order
     */
    void deleteByOrder(Order order);
}