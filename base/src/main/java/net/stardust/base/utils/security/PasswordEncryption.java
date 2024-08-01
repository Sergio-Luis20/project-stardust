package net.stardust.base.utils.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility class for generating salts and hashing
 * for passwords.
 * 
 * @author Sergio Luis
 */
public final class PasswordEncryption {

    /**
     * The default value for the salt array length
     * used in {@link #generateSalt()}.
     */
    public static final int DEFAULT_SALT_LENGTH = 16;

    /**
     * The default number of iterations used in
     * {@link #generateHash(String, byte[])}.
     */
    public static final int DEFAULT_ITERATION_COUNT = 10000;

    /**
     * The default password array length used in
     * {@link #generateHash(String, byte[])}.
     */
    public static final int DEFAULT_KEY_LENGTH = 256;

    /**
     * The default algorithm for password encryption used in
     * {@link #generateHash(String, byte[])}.
     */
    public static final String DEFAULT_ALGORITHM = "PBKDF2WithHmacSHA256";

    private PasswordEncryption() {
    }

    /**
     * Generates a byte array representing a salt
     * for using in a password hash. This method
     * uses a default internal length for the array.
     * 
     * @return an array with random bytes
     */
    public static byte[] generateSalt() {
        return generateSalt(DEFAULT_SALT_LENGTH);
    }

    /**
     * Works the same way as defined in {@link #generateSalt()},
     * but here you can set the length of the array.
     * 
     * @param length the array length
     * @return and array with random bytes
     */
    public static byte[] generateSalt(int length) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return salt;
    }
    
    /**
     * Generates a password hash, taking default values
     * for {@code iterationCount}, {@code keyLength} and
     * {@code algorithm}.
     * 
     * @param password the raw password string
     * @param salt the salt for hashing
     * @return the byte array representing the password hash
     * @throws PasswordException if some problem occurs during password encryption
     */
    public static byte[] generateHash(String password, byte[] salt) throws PasswordException {
        return generateHash(password, salt, DEFAULT_ITERATION_COUNT, DEFAULT_KEY_LENGTH, DEFAULT_ALGORITHM);
    }

    /**
     * Works as specified in {@link #generateHash(String, byte[])}, but
     * here you can set manually parameters for {@code iterationCount},
     * {@code keyLength} and {@code algorithm}.
     * 
     * @param password the raw password string
     * @param salt the salt for hashing
     * @param iterationCount the number of iterations over the password
     * @param keyLength the size of the generated hash
     * @param algorithm the algorithm to use for hashing
     * @return the byte array representing the password hash
     * @throws PasswordException if the algorithm could not be found or the
     * key specification using the other parameters results to be invalid; you
     * can use {@link Exception#getCause()} to see what was the problem
     */
    public static byte[] generateHash(String password, byte[] salt, int iterationCount, int keyLength,
            String algorithm) throws PasswordException {
        char[] passwordChars = password.toCharArray();
        try {
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, iterationCount, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return hash;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PasswordException("Could not generate password", e);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    /**
     * Returns a Set containing all algorithms that can be used
     * in {@link #generateHash(String, byte[], int, int, String)}.
     * 
     * @return a set of valid algorithms for password encryption
     */
    public static Set<String> getSupportedAlgorithms() {
        return Security.getAlgorithms("SecretKeyFactory");
    }

}
