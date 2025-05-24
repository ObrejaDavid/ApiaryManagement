package org.apiary.repository.interfaces;

import org.apiary.model.User;
import java.util.Optional;

public interface UserRepository extends Repository<Integer, User> {
    /**
     * Find a user by username
     * @param username The username to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a username exists
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    boolean usernameExists(String username);
}