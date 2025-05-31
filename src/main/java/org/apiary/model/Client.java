package org.apiary.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a client user who can browse and purchase honey products
 */
@Entity
@DiscriminatorValue("CLIENT")
public class Client extends User {

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShoppingCart shoppingCart;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @Column(name = "fullName")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    public Client() {}

    public Client(String username, String password) {
        super(username, password);
        this.shoppingCart = new ShoppingCart(this);
    }

    public Client(String username, String password, String fullName, String email, String address, String phone) {
        super(username, password);
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.shoppingCart = new ShoppingCart(this);
    }

    // Getters and setters
    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
        if (shoppingCart != null) {
            shoppingCart.setClient(this);
        }
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Initialize the shopping cart if it doesn't exist
     */
    public void initializeShoppingCart() {
        if (shoppingCart == null) {
            shoppingCart = new ShoppingCart(this);
        }
    }

    /**
     * View honey products (implemented in service layer)
     */
    public void viewHoneyProducts() {
        // Implementation will be in service layer
    }

    /**
     * Add a product to the shopping cart
     * @param product The product to add
     * @param quantity The quantity to add
     */
    public void addToCart(HoneyProduct product, int quantity) {
        initializeShoppingCart();
        shoppingCart.addItem(product, quantity);
    }

    /**
     * Create an order from the current shopping cart
     * @return The created order, or null if the cart is empty
     */
    public Order confirmOrder() {
        if (shoppingCart == null || shoppingCart.isEmpty()) {
            return null;
        }

        Order order = new Order(this);

        // Convert cart items to order items
        for (CartItem cartItem : shoppingCart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    order,
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getPrice()
            );
            order.addItem(orderItem);
        }

        // Clear the cart
        shoppingCart.clear();

        // Add the order to the client's orders
        orders.add(order);

        return order;
    }

    @Override
    public String toString() {
        return "Client: " + getUsername() + (fullName != null ? " (" + fullName + ")" : "");
    }
}