package org.apiary.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for securely hashing and verifying passwords
 */
public class PasswordUtils {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtils.class.getName());

    // Algorithm parameters
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    // Delimiter for storing salt:hash
    private static final String DELIMITER = ":";

    // Prevent instantiation
    private PasswordUtils() {
    }

    /**
     * Hash a password using PBKDF2 with a random salt
     *
     * @param password The plain text password to hash
     * @return A Base64 encoded string containing the salt and hash, separated by a colon
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            byte[] salt = generateSalt();

            // Hash the password
            byte[] hash = hashPassword(password.toCharArray(), salt);

            // Encode salt and hash as Base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            // Return salt:hash
            return saltBase64 + DELIMITER + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Error hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify a password against a stored hash
     *
     * @param password    The plain text password to verify
     * @param storedHash  The stored hash (salt:hash)
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Split the stored hash into salt and hash
            String[] parts = storedHash.split(DELIMITER);
            if (parts.length != 2) {
                return false;
            }

            // Decode salt and hash from Base64
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);

            // Hash the password with the same salt
            byte[] testHash = hashPassword(password.toCharArray(), salt);

            // Compare the hashes
            return Arrays.equals(hash, testHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Error verifying password", e);
            return false;
        }
    }

    /**
     * Generate a random salt
     *
     * @return A random salt
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hash a password with a salt using PBKDF2
     *
     * @param password The password to hash
     * @param salt     The salt
     * @return The hashed password
     * @throws NoSuchAlgorithmException If the algorithm is not available
     * @throws InvalidKeySpecException  If the key specification is invalid
     */
    private static byte[] hashPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }
}