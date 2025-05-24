package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.CartItem;
import org.apiary.model.HoneyProduct;
import org.apiary.model.ShoppingCart;
import org.apiary.repository.interfaces.CartItemRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CartItemRepositoryImpl extends AbstractRepository<Integer, CartItem> implements CartItemRepository {

    private static final Logger LOGGER = Logger.getLogger(CartItemRepositoryImpl.class.getName());

    public CartItemRepositoryImpl() {
        super(CartItem.class);
    }

    @Override
    public List<CartItem> findByCart(ShoppingCart cart) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<CartItem> query = session.createQuery(
                    "FROM CartItem WHERE cart.id = :cartId", CartItem.class);
            query.setParameter("cartId", cart.getCartId());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding cart items by cart: " + cart.getCartId(), e);
            return List.of();
        }
    }

    @Override
    public Optional<CartItem> findByCartAndProduct(ShoppingCart cart, HoneyProduct product) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<CartItem> query = session.createQuery(
                    "FROM CartItem WHERE cart.id = :cartId AND product.id = :productId", CartItem.class);
            query.setParameter("cartId", cart.getCartId());
            query.setParameter("productId", product.getProductId());
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding cart item by cart and product: "
                    + cart.getCartId() + ", " + product.getProductId(), e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteByCart(ShoppingCart cart) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query query = session.createQuery(
                    "DELETE FROM CartItem WHERE cart.id = :cartId");
            query.setParameter("cartId", cart.getCartId());
            query.executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting cart items by cart: " + cart.getCartId(), e);
        }
    }
}