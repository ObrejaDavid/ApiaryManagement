package org.apiary.repository.interfaces;

import org.apiary.model.Order;
import org.apiary.model.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends Repository<Integer, Payment> {
    /**
     * Find the payment for a specific order
     * @param order The order
     * @return The payment for the order, or null if not found
     */
    Payment findByOrder(Order order);

    /**
     * Find payments with a specific status
     * @param status The status to search for
     * @return A list of payments with the specified status
     */
    List<Payment> findByStatus(String status);

    /**
     * Find payments made after a specific date
     * @param date The date
     * @return A list of payments made after the specified date
     */
    List<Payment> findByDateAfter(LocalDateTime date);

    /**
     * Find payments made before a specific date
     * @param date The date
     * @return A list of payments made before the specified date
     */
    List<Payment> findByDateBefore(LocalDateTime date);

    /**
     * Find payments made between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @return A list of payments made between the specified dates
     */
    List<Payment> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}