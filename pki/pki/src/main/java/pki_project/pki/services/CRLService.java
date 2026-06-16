package pki_project.pki.services;

import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v2CRLBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.RevokeCertificateRequestDTO;
import pki_project.pki.model.CRL;
import pki_project.pki.model.CertificateEntity;
import pki_project.pki.repositories.CRLRepository;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class CRLService {
    @Autowired
    CRLRepository crlRepository;
    @Autowired
    KeyManagementService keyManagementService;

    private int mapReason(String reason) {
        if (reason == null) {
            return CRLReason.unspecified;
        }

        return switch (reason) {
            case "KEY_COMPROMISE" -> CRLReason.keyCompromise;
            case "CA_COMPROMISE" -> CRLReason.cACompromise;
            case "AFFILIATION_CHANGED" -> CRLReason.affiliationChanged;
            case "SUPERSEDED" -> CRLReason.superseded;
            case "CESSATION_OF_OPERATION" -> CRLReason.cessationOfOperation;
            case "CERTIFICATE_HOLD" -> CRLReason.certificateHold;
            case "PRIVILEGE_WITHDRAWN" -> CRLReason.privilegeWithdrawn;
            case "AA_COMPROMISE" -> CRLReason.aACompromise;
            default -> CRLReason.unspecified;
        };
    }

    public CRL createOrUpdateCRL(CertificateEntity caCertificate,
                                 List<CertificateEntity> revokedCertificates) {
        try {
            X509Certificate caX509 = pemToX509Certificate(caCertificate.getCertificatePem());
            //crl is signed with ca certificate's private key
            PrivateKey caPrivateKey = keyManagementService.decryptPrivateKey(
                    caCertificate.getEncryptedPrivateKey(),
                    caCertificate.getOrganization()
            );

            X509v2CRLBuilder crlBuilder = new JcaX509v2CRLBuilder(
                    caX509.getSubjectX500Principal(),
                    new Date()
            );

            crlBuilder.setNextUpdate(Date.from(
                    Instant.now().plus(7, ChronoUnit.DAYS)
            ));

            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

            crlBuilder.addExtension(
                    Extension.authorityKeyIdentifier,
                    false,
                    extUtils.createAuthorityKeyIdentifier(caX509)
            );

            for (CertificateEntity revokedCertificate : revokedCertificates) {
                //for every certificate crl stores it's serial number, revocation date and revocation reason
                crlBuilder.addCRLEntry(
                        new BigInteger(revokedCertificate.getSerialNumber()),
                        new Date(),
                        mapReason(revokedCertificate.getRevocationReason())
                );
            }

            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                    .setProvider("BC")
                    .build(caPrivateKey);

            X509CRL x509CRL = new JcaX509CRLConverter()
                    .setProvider("BC")
                    .getCRL(crlBuilder.build(signer));

            CRL crl = crlRepository.findByCaCertificate(caCertificate);

            if (crl == null) {
                crl = new CRL();
                crl.setCaCertificate(caCertificate);
            }

            crl.setCrl(x509CRL.getEncoded());

            return crlRepository.save(crl);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create CRL: " + e.getMessage(), e);
        }
    }

    private X509Certificate pemToX509Certificate(String certificatePem) {
        try {
            String cleanPem = certificatePem
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(cleanPem);

            CertificateFactory certificateFactory =
                    CertificateFactory.getInstance("X.509");

            return (X509Certificate) certificateFactory.generateCertificate(
                    new ByteArrayInputStream(decoded)
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse certificate PEM", e);
        }
    }
}
