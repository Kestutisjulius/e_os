package eu.minted.eos.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderGenerator {
    public static void main(String[] args) {
        String originalPassword = "password";
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(originalPassword);

        System.out.println("Original: " + originalPassword);
        System.out.println("Encoded: " + encodedPassword);
    }
}
