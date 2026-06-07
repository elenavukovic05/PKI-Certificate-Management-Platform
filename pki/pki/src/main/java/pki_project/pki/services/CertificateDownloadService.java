package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import pki_project.pki.model.CertificateEntity;
import pki_project.pki.model.enums.CertificateType;
import pki_project.pki.model.enums.Role;
import pki_project.pki.repositories.CertificateRepository;
import pki_project.pki.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Service
public class CertificateDownloadService {
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private KeyManagementService keyManagementService;

    public String downloadAsPem(Long certificateId) {
        CertificateEntity cert = findCertificate(certificateId);
        return cert.getCertificatePem();
    }

    public byte[] downloadAsCer(Long certificateId) {
        CertificateEntity cert = findCertificate(certificateId);
        return pemToDer(cert.getCertificatePem());
    }

    private byte[] pemToDer(String certificatePem) {
        try {
            String base64 = certificatePem
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");
            return Base64.getDecoder().decode(base64);
        } catch (Exception e) {
            throw new RuntimeException("failed to convert PEM to DER", e);
        }
    }

    private CertificateEntity findCertificate(Long certificateId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        CertificateEntity certificateEntity = certificateRepository.findById(certificateId).orElseThrow(() -> new RuntimeException("Certificate not found"));
        Role role = currentUser.getRole();

        switch (role) {
            case ADMIN:
                break;
            case CA_USER:
                if (certificateEntity.getOrganization() == null ||
                    !certificateEntity.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
                    throw new RuntimeException("certificate belongs to a different organization");
                }
                break;
            case USER:
                if (certificateEntity.getType() != CertificateType.END_ENTITY) {
                    throw new RuntimeException("users can only download end-entity certificates");
                }
                if (certificateEntity.getOwner() == null || !certificateEntity.getOwner().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("this certificate does not belong to you");
                }
                break;
            default:
                throw new RuntimeException("access denied: unknown role");
        }
        return certificateEntity;
    }

    public byte[] downloadAsPkcs12(Long id, String password){
        CertificateEntity certificateEntity = findCertificate(id);
        if (certificateEntity.getEncryptedPrivateKey() == null) {
            throw new RuntimeException("Private key not available for this certificate");
        }

        PrivateKey privateKey = keyManagementService.decryptPrivateKey(
                certificateEntity.getEncryptedPrivateKey(), certificateEntity.getOrganization());

        String pem = certificateEntity.getCertificatePem();

        try {
            String cleanPem = pem
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = java.util.Base64.getDecoder().decode(cleanPem);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate x509 = (X509Certificate) certificateFactory.generateCertificate(
                    new ByteArrayInputStream(decoded)
            );

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);

            keyStore.setKeyEntry(
                    "certificate",
                    privateKey,
                    password.toCharArray(),
                    new Certificate[]{x509}
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            keyStore.store(outputStream, password.toCharArray());
            return outputStream.toByteArray();
        } catch (Exception e){
            throw new RuntimeException("Failed to create PKCS12", e);
        }
    }

}
