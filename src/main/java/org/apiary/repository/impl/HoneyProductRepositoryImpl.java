package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.Apiary;
import org.apiary.model.Hive;
import org.apiary.model.HoneyProduct;
import org.apiary.repository.interfaces.HoneyProductRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HoneyProductRepositoryImpl extends AbstractRepository<Integer, HoneyProduct> implements HoneyProductRepository {

    private static final Logger LOGGER = Logger.getLogger(HoneyProductRepositoryImpl.class.getName());

    public HoneyProductRepositoryImpl() {
        super(HoneyProduct.class);
    }

    @Override
    public List<HoneyProduct> findByApiary(Apiary apiary) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<HoneyProduct> query = session.createQuery(
                    "FROM HoneyProduct WHERE apiary.id = :apiaryId", HoneyProduct.class);
            query.setParameter("apiaryId", apiary.getApiaryId());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by apiary: " + apiary.getApiaryId(), e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByHive(Hive hive) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<HoneyProduct> query = session.createQuery(
                    "FROM HoneyProduct WHERE hive.id = :hiveId", HoneyProduct.class);
            query.setParameter("hiveId", hive.getHiveId());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by hive: " + hive.getHiveId(), e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByNameContaining(String name) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<HoneyProduct> query = session.createQuery(
                    "FROM HoneyProduct WHERE name LIKE :name", HoneyProduct.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by name: " + name, e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByPriceLessThan(BigDecimal price) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<HoneyProduct> query = session.createQuery(
                    "FROM HoneyProduct WHERE price < :price", HoneyProduct.class);
            query.setParameter("price", price);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by price less than: " + price, e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByPriceGreaterThan(BigDecimal price) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<HoneyProduct> query = session.createQuery(
                    "FROM HoneyProduct WHERE price > :price", HoneyProduct.class);
            query.setParameter("price", price);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by price greater than: " + price, e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<HoneyProduct> query = session.createQuery(
                    "FROM HoneyProduct WHERE price BETWEEN :minPrice AND :maxPrice", HoneyProduct.class);
            query.setParameter("minPrice", minPrice);
            query.setParameter("maxPrice", maxPrice);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding honey products by price between: "
                    + minPrice + " and " + maxPrice, e);
            return List.of();
        }
    }

    @Override
    public List<HoneyProduct> findAvailableProducts() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<HoneyProduct> query = session.createQuery(
                    "FROM HoneyProduct WHERE quantity > 0", HoneyProduct.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding available honey products", e);
            return List.of();
        }
    }
}