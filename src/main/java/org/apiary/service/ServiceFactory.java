package org.apiary.service;

import org.apiary.service.interfaces.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.logging.Logger;

/**
 * Factory class for creating service instances using Spring XML configuration
 */
public class ServiceFactory {

    private static final Logger LOGGER = Logger.getLogger(ServiceFactory.class.getName());
    private static ApplicationContext applicationContext;
    private static AllServices allServices;

    // Static initialization block to set up Spring context
    static {
        try {
            LOGGER.info("Initializing Spring ApplicationContext from XML...");
            applicationContext = new ClassPathXmlApplicationContext("ApiaryConfig.xml");
            allServices = applicationContext.getBean("allServices", AllServices.class);
            LOGGER.info("Spring ApplicationContext initialized successfully from XML");

            // Log successful bean creation
            LOGGER.info("Successfully created " + applicationContext.getBeanDefinitionCount() + " beans");
            String[] beanNames = applicationContext.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                LOGGER.info("Bean created: " + beanName);
            }

        } catch (Exception e) {
            LOGGER.severe("Failed to initialize Spring ApplicationContext from XML: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Spring context from XML", e);
        }
    }

    private ServiceFactory() {
        // Private constructor to prevent instantiation
    }

    public static void logServiceInstances() {
        Logger logger = Logger.getLogger(ServiceFactory.class.getName());

        logger.info("=== SERVICE FACTORY INSTANCES (Spring XML DI) ===");
        logger.info("HoneyProductService: " + getHoneyProductService().getClass().getSimpleName() + "@" +
                Integer.toHexString(getHoneyProductService().hashCode()));
        logger.info("OrderService: " + getOrderService().getClass().getSimpleName() + "@" +
                Integer.toHexString(getOrderService().hashCode()));
        logger.info("ApiaryService: " + getApiaryService().getClass().getSimpleName() + "@" +
                Integer.toHexString(getApiaryService().hashCode()));

        // Check observer counts with detailed logging
        if (getHoneyProductService() instanceof org.apiary.utils.observer.EventManager) {
            org.apiary.utils.observer.EventManager<?> eventManager =
                    (org.apiary.utils.observer.EventManager<?>) getHoneyProductService();
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

        if (getOrderService() instanceof org.apiary.utils.observer.EventManager) {
            org.apiary.utils.observer.EventManager<?> eventManager =
                    (org.apiary.utils.observer.EventManager<?>) getOrderService();
            int observerCount = eventManager.countObservers();
            logger.info("OrderService has " + observerCount + " observers");

            if (observerCount == 0) {
                logger.warning("WARNING: OrderService has NO observers registered!");
            }
        }

        if (getApiaryService() instanceof org.apiary.utils.observer.EventManager) {
            org.apiary.utils.observer.EventManager<?> eventManager =
                    (org.apiary.utils.observer.EventManager<?>) getApiaryService();
            int observerCount = eventManager.countObservers();
            logger.info("ApiaryService has " + observerCount + " observers");

            if (observerCount == 0) {
                logger.warning("WARNING: ApiaryService has NO observers registered!");
            }
        }

        logger.info("=== END SERVICE FACTORY INSTANCES ===");
    }

    // Service getter methods
    public static HoneyProductService getHoneyProductService() {
        if (allServices == null) {
            throw new RuntimeException("Spring context not initialized");
        }
        LOGGER.fine("Returning HoneyProductService instance: " +
                allServices.getHoneyProductService().getClass().getSimpleName() + "@" +
                Integer.toHexString(allServices.getHoneyProductService().hashCode()));
        return allServices.getHoneyProductService();
    }

    public static UserService getUserService() {
        if (allServices == null) {
            throw new RuntimeException("Spring context not initialized");
        }
        return allServices.getUserService();
    }

    public static ApiaryService getApiaryService() {
        if (allServices == null) {
            throw new RuntimeException("Spring context not initialized");
        }
        return allServices.getApiaryService();
    }

    public static HiveService getHiveService() {
        if (allServices == null) {
            throw new RuntimeException("Spring context not initialized");
        }
        return allServices.getHiveService();
    }

    public static ShoppingCartService getShoppingCartService() {
        if (allServices == null) {
            throw new RuntimeException("Spring context not initialized");
        }
        return allServices.getShoppingCartService();
    }

    public static OrderService getOrderService() {
        if (allServices == null) {
            throw new RuntimeException("Spring context not initialized");
        }
        return allServices.getOrderService();
    }

    public static PaymentService getPaymentService() {
        if (allServices == null) {
            throw new RuntimeException("Spring context not initialized");
        }
        return allServices.getPaymentService();
    }

    /**
     * Get the Spring ApplicationContext for advanced usage
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Get a specific bean by name and type
     */
    public static <T> T getBean(String beanName, Class<T> requiredType) {
        return applicationContext.getBean(beanName, requiredType);
    }

    /**
     * Get a specific bean by type
     */
    public static <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    /**
     * Check if a bean exists
     */
    public static boolean containsBean(String beanName) {
        return applicationContext.containsBean(beanName);
    }

    /**
     * Shutdown the Spring context gracefully
     */
    public static void shutdown() {
        if (applicationContext instanceof ClassPathXmlApplicationContext) {
            ((ClassPathXmlApplicationContext) applicationContext).close();
            LOGGER.info("Spring ApplicationContext closed");
        }
    }

    /**
     * Refresh the Spring context (useful for testing)
     */
    public static void refresh() {
        if (applicationContext instanceof ClassPathXmlApplicationContext) {
            ((ClassPathXmlApplicationContext) applicationContext).refresh();
            allServices = applicationContext.getBean("allServices", AllServices.class);
            LOGGER.info("Spring ApplicationContext refreshed");
        }
    }
}