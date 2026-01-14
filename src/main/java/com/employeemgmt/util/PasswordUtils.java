package com.employeemgmt.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 with salt for secure password storage
 * 
 * @author Team 6
 */
public class PasswordUtils {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final String SEPARATOR = ":";
    
    /**
     * Hash a password with a random salt
     * @param password Plain text password
     * @return Hashed password with salt (format: salt:hash)
     */
    public static String hashPassword(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash password with salt
            String hash = hashPasswordWithSalt(password, salt);
            
            // Return salt and hash combined
            return Base64.getEncoder().encodeToString(salt) + SEPARATOR + hash;
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a stored hash
     * @param password Plain text password to verify
     * @param storedHash Stored hash (format: salt:hash)
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            if (storedHash == null || !storedHash.contains(SEPARATOR)) {
                return false;
            }
            
            // Split salt and hash
            String[] parts = storedHash.split(SEPARATOR, 2);
            if (parts.length != 2) {
                return false;
            }
            
            // Decode salt
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String expectedHash = parts[1];
            
            // Hash input password with same salt
            String actualHash = hashPasswordWithSalt(password, salt);
            
            // Compare hashes (use constant-time comparison to prevent timing attacks)
            return constantTimeEquals(expectedHash, actualHash);
            
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Hash password with provided salt
     * @param password Plain text password
     * @param salt Salt bytes
     * @return Base64 encoded hash
     * @throws NoSuchAlgorithmException if algorithm not available
     */
    private static String hashPasswordWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        
        // Add salt to hash
        md.update(salt);
        
        // Hash password
        byte[] hashedPassword = md.digest(password.getBytes());
        
        // Return Base64 encoded hash
        return Base64.getEncoder().encodeToString(hashedPassword);
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks
     * @param a First string
     * @param b Second string
     * @return true if strings are equal
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }
    
    /**
     * Generate a random password
     * @param length Length of password to generate
     * @return Random password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }
        
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = uppercase + lowercase + digits + special;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        
        // Fill remaining length with random characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }
    
    /**
     * Shuffle characters in a string
     * @param input Input string
     * @return Shuffled string
     */
    private static String shuffleString(String input) {
        char[] array = input.toCharArray();
        SecureRandom random = new SecureRandom();
        
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        
        return new String(array);
    }
    
    /**
     * Check if password meets minimum requirements
     * @param password Password to check
     * @return true if password meets requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            
            if (hasUpper && hasLower && hasDigit) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calculate password strength score (0-4)
     * @param password Password to evaluate
     * @return Strength score (0=very weak, 4=very strong)
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety
        if (password.matches(".*[A-Z].*")) score++; // Uppercase
        if (password.matches(".*[a-z].*")) score++; // Lowercase
        if (password.matches(".*\\d.*")) score++; // Digit
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) score++; // Special char
        
        // Cap at 4
        return Math.min(score, 4);
    }
    
    /**
     * Get password strength description
     * @param password Password to evaluate
     * @return Strength description
     */
    public static String getPasswordStrengthDescription(String password) {
        int strength = calculatePasswordStrength(password);
        
        switch (strength) {
            case 0:
            case 1:
                return "Very Weak";
            case 2:
                return "Weak";
            case 3:
                return "Good";
            case 4:
                return "Strong";
            default:
                return "Unknown";
        }
    }
}