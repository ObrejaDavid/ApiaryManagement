package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.Apiary;
import org.apiary.model.Hive;
import org.apiary.repository.interfaces.HiveRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiveRepositoryImpl extends AbstractRepository<Integer, Hive> implements HiveRepository {

    private static final Logger LOGGER = Logger.getLogger(HiveRepositoryImpl.class.getName());

    public HiveRepositoryImpl() {
        super(Hive.class);
    }

    @Override
    public List<Hive> findByApiary(Apiary apiary) {
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
    public List<Hive> findByApiaryAndHiveNumber(Apiary apiary, Integer hiveNumber) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Hive> query = session.createQuery(
                    "FROM Hive WHERE apiary.id = :apiaryId AND hiveNumber = :hiveNumber", Hive.class);
            query.setParameter("apiaryId", apiary.getApiaryId());
            query.setParameter("hiveNumber", hiveNumber);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding hives by apiary and hive number: "
                    + apiary.getApiaryId() + ", " + hiveNumber, e);
            return List.of();
        }
    }

    @Override
    public List<Hive> findByQueenYear(Integer queenYear) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Hive> query = session.createQuery(
                    "FROM Hive WHERE queenYear = :queenYear", Hive.class);
            query.setParameter("queenYear", queenYear);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding hives by queen year: " + queenYear, e);
            return List.of();
        }
    }

    @Override
    public long countByApiary(Apiary apiary) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM Hive WHERE apiary.id = :apiaryId", Long.class);
            query.setParameter("apiaryId", apiary.getApiaryId());
            return query.uniqueResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting hives by apiary: " + apiary.getApiaryId(), e);
            return 0;
        }
    }
}