package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.Client;
import org.apiary.model.Order;
import org.apiary.repository.interfaces.OrderRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderRepositoryImpl extends AbstractRepository<Integer, Order> implements OrderRepository {

    private static final Logger LOGGER = Logger.getLogger(OrderRepositoryImpl.class.getName());

    public OrderRepositoryImpl() {
        super(Order.class);
    }

    @Override
    public List<Order> findByClient(Client client) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "FROM Order WHERE client.id = :clientId ORDER BY date DESC", Order.class);
            query.setParameter("clientId", client.getUserId());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by client: " + client.getUserId(), e);
            return List.of();
        }
    }

    @Override
    public List<Order> findByStatus(String status) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "FROM Order WHERE status = :status ORDER BY date DESC", Order.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by status: " + status, e);
            return List.of();
        }
    }

    @Override
    public List<Order> findByDateAfter(LocalDateTime date) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "FROM Order WHERE date > :date ORDER BY date DESC", Order.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by date after: " + date, e);
            return List.of();
        }
    }

    @Override
    public List<Order> findByDateBefore(LocalDateTime date) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "FROM Order WHERE date < :date ORDER BY date DESC", Order.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by date before: " + date, e);
            return List.of();
        }
    }

    @Override
    public List<Order> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery(
                    "FROM Order WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC", Order.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding orders by date between: "
                    + startDate + " and " + endDate, e);
            return List.of();
        }
    }
}