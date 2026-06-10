package pki_project.pki.services;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pki_project.pki.model.MasterKeyRotation;
import pki_project.pki.model.Organization;
import pki_project.pki.repositories.MasterKeyRotationRepository;
import pki_project.pki.repositories.OrganizationRepository;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class KeyManagementService {
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    MasterKeyRotationRepository rotationRepository;

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey baseKey;

    // base key from .env
    public KeyManagementService(@Value("${app.crypto.base-key}") String base64BaseKey) {
        byte[] keyBytes = Base64.getDecoder().decode(base64BaseKey);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("base key must be 32 bytes for AES-256.");
        }
        this.baseKey = new SecretKeySpec(keyBytes, "AES");
    }

    @PostConstruct
    public void initializeRotatingKey() {
        if (rotationRepository.findById(1L).isEmpty()) {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(AES_KEY_SIZE, new SecureRandom());
                SecretKey initialRotatingKey = keyGen.generateKey();

                MasterKeyRotation record = new MasterKeyRotation();
                record.setId(1L);
                record.setEncryptedKey(encryptWithKey(baseKey, initialRotatingKey));
                record.setLastRotatedAt(new Date());
                record.setRotationIntervalDays(180);
                rotationRepository.save(record);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to initialize rotating key", e);
            }
        }
    }

    private SecretKey getRotatingKey() {
        MasterKeyRotation record = rotationRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Rotating key not found"));
        return decryptWithKey(baseKey, record.getEncryptedKey());
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
        return encryptWithKey(getRotatingKey(), orgKey);
    }

    // decrypt org key from db
    public SecretKey decryptOrganizationKey(String encryptedOrgKeyBase64) {
        return decryptWithKey(getRotatingKey(), encryptedOrgKeyBase64);
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
            throw new RuntimeException("failed to decrypt private key with org key" + e.getMessage(), e);
        }
    }

    public String encryptText(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getRotatingKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("failed to encrypt 2FA secret", e);
        }
    }

    public String decryptText(String encryptedTextBase64) {
        try {
            byte[] all = Base64.getDecoder().decode(encryptedTextBase64);
            byte[] iv = Arrays.copyOfRange(all, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(all, GCM_IV_LENGTH, all.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getRotatingKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plainBytes = cipher.doFinal(encrypted);

            return new String(plainBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("failed to decrypt 2FA secret: " + e.getMessage(), e);
        }
    }

    public String encryptWithKey(SecretKey key, SecretKey encryptedKey) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(encryptedKey.getEncoded());

            byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt key", e);
        }
    }

    public SecretKey decryptWithKey(SecretKey key, String encryptedKeyBase64) {
        try {
            byte[] all = Base64.getDecoder().decode(encryptedKeyBase64);
            byte[] iv = Arrays.copyOfRange(all, 0, GCM_IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(all, GCM_IV_LENGTH, all.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] keyBytes = cipher.doFinal(encrypted);

            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("failed to decrypt key" + e.getMessage(), e);
        }
    }

    public boolean canRotate() {
        MasterKeyRotation rotation = rotationRepository.findById(1L).orElse(null);
        if (rotation == null) return true; // nikad nije rotiran

        long daysSinceLastRotation = (new Date().getTime() - rotation.getLastRotatedAt().getTime())
                / (1000 * 60 * 60 * 24);
        return daysSinceLastRotation >= rotation.getRotationIntervalDays();
    }

    public int getDaysUntilNextRotation() {
        MasterKeyRotation rotation = rotationRepository.findById(1L).orElse(null);
        if (rotation == null) return 0;

        long daysSinceLastRotation = (new Date().getTime() - rotation.getLastRotatedAt().getTime())
                / (1000 * 60 * 60 * 24);
        return (int) Math.max(0, rotation.getRotationIntervalDays() - daysSinceLastRotation);
    }

    @Transactional
    public String rotateMasterKey(boolean force){
        if (!force && !canRotate()) {
            throw new RuntimeException("Rotation not allowed yet. Days until next rotation: "
                    + getDaysUntilNextRotation());
        }

        try{
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE, new SecureRandom());
            SecretKey newMasterKey = keyGen.generateKey();

            List<Organization> organizations = organizationRepository.findAll();
            for(Organization organization : organizations){
                SecretKey orgKey = decryptWithKey(getRotatingKey(), organization.getEncryptedOrgKey());
                String newEncryptedOrgKey = encryptWithKey(newMasterKey, orgKey);
                organization.setEncryptedOrgKey(newEncryptedOrgKey);
            }
            organizationRepository.saveAll(organizations);

            MasterKeyRotation rotation = rotationRepository.findById(1L)
                    .orElse(new MasterKeyRotation(new Date(), 180));
            rotation.setLastRotatedAt(new Date());
            rotation.setEncryptedKey(
                    encryptWithKey(baseKey, newMasterKey)
            );
            rotationRepository.save(rotation);

            return "Master key rotation successful";
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("Failed to rotate master key", e);
        }

    }
}