package org.apiary.service;

import org.apiary.service.interfaces.*;

/**
 * Aggregator class that holds all services for easy access
 */
public class AllServices {
    private final UserService userService;
    private final ApiaryService apiaryService;
    private final HiveService hiveService;
    private final HoneyProductService honeyProductService;
    private final ShoppingCartService shoppingCartService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public AllServices(UserService userService,
                       ApiaryService apiaryService,
                       HiveService hiveService,
                       HoneyProductService honeyProductService,
                       ShoppingCartService shoppingCartService,
                       OrderService orderService,
                       PaymentService paymentService) {
        this.userService = userService;
        this.apiaryService = apiaryService;
        this.hiveService = hiveService;
        this.honeyProductService = honeyProductService;
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    // Getters
    public UserService getUserService() {
        return userService;
    }

    public ApiaryService getApiaryService() {
        return apiaryService;
    }

    public HiveService getHiveService() {
        return hiveService;
    }

    public HoneyProductService getHoneyProductService() {
        return honeyProductService;
    }

    public ShoppingCartService getShoppingCartService() {
        return shoppingCartService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }
}