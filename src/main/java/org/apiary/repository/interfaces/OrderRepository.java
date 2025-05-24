package org.apiary.repository.interfaces;

import org.apiary.model.Client;
import org.apiary.model.Order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends Repository<Integer, Order> {
    /**
     * Find all orders for a specific client
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
}