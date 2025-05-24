package org.apiary.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "CartItem")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemId")
    private Integer itemId;

    @ManyToOne
    @JoinColumn(name = "cartId", nullable = false)
    private ShoppingCart cart;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private HoneyProduct product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Default constructor required by JPA
    public CartItem() {}

    public CartItem(ShoppingCart cart, HoneyProduct product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
    }

    // Getters and setters
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public ShoppingCart getCart() {
        return cart;
    }

    public void setCart(ShoppingCart cart) {
        this.cart = cart;
    }

    public HoneyProduct getProduct() {
        return product;
    }

    public void setProduct(HoneyProduct product) {
        this.product = product;
        if (product != null) {
            this.price = product.getPrice();
        }
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Calculate the subtotal for this cart item
     * @return The subtotal (price * quantity)
     */
    @Transient
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Update the quantity of this cart item
     * @param quantity The new quantity
     */
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(itemId, cartItem.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public String toString() {
        return quantity + " × " + product.getName();
    }
}