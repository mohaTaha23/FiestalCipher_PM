package com.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordManagerAuth {

    // Directory to store user files
    private static final String USER_DATA_DIR = "users";
    // this map contains the passwords for each user and each user contains a map
    // for his/her passwords
    private Map<String, Map<String, String>> userPasswords = new HashMap<>();

    public PasswordManagerAuth() {
        // this will initialize the directory to store users files (each user will have
        // a file with their data)
        File dir = new File(USER_DATA_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    // Register a new user
    public void register(String username, String masterPassword) throws IOException, NoSuchAlgorithmException {
        // Check if user already exists
        // users/Alice.data (File.separator = / on Unix, \ on Windows)
        File userFile = new File(USER_DATA_DIR + File.separator + username + ".data");
        if (userFile.exists()) {
            // ! later on this will be a popup
            System.out.println("User already exists.");
            return;
        }

        // ? The reason behind using salt is even if two users have the same password,
        // ? their derived keys will be different

        byte[] salt = generateSalt();
        // deriving key with salt & masterPassword ensuring different hashes even when
        // users register
        // with the same password
        String derivedKey = deriveKey(masterPassword, salt);

        // Save user data (salt and derived key) to file
        // FileOutputStream is used to write raw bytes to a file
        // DataOutputStream is used to write primitive data types to a file (int,
        // byte[], UTF strings)
        try (FileOutputStream fos = new FileOutputStream(userFile);
                DataOutputStream dos = new DataOutputStream(fos)) {
            // we write the primitives and then it will be written to the file in raw bytes

            // write the length of the salt so you know how many bytes to read when reading
            // the salt
            dos.writeInt(salt.length);
            // write the actual salt byte array
            dos.write(salt);
            // write the derived key as a UTF string
            dos.writeUTF(derivedKey);
        }

        System.out.println("User registered successfully.");
    }

    // Login an existing user
    public boolean login(String username, String masterPassword) throws IOException, NoSuchAlgorithmException {
        // Check if user file exists
        File userFile = new File(USER_DATA_DIR + File.separator + username + ".data");
        if (!userFile.exists()) {
            System.out.println("User does not exist.");
            return false;
        }

        // Read salt and stored key from file
        byte[] salt;
        String storedKey;
        try (FileInputStream fis = new FileInputStream(userFile);
                DataInputStream dis = new DataInputStream(fis)) {
            // we read the salt length and initialize the salt array with the length
            int saltLength = dis.readInt();
            salt = new byte[saltLength];
            // then we read the actual salt bytes into the array
            dis.readFully(salt);
            // then we read the stored key as a UTF string
            storedKey = dis.readUTF();
        }

        // Derive key from the entered master password
        String derivedKey = deriveKey(masterPassword, salt);

        if (derivedKey.equals(storedKey)) {
            loadUserPasswords(username);
            return true;
        } else {
            return false;
        }
    }

    // Generate a random salt to be added to the masterPassword
    // e.g., [12, 34, 56, 78, 90, 12, 34, 56, 78, 90, 12, 34, 56, 78, 90, 12]
    private byte[] generateSalt() {
        // SecureRandom is a cryptographically strong random number generator
        SecureRandom random = new SecureRandom();
        // 16 bytes = 128 bits (good balance between security and performance)
        // the more salt size the better (it will be hard for the attacker to perform a
        // brute force attack/rainbow table attack)
        // longer salt size = require more space, more time to compute. So we keep
        // balance by using 16 bytes
        byte[] salt = new byte[16];

        // here we use the nextBytes method to fill the salt array with random bytes
        random.nextBytes(salt);
        return salt;
    }

    // Derive a key from the master password and salt
    // e.g: "aGVsbG93b3JsZDEyMzQ1Njc4OTA="
    private String deriveKey(String password, byte[] salt) throws NoSuchAlgorithmException {
        // SHA-256 is a hash function that produces a 256-bit (32-byte) hash value
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // here we update the digest with the salt to make it unique for each user
        md.update(salt);

        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        // convert the hash array to a string format using Base64 encoding for easier
        // storage
        return Base64.getEncoder().encodeToString(hash);
    }

    // the file contnetn will be like this
    /*
     * 16
     * [12, 34, 56, 78, 90, 12, 34, 56, 78, 90, 12, 34, 56, 78, 90, 12]
     * "aGVsbG93b3JsZDEyMzQ1Njc4OTA="
     */

    // load user passwords from file
    private void loadUserPasswords(String username) throws IOException {
        File userFile = new File(USER_DATA_DIR + File.separator + username + "_passwords.data");
        // to store the passwords of the current user
        Map<String, String> passwords = new HashMap<>();
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {

                        String decryptedPassword = FeistelCipher.decrypt(parts[1], getKey(username));

                        passwords.put(parts[0], decryptedPassword);
                    }
                }
            }
        }
        userPasswords.put(username, passwords);
    }

    private void saveUserPasswords(String username, String key) throws IOException {
        File userFile = new File(USER_DATA_DIR + File.separator + username + "_passwords.data");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile))) {
            Map<String, String> passwords = userPasswords.get(username);
            for (Map.Entry<String, String> entry : passwords.entrySet()) {
                String encryptedPassword = FeistelCipher.encrypt(entry.getValue(), key);
                writer.write(entry.getKey() + "," + encryptedPassword);
                writer.newLine();
            }
        }
    }

    // the reason behind saving the passwords even with one password added is the
    // fact that we are using files to store the passwords to store them and because
    // of that we use the map to keep track of all the passwords and update the file
    // upon updaing the map
    public void addPassword(String username, String account, String password) {
        userPasswords.computeIfAbsent(username, k -> new HashMap<>()).put(account, password);
        try {
            saveUserPasswords(username, getKey(username));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPasswords(String username) {
        Map<String, String> passwords = userPasswords.get(username);
        List<String> passwordList = new ArrayList<>();
        if (passwords != null) {
            for (Map.Entry<String, String> entry : passwords.entrySet()) {
                // the entry contains the account name (key) and the password (value)
                passwordList.add(entry.getKey() + ": " + entry.getValue());
            }
        }
        return passwordList;
    }

    public void updatePassword(String username, String account, String newPassword) {
        userPasswords.computeIfAbsent(username, k -> new HashMap<>()).put(account, newPassword);
        try {
            saveUserPasswords(username, getKey(username));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deletePassword(String username, String account) {
        Map<String, String> passwords = userPasswords.get(username);
        if (passwords != null) {
            passwords.remove(account);
            try {
                saveUserPasswords(username, getKey(username));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getKey(String username) throws IOException {
        File userFile = new File(USER_DATA_DIR + File.separator + username + ".data");
        if (!userFile.exists()) {
            throw new FileNotFoundException("User file not found");
        }

        byte[] salt;
        String storedKey;
        try (FileInputStream fis = new FileInputStream(userFile);
                DataInputStream dis = new DataInputStream(fis)) {
            int saltLength = dis.readInt();
            salt = new byte[saltLength];
            dis.readFully(salt);
            storedKey = dis.readUTF();
        }

        return storedKey;
    }

}
