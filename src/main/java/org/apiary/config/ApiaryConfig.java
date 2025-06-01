package org.apiary.config;

import org.apiary.repository.impl.*;
import org.apiary.repository.interfaces.*;
import org.apiary.service.AllServices;
import org.apiary.service.impl.*;
import org.apiary.service.interfaces.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Properties;

@Configuration
public class ApiaryConfig {

    @Bean
    public Properties getDatabaseProperties() {
        Properties properties = new Properties();
        try {
            // Load from classpath (src/main/resources)
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
        } catch(IOException e) {
            System.err.println("Failed to load application.properties: " + e.getMessage());
            throw new RuntimeException("Configuration file application.properties not found", e);
        }
        return properties;
    }
    // Repository
    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl();
    }

    @Bean
    public ApiaryRepository apiaryRepository() {
        return new ApiaryRepositoryImpl();
    }

    @Bean
    public HiveRepository hiveRepository() {
        return new HiveRepositoryImpl();
    }

    @Bean
    public HoneyProductRepository honeyProductRepository() {
        return new HoneyProductRepositoryImpl();
    }

    @Bean
    public ShoppingCartRepository shoppingCartRepository() {
        return new ShoppingCartRepositoryImpl();
    }

    @Bean
    public CartItemRepository cartItemRepository() {
        return new CartItemRepositoryImpl();
    }

    @Bean
    public OrderRepository orderRepository() {
        return new OrderRepositoryImpl();
    }

    @Bean
    public OrderItemRepository orderItemRepository() {
        return new OrderItemRepositoryImpl();
    }

    @Bean
    public PaymentRepository paymentRepository() {
        return new PaymentRepositoryImpl();
    }

    // Service
    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    @Bean
    public ApiaryService apiaryService(ApiaryRepository apiaryRepository,
                                       HoneyProductRepository honeyProductRepository,
                                       OrderItemRepository orderItemRepository,
                                       CartItemRepository cartItemRepository) {
        return new ApiaryServiceImpl(apiaryRepository, honeyProductRepository,
                orderItemRepository, cartItemRepository);
    }

    @Bean
    public HiveService hiveService(HiveRepository hiveRepository, ApiaryService apiaryService) {
        return new HiveServiceImpl(hiveRepository, apiaryService);
    }

    @Bean
    public HoneyProductService honeyProductService(HoneyProductRepository honeyProductRepository,
                                                   ApiaryService apiaryService,
                                                   HiveService hiveService) {
        return new HoneyProductServiceImpl(honeyProductRepository, apiaryService, hiveService);
    }

    @Bean
    public ShoppingCartService shoppingCartService(ShoppingCartRepository shoppingCartRepository,
                                                   CartItemRepository cartItemRepository,
                                                   HoneyProductRepository honeyProductRepository) {
        return new ShoppingCartServiceImpl(shoppingCartRepository, cartItemRepository, honeyProductRepository);
    }

    @Bean
    public PaymentService paymentService(PaymentRepository paymentRepository) {
        return new PaymentServiceImpl(paymentRepository);
    }

    @Bean
    public OrderService orderService(OrderRepository orderRepository,
                                     OrderItemRepository orderItemRepository,
                                     ShoppingCartService shoppingCartService,
                                     PaymentService paymentService,
                                     HoneyProductService honeyProductService,
                                     HoneyProductRepository honeyProductRepository) {
        return new OrderServiceImpl(orderRepository, orderItemRepository, shoppingCartService,
                paymentService, honeyProductService, honeyProductRepository);
    }

    @Bean
    public AllServices allServices(UserService userService,
                                   ApiaryService apiaryService,
                                   HiveService hiveService,
                                   HoneyProductService honeyProductService,
                                   ShoppingCartService shoppingCartService,
                                   OrderService orderService,
                                   PaymentService paymentService) {
        return new AllServices(userService, apiaryService, hiveService, honeyProductService,
                shoppingCartService, orderService, paymentService);
    }
}