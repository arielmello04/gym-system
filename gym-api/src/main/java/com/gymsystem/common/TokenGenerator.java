// src/main/java/com/gymsystem/common/TokenGenerator.java
package com.gymsystem.common;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class responsible for generating secure, URL-safe tokens.
 */
public final class TokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom(); // Creates a static SecureRandom instance

    private TokenGenerator() {
        // empty
    }

    /**
     * Generates a secure random token encoded with URL-safe Base64.
     * @param byteLength number of random bytes to generate (e.g., 32 or 48 for stronger tokens)
     * @return a URL-safe Base64 encoded string without padding
     */
    public static String generateUrlSafeToken(int byteLength) {
        byte[] bytes = new byte[byteLength]; // Allocates a byte array with the requested length
        RANDOM.nextBytes(bytes); // Fills the array with secure random bytes
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); // Encodes
    }
}
