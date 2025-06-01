package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
import org.apiary.model.Hive;
import org.apiary.repository.interfaces.ApiaryRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApiaryRepositoryImpl extends AbstractRepository<Integer, Apiary> implements ApiaryRepository {

    private static final Logger LOGGER = Logger.getLogger(ApiaryRepositoryImpl.class.getName());

    public ApiaryRepositoryImpl() {
        super(Apiary.class);
    }

    @Override
    public List<Apiary> findByBeekeeper(Beekeeper beekeeper) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Apiary> query = session.createQuery(
                    "FROM Apiary WHERE beekeeper.id = :beekeeperId", Apiary.class);
            query.setParameter("beekeeperId", beekeeper.getUserId());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding apiaries by beekeeper: " + beekeeper.getUserId(), e);
            return List.of();
        }
    }

    @Override
    public List<Apiary> findByNameContaining(String name) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Apiary> query = session.createQuery(
                    "FROM Apiary WHERE name LIKE :name", Apiary.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding apiaries by name: " + name, e);
            return List.of();
        }
    }

    @Override
    public List<Apiary> findByLocationContaining(String location) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Apiary> query = session.createQuery(
                    "FROM Apiary WHERE location LIKE :location", Apiary.class);
            query.setParameter("location", "%" + location + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding apiaries by location: " + location, e);
            return List.of();
        }
    }

    @Override
    public List<Hive> findHivesByApiary(Apiary apiary) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Hive> query = session.createQuery(
                    "FROM Hive WHERE apiary.id = :apiaryId ORDER BY hiveNumber", Hive.class);
            query.setParameter("apiaryId", apiary.getApiaryId());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding hives by apiary: " + apiary.getApiaryId(), e);
            return List.of();
        }
    }

    @Override
    public List<Hive> findHivesByApiaryId(Integer apiaryId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Hive> query = session.createQuery(
                    "FROM Hive WHERE apiary.id = :apiaryId ORDER BY hiveNumber", Hive.class);
            query.setParameter("apiaryId", apiaryId);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding hives by apiary ID: " + apiaryId, e);
            return List.of();
        }
    }

    @Override
    public long countHivesByApiary(Apiary apiary) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM Hive WHERE apiary.id = :apiaryId", Long.class);
            query.setParameter("apiaryId", apiary.getApiaryId());
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting hives by apiary: " + apiary.getApiaryId(), e);
            return 0L;
        }
    }

    @Override
    public long countHivesByApiaryId(Integer apiaryId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM Hive WHERE apiary.id = :apiaryId", Long.class);
            query.setParameter("apiaryId", apiaryId);
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting hives by apiary ID: " + apiaryId, e);
            return 0L;
        }
    }
}