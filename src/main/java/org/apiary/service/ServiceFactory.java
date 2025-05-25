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

    // src/main/java/org/apiary/service/ServiceFactory.java
// Replace the existing logServiceInstances method

    public static void logServiceInstances() {
        Logger logger = Logger.getLogger(ServiceFactory.class.getName());

        logger.info("=== SERVICE FACTORY INSTANCES ===");
        logger.info("HoneyProductService: " + honeyProductService.getClass().getSimpleName() + "@" +
                Integer.toHexString(honeyProductService.hashCode()));
        logger.info("OrderService: " + orderService.getClass().getSimpleName() + "@" +
                Integer.toHexString(orderService.hashCode()));
        logger.info("ApiaryService: " + apiaryService.getClass().getSimpleName() + "@" +
                Integer.toHexString(apiaryService.hashCode()));

        // Check observer counts with detailed logging
        if (honeyProductService instanceof org.apiary.utils.observer.EventManager) {
            org.apiary.utils.observer.EventManager<?> eventManager =
                    (org.apiary.utils.observer.EventManager<?>) honeyProductService;
            int observerCount = eventManager.countObservers();
            logger.info("HoneyProductService has " + observerCount + " observers");

            if (observerCount == 0) {
                logger.warning("WARNING: HoneyProductService has NO observers registered!");
            } else if (observerCount == 1) {
                logger.warning("WARNING: HoneyProductService has only 1 observer. Expected 2 (Client + Beekeeper)");
            } else {
                logger.info("GOOD: HoneyProductService has multiple observers registered");
            }
        }

        if (orderService instanceof org.apiary.utils.observer.EventManager) {
            org.apiary.utils.observer.EventManager<?> eventManager =
                    (org.apiary.utils.observer.EventManager<?>) orderService;
            int observerCount = eventManager.countObservers();
            logger.info("OrderService has " + observerCount + " observers");

            if (observerCount == 0) {
                logger.warning("WARNING: OrderService has NO observers registered!");
            }
        }

        if (apiaryService instanceof org.apiary.utils.observer.EventManager) {
            org.apiary.utils.observer.EventManager<?> eventManager =
                    (org.apiary.utils.observer.EventManager<?>) apiaryService;
            int observerCount = eventManager.countObservers();
            logger.info("ApiaryService has " + observerCount + " observers");

            if (observerCount == 0) {
                logger.warning("WARNING: ApiaryService has NO observers registered!");
            }
        }

        logger.info("=== END SERVICE FACTORY INSTANCES ===");
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