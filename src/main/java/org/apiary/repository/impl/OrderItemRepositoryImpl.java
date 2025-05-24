package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.HoneyProduct;
import org.apiary.model.Order;
import org.apiary.model.OrderItem;
import org.apiary.repository.interfaces.OrderItemRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderItemRepositoryImpl extends AbstractRepository<Integer, OrderItem> implements OrderItemRepository {

    private static final Logger LOGGER = Logger.getLogger(OrderItemRepositoryImpl.class.getName());

    public OrderItemRepositoryImpl() {
        super(OrderItem.class);
    }

    @Override
    public List<OrderItem> findByOrder(Order order) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<OrderItem> query = session.createQuery(
                    "FROM OrderItem WHERE order.id = :orderId", OrderItem.class);
            query.setParameter("orderId", order.getOrderId());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding order items by order: " + order.getOrderId(), e);
            return List.of();
        }
    }

    @Override
    public List<OrderItem> findByProduct(HoneyProduct product) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<OrderItem> query = session.createQuery(
                    "FROM OrderItem WHERE product.id = :productId", OrderItem.class);
            query.setParameter("productId", product.getProductId());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding order items by product: " + product.getProductId(), e);
            return List.of();
        }
    }

    @Override
    public void deleteByOrder(Order order) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query query = session.createQuery(
                    "DELETE FROM OrderItem WHERE order.id = :orderId");
            query.setParameter("orderId", order.getOrderId());
            query.executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting order items by order: " + order.getOrderId(), e);
        }
    }
}