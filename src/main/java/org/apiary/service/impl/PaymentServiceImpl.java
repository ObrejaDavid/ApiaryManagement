package org.apiary.service.impl;

import org.apiary.model.Order;
import org.apiary.model.Payment;
import org.apiary.model.PaymentSystem;
import org.apiary.repository.interfaces.PaymentRepository;
import org.apiary.service.interfaces.PaymentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentServiceImpl.class.getName());
    private final PaymentRepository paymentRepository;
    private final PaymentSystem paymentSystem;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;

        // Initialize payment system (could be injected or configured elsewhere)
        this.paymentSystem = new PaymentSystem("Stripe", "stripe_api_key");
    }

    @Override
    public boolean processPayment(Order order) {
        try {
            boolean paymentSuccessful = paymentSystem.processPayment(order);
            LOGGER.info("Processed payment for order: " + order.getOrderId() +
                    " with result: " + paymentSuccessful);
            return paymentSuccessful;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing payment for order: " + order.getOrderId(), e);
            return false;
        }
    }

    @Override
    public Payment findByOrder(Order order) {
        try {
            return paymentRepository.findByOrder(order);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payment by order: " + order.getOrderId(), e);
            return null;
        }
    }

    @Override
    public List<Payment> findByStatus(String status) {
        try {
            return paymentRepository.findByStatus(status);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payments by status: " + status, e);
            return List.of();
        }
    }

    @Override
    public List<Payment> findByDateAfter(LocalDateTime date) {
        try {
            return paymentRepository.findByDateAfter(date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payments by date after: " + date, e);
            return List.of();
        }
    }

    @Override
    public List<Payment> findByDateBefore(LocalDateTime date) {
        try {
            return paymentRepository.findByDateBefore(date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payments by date before: " + date, e);
            return List.of();
        }
    }

    @Override
    public List<Payment> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return paymentRepository.findByDateBetween(startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payments by date between: " +
                    startDate + " and " + endDate, e);
            return List.of();
        }
    }
}