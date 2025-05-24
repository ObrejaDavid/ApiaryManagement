package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.Client;
import org.apiary.model.ShoppingCart;
import org.apiary.repository.interfaces.ShoppingCartRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShoppingCartRepositoryImpl extends AbstractRepository<Integer, ShoppingCart> implements ShoppingCartRepository {

    private static final Logger LOGGER = Logger.getLogger(ShoppingCartRepositoryImpl.class.getName());

    public ShoppingCartRepositoryImpl() {
        super(ShoppingCart.class);
    }

    @Override
    public Optional<ShoppingCart> findByClient(Client client) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<ShoppingCart> query = session.createQuery(
                    "FROM ShoppingCart WHERE client.id = :clientId", ShoppingCart.class);
            query.setParameter("clientId", client.getUserId());
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding shopping cart by client: " + client.getUserId(), e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteByClient(Client client) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Query query = session.createQuery(
                    "DELETE FROM ShoppingCart WHERE client.id = :clientId");
            query.setParameter("clientId", client.getUserId());
            query.executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting shopping cart by client: " + client.getUserId(), e);
        }
    }
}