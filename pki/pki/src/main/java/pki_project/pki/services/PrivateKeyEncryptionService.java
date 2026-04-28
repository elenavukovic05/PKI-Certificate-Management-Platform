package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Service
public class PrivateKeyEncryptionService {

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;

    private final SecretKeySpec masterKey;

    public PrivateKeyEncryptionService(@Value("${app.crypto.master-key}") String base64MasterKey) {
        byte[] keyBytes = Base64.getDecoder().decode(base64MasterKey);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Master key must be 32 bytes for AES-256.");
        }

        this.masterKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encryptPrivateKey(PrivateKey privateKey) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    masterKey,
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv)
            );

            byte[] encryptedBytes = cipher.doFinal(privateKey.getEncoded());

            byte[] result = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, result, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt private key", e);
        }
    }

    public PrivateKey decryptPrivateKey(String encryptedPrivateKeyBase64) {
        try {
            byte[] allBytes = Base64.getDecoder().decode(encryptedPrivateKeyBase64);

            byte[] iv = Arrays.copyOfRange(allBytes, 0, IV_LENGTH);
            byte[] encryptedBytes = Arrays.copyOfRange(allBytes, IV_LENGTH, allBytes.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    masterKey,
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv)
            );

            byte[] privateKeyBytes = cipher.doFinal(encryptedBytes);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt private key", e);
        }
    }

    public String encryptData(byte[] data) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] encrypted = cipher.doFinal(data);
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public byte[] decryptData(String base64Data) {
        try {
            byte[] allBytes = Base64.getDecoder().decode(base64Data);
            byte[] iv = Arrays.copyOfRange(allBytes, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(allBytes, IV_LENGTH, allBytes.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
