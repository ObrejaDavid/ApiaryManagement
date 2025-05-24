package org.apiary.repository;

import org.apiary.repository.impl.*;
import org.apiary.repository.interfaces.*;

/**
 * Factory class for creating repository instances
 */
public class RepositoryFactory {

    private static final UserRepository userRepository = new UserRepositoryImpl();
    private static final ApiaryRepository apiaryRepository = new ApiaryRepositoryImpl();
    private static final HiveRepository hiveRepository = new HiveRepositoryImpl();
    private static final HoneyProductRepository honeyProductRepository = new HoneyProductRepositoryImpl();
    private static final ShoppingCartRepository shoppingCartRepository = new ShoppingCartRepositoryImpl();
    private static final CartItemRepository cartItemRepository = new CartItemRepositoryImpl();
    private static final OrderRepository orderRepository = new OrderRepositoryImpl();
    private static final OrderItemRepository orderItemRepository = new OrderItemRepositoryImpl();
    private static final PaymentRepository paymentRepository = new PaymentRepositoryImpl();

    private RepositoryFactory() {
        // Private constructor to prevent instantiation
    }

    public static UserRepository getUserRepository() {
        return userRepository;
    }

    public static ApiaryRepository getApiaryRepository() {
        return apiaryRepository;
    }

    public static HiveRepository getHiveRepository() {
        return hiveRepository;
    }

    public static HoneyProductRepository getHoneyProductRepository() {
        return honeyProductRepository;
    }

    public static ShoppingCartRepository getShoppingCartRepository() {
        return shoppingCartRepository;
    }

    public static CartItemRepository getCartItemRepository() {
        return cartItemRepository;
    }

    public static OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public static OrderItemRepository getOrderItemRepository() {
        return orderItemRepository;
    }

    public static PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }
}