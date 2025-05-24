package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.Apiary;
import org.apiary.model.Beekeeper;
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
}