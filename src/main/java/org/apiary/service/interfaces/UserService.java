package org.apiary.service.interfaces;

import org.apiary.model.Beekeeper;
import org.apiary.model.Client;
import org.apiary.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    /**
     * Authenticate a user with username and password
     * @param username The username
     * @param password The password
     * @return true if authentication is successful, false otherwise
     */
    boolean authenticate(String username, String password);

    /**
     * Register a new client
     * @param username The username
     * @param password The password
     * @param fullName The full name
     * @param email The email
     * @param address The address
     * @param phone The phone number
     * @return The registered client, or null if registration failed
     */
    Client registerClient(String username, String password, String fullName,
                          String email, String address, String phone);

    /**
     * Register a new beekeeper
     * @param username The username
     * @param password The password
     * @param phone The phone number
     * @param address The address
     * @param yearsOfExperience The years of experience
     * @return The registered beekeeper, or null if registration failed
     */
    Beekeeper registerBeekeeper(String username, String password, String phone,
                                String address, Integer yearsOfExperience);

    /**
     * Find a user by username
     * @param username The username
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a client by username
     * @param username The username
     * @return An Optional containing the client if found, empty otherwise
     */
    Optional<Client> findClientByUsername(String username);

    /**
     * Find a beekeeper by username
     * @param username The username
     * @return An Optional containing the beekeeper if found, empty otherwise
     */
    Optional<Beekeeper> findBeekeeperByUsername(String username);

    /**
     * Find all clients
     * @return A list of all clients
     */
    List<Client> findAllClients();

    /**
     * Find all beekeepers
     * @return A list of all beekeepers
     */
    List<Beekeeper> findAllBeekeepers();

    /**
     * Update a user's profile
     * @param user The user to update
     * @return The updated user
     */
    User updateProfile(User user);

    /**
     * Change a user's password
     * @param username The username
     * @param oldPassword The old password
     * @param newPassword The new password
     * @return true if the password was changed successfully, false otherwise
     */
    boolean changePassword(String username, String oldPassword, String newPassword);

    /**
     * Delete a user
     * @param username The username
     * @return true if the user was deleted successfully, false otherwise
     */
    boolean deleteUser(String username);

    /**
     * Verify a user's password
     * @param username The username
     * @param password The password to verify
     * @return true if the password is correct, false otherwise
     */
    boolean verifyPassword(String username, String password);

    /**
     * Update user profile
     * @param userId The user ID
     * @param fullName The full name
     * @param email The email
     * @param phone The phone number
     * @param newPassword The new password (optional)
     * @return The updated user
     */
    User updateUserProfile(Integer userId, String fullName, String email, String phone, String newPassword);
}