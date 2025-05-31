package org.apiary.service.impl;

import org.apiary.model.*;
import org.apiary.repository.interfaces.OrderItemRepository;
import org.apiary.repository.interfaces.OrderRepository;
import org.apiary.repository.interfaces.HoneyProductRepository;
import org.apiary.service.interfaces.HoneyProductService;
import org.apiary.service.interfaces.OrderService;
import org.apiary.service.interfaces.PaymentService;
import org.apiary.service.interfaces.ShoppingCartService;
import org.apiary.utils.events.EntityChangeEvent;
import org.apiary.utils.observer.EventManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OrderServiceImpl extends EventManager<EntityChangeEvent<?>> implements OrderService {

    private static final Logger LOGGER = Logger.getLogger(OrderServiceImpl.class.getName());
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShoppingCartService shoppingCartService;
    private final PaymentService paymentService;
    private final HoneyProductService honeyProductService;
    private final HoneyProductRepository honeyProductRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            ShoppingCartService shoppingCartService,
                            PaymentService paymentService,
                            HoneyProductService honeyProductService,
                            HoneyProductRepository honeyProductRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shoppingCartService = shoppingCartService;
        this.paymentService = paymentService;
        this.honeyProductService = honeyProductService;
        this.honeyProductRepository = honeyProductRepository;
    }

    @Override
    public Order createOrderFromCart(Client client) {
        try {
            LOGGER.info("=== STARTING ORDER CREATION WITH DIRECT PAYMENT ===");
            LOGGER.info("Client: " + client.getUsername() + " (ID: " + client.getUserId() + ")");
            List<CartItem> cartItems = shoppingCartService.getCartItems(client);
            LOGGER.info("Found " + cartItems.size() + " cart items");

            if (cartItems.isEmpty()) {
                LOGGER.warning("Cannot create order from empty cart for client: " + client.getUsername());
                return null;
            }

            for (int i = 0; i < cartItems.size(); i++) {
                CartItem item = cartItems.get(i);
                LOGGER.info("Cart Item " + (i+1) + ": " + item.getProduct().getName() +
                        " | Quantity: " + item.getQuantity() +
                        " | Price: " + item.getPrice() +
                        " | Product ID: " + item.getProduct().getProductId());

                Optional<HoneyProduct> productOpt = honeyProductService.findById(item.getProduct().getProductId());
                if (productOpt.isEmpty()) {
                    LOGGER.severe("Product not found in database: " + item.getProduct().getProductId());
                    return null;
                }

                HoneyProduct product = productOpt.get();
                LOGGER.info("Product current stock: " + product.getQuantity());

                if (product.getQuantity().compareTo(BigDecimal.valueOf(item.getQuantity())) < 0) {
                    LOGGER.severe("Insufficient stock for product: " + product.getName() +
                            " (Available: " + product.getQuantity() + ", Requested: " + item.getQuantity() + ")");
                    return null;
                }
            }

            Order order = new Order(client);
            order.setStatus("PAID");
            if (order.getClient() == null || order.getClient().getUserId() == null) {
                LOGGER.severe("Client is not properly attached to order");
                return null;
            }
            LOGGER.info("Client properly attached to order");

            for (CartItem cartItem : cartItems) {
                try {
                    OrderItem orderItem = new OrderItem(
                            order,
                            cartItem.getProduct(),
                            cartItem.getQuantity(),
                            cartItem.getPrice());

                    order.addItem(orderItem);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error creating order item for product: " +
                            cartItem.getProduct().getName(), e);
                    return null;
                }
            }

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
                LOGGER.info("Processing payment automatically for order: " + savedOrder.getOrderId());
                boolean paymentSuccess = paymentService.processPayment(savedOrder);

                if (paymentSuccess) {
                    LOGGER.info("Payment successful, updating product quantities");
                    List<OrderItem> orderItems = orderItemRepository.findByOrder(savedOrder);
                    boolean allStockUpdated = true;

                    for (OrderItem item : orderItems) {
                        BigDecimal quantityToSubtract = BigDecimal.valueOf(item.getQuantity());

                        LOGGER.info("Updating stock for product " + item.getProduct().getProductId() +
                                " - subtracting " + quantityToSubtract + " units");

                        boolean stockUpdated = honeyProductService.updateQuantityAfterPurchase(
                                item.getProduct().getProductId(),
                                quantityToSubtract);

                        if (!stockUpdated) {
                            LOGGER.severe("Failed to update stock for product: " + item.getProduct().getProductId());
                            allStockUpdated = false;
                            break;
                        } else {
                            LOGGER.info("Successfully updated stock for product: " + item.getProduct().getProductId());
                        }
                    }

                    if (!allStockUpdated) {
                        LOGGER.severe("Stock update failed, canceling order");
                        savedOrder.setStatus("CANCELED");
                        orderRepository.save(savedOrder);
                        notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.UPDATED, savedOrder));
                        return null;
                    }

                    LOGGER.info("Clearing shopping cart for client: " + client.getUsername());
                    boolean cartCleared = shoppingCartService.clearCart(client);
                    if (!cartCleared) {
                        LOGGER.warning("Failed to clear cart for client: " + client.getUsername());
                    }

                    notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.CREATED, savedOrder));

                    LOGGER.info("=== ORDER CREATION WITH PAYMENT COMPLETED SUCCESSFULLY ===");
                    return savedOrder;

                } else {
                    LOGGER.severe("Payment failed for order: " + savedOrder.getOrderId());
                    savedOrder.setStatus("CANCELED");
                    orderRepository.save(savedOrder);

                    notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.UPDATED, savedOrder));

                    return null;
                }

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
            Order oldOrder = new Order(order.getClient());
            oldOrder.setOrderId(order.getOrderId());
            oldOrder.setDate(order.getDate());
            oldOrder.setTotal(order.getTotal());
            oldOrder.setStatus(order.getStatus());

            if ("PAID".equals(order.getStatus())) {
                LOGGER.warning("Order is already paid: " + orderId);
                return true;
            }
            if ("CANCELED".equals(order.getStatus())) {
                LOGGER.warning("Cannot process payment for canceled order: " + orderId);
                return false;
            }

            LOGGER.info("Processing payment for order: " + orderId);
            boolean paymentSuccess = paymentService.processPayment(order);
            if (paymentSuccess) {
                LOGGER.info("Payment successful, updating order status and product quantities");
                order.setStatus("PAID");
                Order updatedOrder = orderRepository.save(order);

                if (updatedOrder != null) {
                    List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                    for (OrderItem item : orderItems) {
                        BigDecimal quantityToSubtract = BigDecimal.valueOf(item.getQuantity());
                        LOGGER.info("Updating stock for product " + item.getProduct().getProductId() +
                                " - subtracting " + quantityToSubtract + " units");
                        boolean stockUpdated = honeyProductService.updateQuantityAfterPurchase(
                                item.getProduct().getProductId(),
                                quantityToSubtract);

                        if (!stockUpdated) {
                            LOGGER.warning("Failed to update stock for product: " + item.getProduct().getProductId());
                        } else {
                            LOGGER.info("Successfully updated stock for product: " + item.getProduct().getProductId());
                        }
                    }
                    notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.UPDATED, updatedOrder, oldOrder));

                    LOGGER.info("Payment processed successfully for order: " + orderId + " and notified observers");
                } else {
                    LOGGER.warning("Failed to save paid order: " + orderId);
                    return false;
                }
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
            Order oldOrder = new Order(order.getClient());
            oldOrder.setOrderId(order.getOrderId());
            oldOrder.setDate(order.getDate());
            oldOrder.setTotal(order.getTotal());
            oldOrder.setStatus(order.getStatus());
            if (!isValidStatusTransition(order.getStatus(), status)) {
                LOGGER.warning("Invalid status transition from " + order.getStatus() +
                        " to " + status + " for order: " + orderId);
                return false;
            }

            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);
            if (updatedOrder != null) {
                notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.UPDATED, updatedOrder, oldOrder));

                LOGGER.info("Updated status to " + status + " for order: " + orderId + " and notified observers");
                return true;
            } else {
                LOGGER.warning("Failed to save order status update for order: " + orderId);
                return false;
            }
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
            Order oldOrder = new Order(order.getClient());
            oldOrder.setOrderId(order.getOrderId());
            oldOrder.setDate(order.getDate());
            oldOrder.setTotal(order.getTotal());
            oldOrder.setStatus(order.getStatus());
            if (!order.getClient().equals(client)) {
                LOGGER.warning("Order does not belong to client: " +
                        orderId + ", " + client.getUsername());
                return false;
            }
            if (!"PENDING".equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
                LOGGER.warning("Cannot cancel order with status: " + order.getStatus() +
                        " for order: " + orderId);
                return false;
            }
            if ("PAID".equals(order.getStatus())) {
                LOGGER.info("Restoring stock quantities for canceled order: " + orderId);

                List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                for (OrderItem item : orderItems) {
                    try {
                        Optional<HoneyProduct> productOpt = honeyProductRepository.findById(item.getProduct().getProductId());
                        if (productOpt.isPresent()) {
                            HoneyProduct product = productOpt.get();
                            BigDecimal currentQuantity = product.getQuantity();
                            BigDecimal restoredQuantity = currentQuantity.add(BigDecimal.valueOf(item.getQuantity()));
                            product.setQuantity(restoredQuantity);
                            HoneyProduct savedProduct = honeyProductRepository.save(product);
                            if (savedProduct != null) {
                                LOGGER.info("Restored " + item.getQuantity() + " units to product: " +
                                        product.getName() + " (new quantity: " + restoredQuantity + ")");
                            } else {
                                LOGGER.warning("Failed to restore stock for product: " + product.getName());
                            }
                        } else {
                            LOGGER.warning("Product not found when restoring stock: " + item.getProduct().getProductId());
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error restoring stock for product: " +
                                item.getProduct().getProductId(), e);
                    }
                }
            }
            order.setStatus("CANCELED");
            Order updatedOrder = orderRepository.save(order);
            if (updatedOrder != null) {
                notifyObservers(new EntityChangeEvent<>(EntityChangeEvent.Type.UPDATED, updatedOrder, oldOrder));
                LOGGER.info("Canceled order: " + orderId + " and notified observers" +
                        ("PAID".equals(oldOrder.getStatus()) ? " (restored stock quantities)" : ""));
                return true;
            } else {
                LOGGER.warning("Failed to save order cancellation for order: " + orderId);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error canceling order: " + orderId, e);
            return false;
        }
    }

    /**
     * Restore stock quantities for a canceled order
     * @param order The order to restore stock for
     * @return true if stock was restored successfully, false otherwise
     */
    private boolean restoreStockForOrder(Order order) {
        try {
            LOGGER.info("Restoring stock for order: " + order.getOrderId());

            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            boolean allRestored = true;

            for (OrderItem item : orderItems) {
                try {
                    Optional<HoneyProduct> productOpt = honeyProductService.findById(item.getProduct().getProductId());
                    if (productOpt.isPresent()) {
                        HoneyProduct product = productOpt.get();
                        Beekeeper productOwner = product.getApiary().getBeekeeper();

                        BigDecimal currentQuantity = product.getQuantity();
                        BigDecimal quantityToRestore = BigDecimal.valueOf(item.getQuantity());
                        BigDecimal newQuantity = currentQuantity.add(quantityToRestore);
                        HoneyProduct updatedProduct = honeyProductService.updateHoneyProduct(
                                product.getProductId(),
                                product.getName(),
                                product.getDescription(),
                                product.getPrice(),
                                newQuantity,
                                productOwner
                        );

                        if (updatedProduct != null) {
                            LOGGER.info("Restored " + quantityToRestore + " units to product: " +
                                    product.getName() + " (new total: " + newQuantity + ")");
                        } else {
                            LOGGER.warning("Failed to restore stock for product: " + product.getName());
                            allRestored = false;
                        }
                    } else {
                        LOGGER.warning("Product not found when restoring stock: " + item.getProduct().getProductId());
                        allRestored = false;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error restoring stock for item in order " +
                            order.getOrderId(), e);
                    allRestored = false;
                }
            }

            return allRestored;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error restoring stock for order: " + order.getOrderId(), e);
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
                return "DELIVERED".equals(newStatus) || "CANCELED".equals(newStatus);
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
            LOGGER.info("Finding orders for beekeeper: " + beekeeper.getUsername());

            // Get all orders
            List<Order> allOrders = orderRepository.findAll();
            LOGGER.info("Found " + allOrders.size() + " total orders in system");

            List<Order> beekeeperOrders = new ArrayList<>();
            for (Order order : allOrders) {
                try {
                    List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                    LOGGER.info("Order " + order.getOrderId() + " has " + orderItems.size() + " items");
                    boolean belongsToBeekeeper = orderItems.stream()
                            .anyMatch(item -> {
                                try {
                                    Apiary apiary = item.getProduct().getApiary();
                                    boolean matches = apiary.getBeekeeper().getUserId().equals(beekeeper.getUserId());
                                    LOGGER.info("Order item product: " + item.getProduct().getName() +
                                            ", Apiary: " + apiary.getName() +
                                            ", Beekeeper: " + apiary.getBeekeeper().getUsername() +
                                            ", Matches: " + matches);
                                    return matches;
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "Error checking order item for beekeeper match", e);
                                    return false;
                                }
                            });

                    if (belongsToBeekeeper) {
                        beekeeperOrders.add(order);
                        LOGGER.info("Order " + order.getOrderId() + " belongs to beekeeper " + beekeeper.getUsername());
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error processing order " + order.getOrderId() + " for beekeeper", e);
                }
            }

            LOGGER.info("Found " + beekeeperOrders.size() + " orders for beekeeper: " + beekeeper.getUsername());
            return beekeeperOrders;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders for beekeeper: " + beekeeper.getUsername(), e);
            return List.of();
        }
    }

    @Override
    public List<Order> findOrdersWithFilters(Beekeeper beekeeper, String status, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<Order> beekeeperOrders = findOrdersForBeekeeper(beekeeper);
            LOGGER.info("Applying filters to " + beekeeperOrders.size() + " orders");
            LOGGER.info("Filters - Status: " + status + ", Start Date: " + startDate + ", End Date: " + endDate);

            List<Order> filteredOrders = beekeeperOrders.stream()
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

            LOGGER.info("After filtering: " + filteredOrders.size() + " orders remain");
            return filteredOrders;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders with filters for beekeeper: " + beekeeper.getUsername(), e);
            return List.of();
        }
    }
}