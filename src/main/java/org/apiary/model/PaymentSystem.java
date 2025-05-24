package org.apiary.model;

import java.math.BigDecimal;

/**
 * Represents an external payment system for processing payments
 * This is not a JPA entity but a service interface
 */
public class PaymentSystem {

    private String paymentProvider;
    private String apiKey;

    public PaymentSystem() {
        // Default constructor
    }

    public PaymentSystem(String paymentProvider, String apiKey) {
        this.paymentProvider = paymentProvider;
        this.apiKey = apiKey;
    }

    /**
     * Process a payment for an order
     * @param order The order to process payment for
     * @return true if payment is successful, false otherwise
     */
    public boolean processPayment(Order order) {
        // In a real application, this would integrate with a payment gateway
        try {
            // Simulate integration with payment processor
            System.out.println("Processing payment with " + paymentProvider + " for Order #" + order.getOrderId());
            System.out.println("Amount: " + order.getTotal() + " RON");

            // Simulate network delay
            Thread.sleep(1500);

            // Create payment record
            Payment payment = new Payment(order, order.getTotal(), "SUCCESS");
            order.setPayment(payment);
            order.setStatus("PAID");

            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Payment processing interrupted: " + e.getMessage());

            // Create failed payment record
            Payment payment = new Payment(order, order.getTotal(), "FAILED");
            order.setPayment(payment);

            return false;
        } catch (Exception e) {
            System.err.println("Payment processing error: " + e.getMessage());

            // Create failed payment record
            Payment payment = new Payment(order, order.getTotal(), "FAILED");
            order.setPayment(payment);

            return false;
        }
    }

    /**
     * Handle payment error (e.g., insufficient funds, card declined)
     * @param order The order with the failed payment
     * @param errorCode The error code
     * @param errorMessage The error message
     */
    public void handleError(Order order, String errorCode, String errorMessage) {
        System.err.println("Payment error for Order #" + order.getOrderId() + ": " + errorCode + " - " + errorMessage);

        // Create failed payment record
        Payment payment = new Payment(order, order.getTotal(), "FAILED");
        payment.setStatus("FAILED");
        order.setPayment(payment);

        // In a real application, this would log the error and possibly notify the user
    }
}