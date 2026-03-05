package com.pro.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Run this ONCE locally to generate the bcrypt hash for the admin password.
 * Copy the printed hash and run the UPDATE statement in pgAdmin 4.
 *
 * Run with:  mvnw.cmd exec:java -Dexec.mainClass="com.pro.backend.GenerateAdminHash"
 */
public class GenerateAdminHash {
    public static void main(String[] args) {
        // ← Change this to your desired admin password before running
        String rawPassword = "prisha101Z#";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hash = encoder.encode(rawPassword);

        System.out.println("=== COPY THE SQL BELOW AND RUN IN pgAdmin 4 ===");
        System.out.println();
        System.out.println("UPDATE users");
        System.out.println("SET password_hash = '" + hash + "'");
        System.out.println("WHERE email = 'prosenjitkm91@gmail.com';");
        System.out.println();
        System.out.println("=== Hash for your reference: " + hash);
    }
}

