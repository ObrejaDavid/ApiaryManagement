package org.apiary.service.impl;

import org.apiary.model.*;
import org.apiary.repository.interfaces.OrderItemRepository;
import org.apiary.repository.interfaces.OrderRepository;
import org.apiary.service.interfaces.HoneyProductService;
import org.apiary.service.interfaces.OrderService;
import org.apiary.service.interfaces.PaymentService;
import org.apiary.service.interfaces.ShoppingCartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
            LOGGER.info("=== STARTING ORDER CREATION DEBUG ===");
            LOGGER.info("Client: " + client.getUsername() + " (ID: " + client.getUserId() + ")");

            // Get cart items with detailed logging
            List<CartItem> cartItems = shoppingCartService.getCartItems(client);
            LOGGER.info("Found " + cartItems.size() + " cart items");

            if (cartItems.isEmpty()) {
                LOGGER.warning("Cannot create order from empty cart for client: " + client.getUsername());
                return null;
            }

            // Log each cart item
            for (int i = 0; i < cartItems.size(); i++) {
                CartItem item = cartItems.get(i);
                LOGGER.info("Cart Item " + (i+1) + ": " + item.getProduct().getName() +
                        " | Quantity: " + item.getQuantity() +
                        " | Price: " + item.getPrice() +
                        " | Product ID: " + item.getProduct().getProductId());

                // Check product availability
                Optional<HoneyProduct> productOpt = honeyProductService.findById(item.getProduct().getProductId());
                if (productOpt.isEmpty()) {
                    LOGGER.severe("Product not found in database: " + item.getProduct().getProductId());
                    return null;
                }

                HoneyProduct product = productOpt.get();
                LOGGER.info("Product available quantity: " + product.getQuantity());

                if (product.getQuantity().compareTo(BigDecimal.valueOf(item.getQuantity())) < 0) {
                    LOGGER.severe("Insufficient stock for product: " + product.getName() +
                            " (Available: " + product.getQuantity() + ", Requested: " + item.getQuantity() + ")");
                    return null;
                }
            }

            // Create order
            LOGGER.info("Creating new order object...");
            Order order = new Order(client);
            LOGGER.info("Order object created successfully");

            // Verify client is properly attached
            if (order.getClient() == null || order.getClient().getUserId() == null) {
                LOGGER.severe("Client is not properly attached to order");
                return null;
            }
            LOGGER.info("Client properly attached to order");

            // Create order items - FIXED VERSION
            LOGGER.info("Creating order items...");
            for (CartItem cartItem : cartItems) {
                try {
                    LOGGER.info("Processing cart item: " + cartItem.getProduct().getName());

                    OrderItem orderItem = new OrderItem(
                            order,
                            cartItem.getProduct(),
                            cartItem.getQuantity(),
                            cartItem.getPrice());

                    // Use the proper addItem method which handles bidirectional relationship
                    order.addItem(orderItem);
                    LOGGER.info("Added order item successfully");

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error creating order item for product: " +
                            cartItem.getProduct().getName(), e);
                    return null;
                }
            }

            // Remove manual recalculateTotal() since addItem() already does this
            // order.recalculateTotal(); // REMOVE THIS LINE

            // Save order
            LOGGER.info("Attempting to save order to database...");
            try {
                Order savedOrder = orderRepository.save(order);
                if (savedOrder == null) {
                    LOGGER.severe("Repository returned null after save attempt");
                    return null;
                }

                if (savedOrder.getOrderId() == null) {
                    LOGGER.severe("Saved order has null ID");
                    return null;
                }

                LOGGER.info("Order saved successfully with ID: " + savedOrder.getOrderId());

                // Clear cart after successful order creation
                shoppingCartService.clearCart(client);
                LOGGER.info("Cart cleared successfully");

                LOGGER.info("=== ORDER CREATION COMPLETED SUCCESSFULLY ===");
                return savedOrder;

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Database error while saving order", e);
                return null;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error creating order from cart for client: " + client.getUsername(), e);
            e.printStackTrace();
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

    @Override
    public List<Order> findOrdersForBeekeeper(Beekeeper beekeeper) {
        try {
            List<Order> allOrders = orderRepository.findAll();
            return allOrders.stream()
                    .filter(order -> {
                        return order.getItems().stream()
                                .anyMatch(item -> {
                                    Apiary apiary = item.getProduct().getApiary();
                                    return apiary.getBeekeeper().equals(beekeeper);
                                });
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders for beekeeper: " + beekeeper.getUsername(), e);
            return List.of();
        }
    }

    @Override
    public List<Order> findOrdersWithFilters(Beekeeper beekeeper, String status, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Order> beekeeperOrders = findOrdersForBeekeeper(beekeeper);

            return beekeeperOrders.stream()
                    .filter(order -> {
                        if (status != null && !order.getStatus().equals(status)) {
                            return false;
                        }
                        if (startDate != null && order.getDate().isBefore(startDate)) {
                            return false;
                        }
                        if (endDate != null && order.getDate().isAfter(endDate)) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders with filters for beekeeper: " + beekeeper.getUsername(), e);
            return List.of();
        }
    }
}