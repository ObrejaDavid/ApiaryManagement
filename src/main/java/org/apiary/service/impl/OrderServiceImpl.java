package org.apiary.service.impl;

import org.apiary.model.*;
import org.apiary.repository.interfaces.OrderItemRepository;
import org.apiary.repository.interfaces.OrderRepository;
import org.apiary.service.interfaces.HoneyProductService;
import org.apiary.service.interfaces.OrderService;
import org.apiary.service.interfaces.PaymentService;
import org.apiary.service.interfaces.ShoppingCartService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = Logger.getLogger(OrderServiceImpl.class.getName());
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartService shoppingCartService;
    private final PaymentService paymentService;
    private final HoneyProductService honeyProductService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            ShoppingCartService shoppingCartService,
                            PaymentService paymentService,
                            HoneyProductService honeyProductService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shoppingCartService = shoppingCartService;
        this.paymentService = paymentService;
        this.honeyProductService = honeyProductService;
    }

    @Override
    public Order createOrderFromCart(Client client) {
        try {
            // Get cart items
            List<CartItem> cartItems = shoppingCartService.getCartItems(client);
            if (cartItems.isEmpty()) {
                LOGGER.warning("Cannot create order from empty cart for client: " + client.getUsername());
                return null;
            }

            // Create order
            Order order = new Order(client);
            Order savedOrder = orderRepository.save(order);

            // Create order items from cart items
            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem(
                        savedOrder,
                        cartItem.getProduct(),
                        cartItem.getQuantity(),
                        cartItem.getPrice());
                orderItemRepository.save(orderItem);

                // Add order item to order
                savedOrder.addItem(orderItem);
            }

            // Calculate total
            savedOrder.recalculateTotal();
            savedOrder = orderRepository.save(savedOrder);

            // Clear cart
            shoppingCartService.clearCart(client);

            LOGGER.info("Created order from cart for client: " + client.getUsername());
            return savedOrder;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating order from cart for client: " + client.getUsername(), e);
            return null;
        }
    }

    @Override
    public Optional<Order> findById(Integer orderId) {
        try {
            return orderRepository.findById(orderId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding order by ID: " + orderId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Order> findByClient(Client client) {
        try {
            return orderRepository.findByClient(client);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by client: " + client.getUsername(), e);
            return List.of();
        }
    }

    @Override
    public List<Order> findByStatus(String status) {
        try {
            return orderRepository.findByStatus(status);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by status: " + status, e);
            return List.of();
        }
    }

    @Override
    public List<Order> findByDateAfter(LocalDateTime date) {
        try {
            return orderRepository.findByDateAfter(date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by date after: " + date, e);
            return List.of();
        }
    }

    @Override
    public List<Order> findByDateBefore(LocalDateTime date) {
        try {
            return orderRepository.findByDateBefore(date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by date before: " + date, e);
            return List.of();
        }
    }

    @Override
    public List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return orderRepository.findByDateBetween(startDate, endDate);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by date between: " +
                    startDate + " and " + endDate, e);
            return List.of();
        }
    }

    @Override
    public List<OrderItem> getOrderItems(Integer orderId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                LOGGER.warning("Order not found: " + orderId);
                return List.of();
            }

            Order order = orderOpt.get();
            return orderItemRepository.findByOrder(order);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting order items for order: " + orderId, e);
            return List.of();
        }
    }

    @Override
    public boolean processPayment(Integer orderId) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                LOGGER.warning("Order not found: " + orderId);
                return false;
            }

            Order order = orderOpt.get();

            // Check if order is already paid
            if ("PAID".equals(order.getStatus())) {
                LOGGER.warning("Order is already paid: " + orderId);
                return true;
            }

            // Check if order is already canceled
            if ("CANCELED".equals(order.getStatus())) {
                LOGGER.warning("Cannot process payment for canceled order: " + orderId);
                return false;
            }

            // Process payment
            boolean paymentSuccess = paymentService.processPayment(order);

            if (paymentSuccess) {
                // Update order status
                order.setStatus("PAID");
                orderRepository.save(order);

                // Update product quantities
                List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                for (OrderItem item : orderItems) {
                    honeyProductService.updateQuantityAfterPurchase(
                            item.getProduct().getProductId(),
                            item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
                }

                LOGGER.info("Payment processed successfully for order: " + orderId);
            } else {
                LOGGER.warning("Payment failed for order: " + orderId);
            }

            return paymentSuccess;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing payment for order: " + orderId, e);
            return false;
        }
    }

    @Override
    public boolean updateOrderStatus(Integer orderId, String status) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                LOGGER.warning("Order not found: " + orderId);
                return false;
            }

            Order order = orderOpt.get();

            // Validate status transition
            if (!isValidStatusTransition(order.getStatus(), status)) {
                LOGGER.warning("Invalid status transition from " + order.getStatus() +
                        " to " + status + " for order: " + orderId);
                return false;
            }

            order.setStatus(status);
            orderRepository.save(order);

            LOGGER.info("Updated status to " + status + " for order: " + orderId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating status for order: " + orderId, e);
            return false;
        }
    }

    @Override
    public boolean cancelOrder(Integer orderId, Client client) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                LOGGER.warning("Order not found: " + orderId);
                return false;
            }

            Order order = orderOpt.get();

            // Check if order belongs to client
            if (!order.getClient().equals(client)) {
                LOGGER.warning("Order does not belong to client: " +
                        orderId + ", " + client.getUsername());
                return false;
            }

            // Check if order can be canceled
            if (!"PENDING".equals(order.getStatus())) {
                LOGGER.warning("Cannot cancel order with status: " + order.getStatus() +
                        " for order: " + orderId);
                return false;
            }

            order.setStatus("CANCELED");
            orderRepository.save(order);

            LOGGER.info("Canceled order: " + orderId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error canceling order: " + orderId, e);
            return false;
        }
    }

    /**
     * Check if a status transition is valid
     * @param currentStatus The current status
     * @param newStatus The new status
     * @return true if the transition is valid, false otherwise
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return true;
        }

        switch (currentStatus) {
            case "PENDING":
                return "PAID".equals(newStatus) || "CANCELED".equals(newStatus);
            case "PAID":
                return "DELIVERED".equals(newStatus);
            case "CANCELED":
                return false; // Cannot transition from canceled
            case "DELIVERED":
                return false; // Cannot transition from delivered
            default:
                return false;
        }
    }
}