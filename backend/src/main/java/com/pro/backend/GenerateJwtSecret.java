package com.pro.backend;

import java.security.SecureRandom;

/**
 * One-off utility — run with:
 *   .\mvnw.cmd exec:java -Dexec.mainClass=com.pro.backend.GenerateJwtSecret
 *
 * Prints a cryptographically secure 256-bit (32-byte) hex string
 * suitable for use as JWT_SECRET in your .env file.
 */
public class GenerateJwtSecret {

    public static void main(String[] args) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        StringBuilder hex = new StringBuilder(64);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }

        System.out.println("=================================================");
        System.out.println("  Generated JWT_SECRET (256-bit, hex-encoded):");
        System.out.println("  " + hex);
        System.out.println("=================================================");
        System.out.println("  Copy the value above into your .env file:");
        System.out.println("  JWT_SECRET=" + hex);
        System.out.println("=================================================");
    }
}

