package org.apiary.repository.impl;

import org.apiary.config.HibernateConfig;
import org.apiary.model.User;
import org.apiary.repository.interfaces.UserRepository;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRepositoryImpl extends AbstractRepository<Integer, User> implements UserRepository {

    private static final Logger LOGGER = Logger.getLogger(UserRepositoryImpl.class.getName());

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE username = :username", User.class);
            query.setParameter("username", username);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean usernameExists(String username) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM User WHERE username = :username", Long.class);
            query.setParameter("username", username);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if username exists: " + username, e);
            return false;
        }
    }
}