package com.emiLoan.EMILoan.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class PANHashingUtil {

    @Value("${app.pepper:emi-loan-default-pepper-2024}")
    private String pepper;

    private static final String ALGORITHM = "SHA-256";

    public String hash(String plainPAN) {
        try {
            String input = plainPAN.toUpperCase() + pepper;
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

            byte[] hashBytes = digest.digest(
                    input.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Security algorithm not found: " + ALGORITHM, e);
        }
    }

    public boolean verify(String plainPAN, String storedHash) {
        if (plainPAN == null || storedHash == null) return false;
        return hash(plainPAN).equals(storedHash);
    }

    public String mask(String first3, String last2) {
        return first3 + "*****" + last2;
    }

    public boolean isValidPAN(String pan) {
        return pan != null && pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]$");
    }

    public String extractFirst3(String pan) {
        return (pan != null && pan.length() >= 3) ? pan.substring(0, 3) : "";
    }

    public String extractLast2(String pan)  {
        return (pan != null && pan.length() == 10) ? pan.substring(8, 10) : "";
    }
}