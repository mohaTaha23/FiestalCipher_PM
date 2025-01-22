package com.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class FeistelCipher {

    private static final int NUM_ROUNDS = 32;
    private static final int BLOCK_SIZE = 8; // Block size in bytes

    public static String encrypt(String plaintext, String key) {
        // Apply padding to the plaintext
        byte[] paddedPlaintext = pad(plaintext.getBytes(StandardCharsets.UTF_8));

        // Convert padded plaintext to binary
        String binaryPlaintext = toBinaryString(paddedPlaintext);

        // Split the binary plaintext into two halves
        String left = binaryPlaintext.substring(0, binaryPlaintext.length() / 2);
        String right = binaryPlaintext.substring(binaryPlaintext.length() / 2);

        // Perform the Feistel rounds
        for (int round = 0; round < NUM_ROUNDS; round++) {
            String newRight = xor(left, feistelFunction(right, round, key));
            left = right;
            right = newRight;
        }

        // Combine the left and right halves to get the final ciphertext
        return fromBinaryString(left + right);
    }

    public static String decrypt(String ciphertext, String key) {
        // Convert ciphertext to binary
        String binaryCiphertext = toBinaryString(ciphertext);

        // Split the binary ciphertext into two halves
        String left = binaryCiphertext.substring(0, binaryCiphertext.length() / 2);
        String right = binaryCiphertext.substring(binaryCiphertext.length() / 2);

        // Perform the Feistel rounds in reverse order
        for (int round = NUM_ROUNDS - 1; round >= 0; round--) {
            String newLeft = xor(right, feistelFunction(left, round, key));
            right = left;
            left = newLeft;
        }

        // Combine the left and right halves to get the final plaintext
        byte[] decryptedBytes = fromBinaryString(left + right).getBytes(StandardCharsets.UTF_8);

        // Remove padding from the decrypted bytes
        byte[] unpaddedBytes = unpad(decryptedBytes);

        return new String(unpaddedBytes, StandardCharsets.UTF_8);
    }

    private static String feistelFunction(String half, int round, String key) {
        try {
            // Generate a round key using the main key and the round number
            byte[] roundKey = generateRoundKey(round, key);

            // Apply HMAC-SHA-256 to the half using the round key
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(roundKey, "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(half.getBytes(StandardCharsets.UTF_8));

            // Convert the hash to a binary string
            return toBinaryString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("HmacSHA256 not available", e);
        } catch (Exception e) {
            throw new RuntimeException("Error in Feistel function", e);
        }
    }

    private static byte[] generateRoundKey(int round, String key) {
        try {
            // Concatenate the main key with the round number
            String roundKeyInput = key + round;

            // Hash the round key input to get a fixed-length round key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(roundKeyInput.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static String xor(String str1, String str2) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str1.length(); i++) {
            result.append(str1.charAt(i) ^ str2.charAt(i % str2.length()));
        }
        return result.toString();
    }

    private static String toBinaryString(String input) {
        StringBuilder binaryString = new StringBuilder();
        for (char c : input.toCharArray()) {
            binaryString.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binaryString.toString();
    }

    private static String toBinaryString(byte[] input) {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : input) {
            binaryString.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return binaryString.toString();
    }

    private static String fromBinaryString(String binaryString) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i += 8) {
            int charCode = Integer.parseInt(binaryString.substring(i, i + 8), 2);
            output.append((char) charCode);
        }
        return output.toString();
    }

    private static byte[] pad(byte[] input) {
        int paddingLength = BLOCK_SIZE - (input.length % BLOCK_SIZE);
        byte[] padded = Arrays.copyOf(input, input.length + paddingLength);
        Arrays.fill(padded, input.length, padded.length, (byte) paddingLength);
        return padded;
    }

    private static byte[] unpad(byte[] input) {
        int paddingLength = input[input.length - 1];
        return Arrays.copyOf(input, input.length - paddingLength);
    }
}