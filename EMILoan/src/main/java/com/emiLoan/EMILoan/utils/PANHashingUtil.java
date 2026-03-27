package com.emiLoan.EMILoan.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class PANHashingUtil {

    @Value("${app.pepper}")
    private String pepper;

    public String hash(String plainPAN) throws NoSuchAlgorithmException {
        String input = plainPAN + pepper;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(
                input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }

    public boolean verify(String plainPAN, String storedHash)
            throws NoSuchAlgorithmException {
        return hash(plainPAN).equals(storedHash);
    }

    public String mask(String first3, String last2) {
        return first3 + "*****" + last2;
    }

    public boolean isValidPAN(String pan) {
        return pan != null && pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]$");
    }

    public String extractFirst3(String pan) { return pan.substring(0, 3); }
    public String extractLast2(String pan)  { return pan.substring(8, 10); }
}