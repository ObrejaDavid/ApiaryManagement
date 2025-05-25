package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.repository.interfaces.Repository;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of the Repository interface with common CRUD operations
 * @param <ID> The type of the entity's identifier
 * @param <T> The type of the entity
 */
public abstract class AbstractRepository<ID, T> implements Repository<ID, T> {

    private static final Logger LOGGER = Logger.getLogger(AbstractRepository.class.getName());
    private final Class<T> entityClass;

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(entityClass, (Serializable) id));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding entity by ID: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            Root<T> root = cq.from(entityClass);
            cq.select(root);
            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all entities", e);
            return List.of();
        }
    }

    @Override
    public T save(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            LOGGER.info("Opening database session for save operation");
            transaction = session.beginTransaction();
            LOGGER.info("Transaction started");

            session.saveOrUpdate(entity);
            LOGGER.info("Entity saveOrUpdate called");

            transaction.commit();
            LOGGER.info("Transaction committed successfully");

            return entity;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in save operation", e);
            if (transaction != null) {
                try {
                    transaction.rollback();
                    LOGGER.info("Transaction rolled back");
                } catch (Exception rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", rollbackEx);
                }
            }
            return null;
        }
    }

    @Override
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting entity", e);
        }
    }

    @Override
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
}