package org.apiary.service.impl;

import org.apiary.model.CartItem;
import org.apiary.model.Client;
import org.apiary.model.HoneyProduct;
import org.apiary.model.ShoppingCart;
import org.apiary.repository.interfaces.CartItemRepository;
import org.apiary.repository.interfaces.HoneyProductRepository;
import org.apiary.repository.interfaces.ShoppingCartRepository;
import org.apiary.service.interfaces.ShoppingCartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final Logger LOGGER = Logger.getLogger(ShoppingCartServiceImpl.class.getName());
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final HoneyProductRepository honeyProductRepository;

    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository,
                                   CartItemRepository cartItemRepository,
                                   HoneyProductRepository honeyProductRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartItemRepository = cartItemRepository;
        this.honeyProductRepository = honeyProductRepository;
    }

    @Override
    public Optional<ShoppingCart> findByClient(Client client) {
        try {
            Optional<ShoppingCart> cartOpt = shoppingCartRepository.findByClient(client);
            if (cartOpt.isEmpty()) {
                // Create a new shopping cart if one doesn't exist
                ShoppingCart cart = new ShoppingCart(client);
                ShoppingCart savedCart = shoppingCartRepository.save(cart);
                return Optional.of(savedCart);
            }
            return cartOpt;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding shopping cart for client: " + client.getUsername(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<CartItem> getCartItems(Client client) {
        try {
            Optional<ShoppingCart> cartOpt = findByClient(client);
            if (cartOpt.isEmpty()) {
                LOGGER.info("No shopping cart found for client: " + client.getUsername());
                return List.of();
            }

            ShoppingCart cart = cartOpt.get();
            List<CartItem> items = cartItemRepository.findByCart(cart);

            LOGGER.info("Found " + items.size() + " items in cart for client: " + client.getUsername());
            for (CartItem item : items) {
                LOGGER.info("Cart item: " + item.getProduct().getName() +
                        " | Quantity: " + item.getQuantity() +
                        " | Price: " + item.getPrice() +
                        " | Item ID: " + item.getItemId() +
                        " | Product ID: " + item.getProduct().getProductId());
            }

            return items;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting cart items for client: " + client.getUsername(), e);
            return List.of();
        }
    }

    @Override
    public boolean addToCart(Client client, HoneyProduct product, int quantity) {
        try {
            // Validate product
            Optional<HoneyProduct> productOpt = honeyProductRepository.findById(product.getProductId());
            if (productOpt.isEmpty()) {
                LOGGER.warning("Product not found: " + product.getProductId());
                return false;
            }

            HoneyProduct actualProduct = productOpt.get();

            // Check if product has enough quantity
            if (actualProduct.getQuantity().compareTo(BigDecimal.valueOf(quantity)) < 0) {
                LOGGER.warning("Not enough quantity available for product: " + product.getProductId());
                return false;
            }

            // Get or create shopping cart
            Optional<ShoppingCart> cartOpt = findByClient(client);
            if (cartOpt.isEmpty()) {
                LOGGER.warning("Could not get or create shopping cart for client: " + client.getUsername());
                return false;
            }

            ShoppingCart cart = cartOpt.get();

            // Check if product already exists in cart
            Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProduct(cart, actualProduct);

            if (existingItemOpt.isPresent()) {
                // Update quantity
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                cartItemRepository.save(existingItem);
            } else {
                // Create new cart item
                CartItem newItem = new CartItem(cart, actualProduct, quantity);
                cartItemRepository.save(newItem);
            }

            LOGGER.info("Added product to cart: " + product.getProductId() +
                    " for client: " + client.getUsername());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding product to cart: " + product.getProductId() +
                    " for client: " + client.getUsername(), e);
            return false;
        }
    }

    @Override
    public boolean updateCartItemQuantity(Client client, Integer cartItemId, int quantity) {
        try {
            // Validate cart item
            Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
            if (itemOpt.isEmpty()) {
                LOGGER.warning("Cart item not found: " + cartItemId);
                return false;
            }

            CartItem item = itemOpt.get();

            // Check if item belongs to client
            Optional<ShoppingCart> cartOpt = findByClient(client);
            if (cartOpt.isEmpty() || !item.getCart().equals(cartOpt.get())) {
                LOGGER.warning("Cart item does not belong to client: " +
                        cartItemId + ", " + client.getUsername());
                return false;
            }

            // Check if product has enough quantity
            HoneyProduct product = item.getProduct();
            if (product.getQuantity().compareTo(BigDecimal.valueOf(quantity)) < 0) {
                LOGGER.warning("Not enough quantity available for product: " + product.getProductId());
                return false;
            }

            // Update quantity
            if (quantity <= 0) {
                // If quantity is 0 or negative, remove item from cart
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(quantity);
                cartItemRepository.save(item);
            }

            LOGGER.info("Updated cart item quantity: " + cartItemId +
                    " for client: " + client.getUsername());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating cart item quantity: " + cartItemId +
                    " for client: " + client.getUsername(), e);
            return false;
        }
    }

    @Override
    public boolean removeFromCart(Client client, Integer cartItemId) {
        try {
            // Validate cart item
            Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
            if (itemOpt.isEmpty()) {
                LOGGER.warning("Cart item not found: " + cartItemId);
                return false;
            }

            CartItem item = itemOpt.get();

            // Check if item belongs to client
            Optional<ShoppingCart> cartOpt = findByClient(client);
            if (cartOpt.isEmpty() || !item.getCart().equals(cartOpt.get())) {
                LOGGER.warning("Cart item does not belong to client: " +
                        cartItemId + ", " + client.getUsername());
                return false;
            }

            // Remove item
            cartItemRepository.delete(item);

            LOGGER.info("Removed cart item: " + cartItemId +
                    " for client: " + client.getUsername());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing cart item: " + cartItemId +
                    " for client: " + client.getUsername(), e);
            return false;
        }
    }

    @Override
    public boolean clearCart(Client client) {
        try {
            Optional<ShoppingCart> cartOpt = findByClient(client);
            if (cartOpt.isEmpty()) {
                LOGGER.warning("Shopping cart not found for client: " + client.getUsername());
                return false;
            }

            ShoppingCart cart = cartOpt.get();

            // Delete all cart items
            cartItemRepository.deleteByCart(cart);

            LOGGER.info("Cleared cart for client: " + client.getUsername());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error clearing cart for client: " + client.getUsername(), e);
            return false;
        }
    }

    @Override
    public BigDecimal calculateCartTotal(Client client) {
        try {
            List<CartItem> items = getCartItems(client);
            return items.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating cart total for client: " + client.getUsername(), e);
            return BigDecimal.ZERO;
        }
    }
}