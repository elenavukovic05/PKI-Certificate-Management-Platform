package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pki_project.pki.model.Organization;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Service
public class KeyManagementService {

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey masterKey;

    // master key from .env
    public KeyManagementService(@Value("${app.crypto.master-key}") String base64MasterKey) {
        byte[] keyBytes = Base64.getDecoder().decode(base64MasterKey);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("master key must be 32 bytes for AES-256.");
        }
        this.masterKey = new SecretKeySpec(keyBytes, "AES");
    }

    public SecretKey generateOrganizationKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE, new SecureRandom());
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("failed to generate organization key", e);
        }
    }

    // encrypt org key via master key
    public String encryptOrganizationKey(SecretKey orgKey) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(orgKey.getEncoded());

            byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt organization key", e);
        }
    }

    // decrypt org key from db
    public SecretKey decryptOrganizationKey(String encryptedOrgKeyBase64) {
        try {
            byte[] all = Base64.getDecoder().decode(encryptedOrgKeyBase64);
            byte[] iv = Arrays.copyOfRange(all, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(all, GCM_IV_LENGTH, all.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] keyBytes = cipher.doFinal(encrypted);

            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("failed to decrypt organization key", e);
        }
    }


    public String encryptPrivateKey(PrivateKey privateKey, Organization organization) {
        // 1. decrypt org key
        SecretKey orgKey = decryptOrganizationKey(organization.getEncryptedOrgKey());

        // 2. encrypt private key
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, orgKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(privateKey.getEncoded());

            byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("failed to encrypt private key with org key", e);
        }
    }

    public PrivateKey decryptPrivateKey(String encryptedPrivateKeyBase64, Organization organization) {
        // 1.  decrypt org key
        SecretKey orgKey = decryptOrganizationKey(organization.getEncryptedOrgKey());

        // 2. decrypt private key
        try {
            byte[] all = Base64.getDecoder().decode(encryptedPrivateKeyBase64);
            byte[] iv = Arrays.copyOfRange(all, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(all, GCM_IV_LENGTH, all.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, orgKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] privateKeyBytes = cipher.doFinal(encrypted);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("failed to decrypt private key with org key", e);
        }
    }
}