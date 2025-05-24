package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.Order;
import org.apiary.model.Payment;
import org.apiary.repository.interfaces.PaymentRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentRepositoryImpl extends AbstractRepository<Integer, Payment> implements PaymentRepository {

    private static final Logger LOGGER = Logger.getLogger(PaymentRepositoryImpl.class.getName());

    public PaymentRepositoryImpl() {
        super(Payment.class);
    }

    @Override
    public Payment findByOrder(Order order) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Payment> query = session.createQuery(
                    "FROM Payment WHERE order.id = :orderId", Payment.class);
            query.setParameter("orderId", order.getOrderId());
            return query.uniqueResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payment by order: " + order.getOrderId(), e);
            return null;
        }
    }

    @Override
    public List<Payment> findByStatus(String status) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Payment> query = session.createQuery(
                    "FROM Payment WHERE status = :status ORDER BY date DESC", Payment.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payments by status: " + status, e);
            return List.of();
        }
    }

    @Override
    public List<Payment> findByDateAfter(LocalDateTime date) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Payment> query = session.createQuery(
                    "FROM Payment WHERE date > :date ORDER BY date DESC", Payment.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payments by date after: " + date, e);
            return List.of();
        }
    }

    @Override
    public List<Payment> findByDateBefore(LocalDateTime date) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Payment> query = session.createQuery(
                    "FROM Payment WHERE date < :date ORDER BY date DESC", Payment.class);
            query.setParameter("date", date);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payments by date before: " + date, e);
            return List.of();
        }
    }

    @Override
    public List<Payment> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Payment> query = session.createQuery(
                    "FROM Payment WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC", Payment.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding payments by date between: "
                    + startDate + " and " + endDate, e);
            return List.of();
        }
    }
}