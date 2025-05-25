package org.apiary.service;

import org.apiary.repository.RepositoryFactory;
import org.apiary.service.impl.*;
import org.apiary.service.interfaces.*;

import java.util.logging.Logger;

/**
 * Factory class for creating service instances
 */
public class ServiceFactory {

    private static final Logger LOGGER = Logger.getLogger(ServiceFactory.class.getName());

    // Services
    private static final UserService userService = new UserServiceImpl(
            RepositoryFactory.getUserRepository());

    private static final ApiaryService apiaryService = new ApiaryServiceImpl(
            RepositoryFactory.getApiaryRepository());

    private static final HiveService hiveService = new HiveServiceImpl(
            RepositoryFactory.getHiveRepository(),
            apiaryService);

    private static final HoneyProductService honeyProductService = new HoneyProductServiceImpl(
            RepositoryFactory.getHoneyProductRepository(),
            apiaryService,
            hiveService);

    private static final ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl(
            RepositoryFactory.getShoppingCartRepository(),
            RepositoryFactory.getCartItemRepository(),
            RepositoryFactory.getHoneyProductRepository());

    private static final PaymentService paymentService = new PaymentServiceImpl(
            RepositoryFactory.getPaymentRepository());

    private static final OrderService orderService = new OrderServiceImpl(
            RepositoryFactory.getOrderRepository(),
            RepositoryFactory.getOrderItemRepository(),
            shoppingCartService,
            paymentService,
            honeyProductService,
            RepositoryFactory.getHoneyProductRepository());

    private ServiceFactory() {
        // Private constructor to prevent instantiation
    }

    public static HoneyProductService getHoneyProductService() {
        LOGGER.info("Returning HoneyProductService instance: " +
                honeyProductService.getClass().getSimpleName() + "@" +
                Integer.toHexString(honeyProductService.hashCode()));
        return honeyProductService;
    }

    public static UserService getUserService() {
        return userService;
    }

    public static ApiaryService getApiaryService() {
        return apiaryService;
    }

    public static HiveService getHiveService() {
        return hiveService;
    }

    public static ShoppingCartService getShoppingCartService() {
        return shoppingCartService;
    }

    public static OrderService getOrderService() {
        return orderService;
    }

    public static PaymentService getPaymentService() {
        return paymentService;
    }
}