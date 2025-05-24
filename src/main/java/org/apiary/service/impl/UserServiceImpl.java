package org.apiary.service.impl;

import org.apiary.model.Beekeeper;
import org.apiary.model.Client;
import org.apiary.model.User;
import org.apiary.repository.interfaces.UserRepository;
import org.apiary.service.interfaces.UserService;
import org.apiary.utils.PasswordUtils;
import org.apiary.utils.StringUtils;
import org.apiary.utils.ValidationUtils;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface with input validation
 */
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    private final UserRepository userRepository;

    /**
     * Create a new user service
     * @param userRepository The user repository
     */
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean authenticate(String username, String password) {
        try {
            // Validate input
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                LOGGER.warning("Authentication attempt with empty username or password");
                return false;
            }

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return PasswordUtils.verifyPassword(password, user.getPassword());
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user: " + username, e);
            return false;
        }
    }

    @Override
    public Client registerClient(String username, String password, String fullName,
                                 String email, String address, String phone) {
        try {
            // Validate username
            if (!ValidationUtils.isValidUsername(username)) {
                LOGGER.warning("Invalid username format: " + username);
                throw new IllegalArgumentException("Username must be 3-20 characters and contain only letters, numbers, underscores, and hyphens");
            }

            // Check if username already exists
            if (userRepository.usernameExists(username)) {
                LOGGER.warning("Username already exists: " + username);
                throw new IllegalArgumentException("Username already exists");
            }

            // Validate password
            if (!ValidationUtils.isValidPassword(password)) {
                LOGGER.warning("Invalid password format for user: " + username);
                throw new IllegalArgumentException("Password must be at least 8 characters and include uppercase, lowercase, number, and special character");
            }

            // Validate email if provided
            if (!StringUtils.isBlank(email) && !ValidationUtils.isValidEmail(email)) {
                LOGGER.warning("Invalid email format: " + email);
                throw new IllegalArgumentException("Invalid email format");
            }

            // Validate phone if provided
            if (!StringUtils.isBlank(phone) && !ValidationUtils.isValidPhone(phone)) {
                LOGGER.warning("Invalid phone format: " + phone);
                throw new IllegalArgumentException("Invalid phone format");
            }

            // Create new client
            Client client = new Client(username, PasswordUtils.hashPassword(password));
            client.setFullName(fullName);
            client.setEmail(email);
            client.setAddress(address);
            client.setPhone(phone);

            // Initialize shopping cart
            client.initializeShoppingCart();

            // Save client
            Client savedClient = (Client) userRepository.save(client);
            LOGGER.info("Registered new client: " + username);
            return savedClient;
        } catch (IllegalArgumentException e) {
            LOGGER.warning(e.getMessage());
            throw e; // Re-throw for controller to handle appropriately
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering client: " + username, e);
            throw new RuntimeException("Error registering client: " + e.getMessage(), e);
        }
    }

    @Override
    public Beekeeper registerBeekeeper(String username, String password, String phone,
                                       String address, Integer yearsOfExperience) {
        try {
            // Validate username
            if (!ValidationUtils.isValidUsername(username)) {
                LOGGER.warning("Invalid username format: " + username);
                throw new IllegalArgumentException("Username must be 3-20 characters and contain only letters, numbers, underscores, and hyphens");
            }

            // Check if username already exists
            if (userRepository.usernameExists(username)) {
                LOGGER.warning("Username already exists: " + username);
                throw new IllegalArgumentException("Username already exists");
            }

            // Validate password
            if (!ValidationUtils.isValidPassword(password)) {
                LOGGER.warning("Invalid password format for user: " + username);
                throw new IllegalArgumentException("Password must be at least 8 characters and include uppercase, lowercase, number, and special character");
            }

            // Validate phone if provided
            if (!StringUtils.isBlank(phone) && !ValidationUtils.isValidPhone(phone)) {
                LOGGER.warning("Invalid phone format: " + phone);
                throw new IllegalArgumentException("Invalid phone format");
            }

            // Validate years of experience if provided
            if (yearsOfExperience != null && yearsOfExperience < 0) {
                LOGGER.warning("Invalid years of experience: " + yearsOfExperience);
                throw new IllegalArgumentException("Years of experience cannot be negative");
            }

            // Create new beekeeper
            Beekeeper beekeeper = new Beekeeper(username, PasswordUtils.hashPassword(password));
            beekeeper.setPhone(phone);
            beekeeper.setAddress(address);
            beekeeper.setYearsOfExperience(yearsOfExperience);

            // Save beekeeper
            Beekeeper savedBeekeeper = (Beekeeper) userRepository.save(beekeeper);
            LOGGER.info("Registered new beekeeper: " + username);
            return savedBeekeeper;
        } catch (IllegalArgumentException e) {
            LOGGER.warning(e.getMessage());
            throw e; // Re-throw for controller to handle appropriately
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error registering beekeeper: " + username, e);
            throw new RuntimeException("Error registering beekeeper: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            if (StringUtils.isBlank(username)) {
                LOGGER.warning("Attempt to find user with empty username");
                return Optional.empty();
            }

            return userRepository.findByUsername(username);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Client> findClientByUsername(String username) {
        try {
            if (StringUtils.isBlank(username)) {
                LOGGER.warning("Attempt to find client with empty username");
                return Optional.empty();
            }

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && userOpt.get() instanceof Client) {
                return Optional.of((Client) userOpt.get());
            }
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding client by username: " + username, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Beekeeper> findBeekeeperByUsername(String username) {
        try {
            if (StringUtils.isBlank(username)) {
                LOGGER.warning("Attempt to find beekeeper with empty username");
                return Optional.empty();
            }

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && userOpt.get() instanceof Beekeeper) {
                return Optional.of((Beekeeper) userOpt.get());
            }
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding beekeeper by username: " + username, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Client> findAllClients() {
        try {
            return userRepository.findAll().stream()
                    .filter(user -> user instanceof Client)
                    .map(user -> (Client) user)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all clients", e);
            return List.of();
        }
    }

    @Override
    public List<Beekeeper> findAllBeekeepers() {
        try {
            return userRepository.findAll().stream()
                    .filter(user -> user instanceof Beekeeper)
                    .map(user -> (Beekeeper) user)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all beekeepers", e);
            return List.of();
        }
    }

    @Override
    public User updateProfile(User user) {
        try {
            // Validate user
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            // Get existing user
            Optional<User> existingUserOpt = userRepository.findById(user.getUserId());
            if (existingUserOpt.isEmpty()) {
                throw new IllegalArgumentException("User not found");
            }

            User existingUser = existingUserOpt.get();

            // Validate client-specific fields
            if (user instanceof Client) {
                Client client = (Client) user;

                // Validate email if changed
                String email = client.getEmail();
                if (!StringUtils.isBlank(email) && !ValidationUtils.isValidEmail(email)) {
                    throw new IllegalArgumentException("Invalid email format");
                }

                // Validate phone if changed
                String phone = client.getPhone();
                if (!StringUtils.isBlank(phone) && !ValidationUtils.isValidPhone(phone)) {
                    throw new IllegalArgumentException("Invalid phone format");
                }
            }

            // Validate beekeeper-specific fields
            if (user instanceof Beekeeper) {
                Beekeeper beekeeper = (Beekeeper) user;

                // Validate phone if changed
                String phone = beekeeper.getPhone();
                if (!StringUtils.isBlank(phone) && !ValidationUtils.isValidPhone(phone)) {
                    throw new IllegalArgumentException("Invalid phone format");
                }

                // Validate years of experience if changed
                Integer yearsOfExperience = beekeeper.getYearsOfExperience();
                if (yearsOfExperience != null && yearsOfExperience < 0) {
                    throw new IllegalArgumentException("Years of experience cannot be negative");
                }
            }

            // Prevent username change (could be allowed with additional validation)
            user.setUsername(existingUser.getUsername());

            // Prevent password change (should be done through changePassword method)
            user.setPassword(existingUser.getPassword());

            User updatedUser = userRepository.save(user);
            LOGGER.info("Updated user profile: " + user.getUsername());
            return updatedUser;
        } catch (IllegalArgumentException e) {
            LOGGER.warning(e.getMessage());
            throw e; // Re-throw for controller to handle appropriately
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating user profile: " +
                    (user != null ? user.getUsername() : "null"), e);
            throw new RuntimeException("Error updating user profile: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            // Validate input
            if (StringUtils.isBlank(username)) {
                throw new IllegalArgumentException("Username cannot be empty");
            }

            if (StringUtils.isBlank(oldPassword)) {
                throw new IllegalArgumentException("Current password cannot be empty");
            }

            if (StringUtils.isBlank(newPassword)) {
                throw new IllegalArgumentException("New password cannot be empty");
            }

            // Validate new password
            if (!ValidationUtils.isValidPassword(newPassword)) {
                throw new IllegalArgumentException("Password must be at least 8 characters and include uppercase, lowercase, number, and special character");
            }

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                LOGGER.warning("User not found for password change: " + username);
                return false;
            }

            User user = userOpt.get();

            // Verify old password
            if (!PasswordUtils.verifyPassword(oldPassword, user.getPassword())) {
                LOGGER.warning("Invalid current password for user: " + username);
                return false;
            }

            // Set new password
            user.setPassword(PasswordUtils.hashPassword(newPassword));
            userRepository.save(user);
            LOGGER.info("Password changed for user: " + username);
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.warning(e.getMessage());
            throw e; // Re-throw for controller to handle appropriately
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error changing password for user: " + username, e);
            return false;
        }
    }

    @Override
    public boolean deleteUser(String username) {
        try {
            if (StringUtils.isBlank(username)) {
                LOGGER.warning("Attempt to delete user with empty username");
                return false;
            }

            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                LOGGER.warning("User not found for deletion: " + username);
                return false;
            }

            User user = userOpt.get();
            userRepository.delete(user);
            LOGGER.info("Deleted user: " + username);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting user: " + username, e);
            return false;
        }
    }
}