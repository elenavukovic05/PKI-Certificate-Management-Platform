package pki_project.pki.services;

import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import pki_project.pki.dto.AutogenerateRequestDTO;
import pki_project.pki.dto.AutogenerateResponseDTO;
import pki_project.pki.dto.CsrRequestDTO;
import pki_project.pki.model.CertificateEntity;
import pki_project.pki.model.enums.CertificateType;
import pki_project.pki.model.Issuer;
import pki_project.pki.model.Organization;
import pki_project.pki.model.Subject;
import pki_project.pki.model.User;
import pki_project.pki.model.enums.CertificateStatus;
import pki_project.pki.repositories.CertificateRepository;

@Service
public class CsrService {
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private CertificateGenerator certificateGenerator;
    @Autowired
    private CertificateService certificateService;
    @Autowired
    private UserDetailsService userDetailsService;

    public Long issueCertificateFromCsr(CsrRequestDTO requestDTO) {
        PKCS10CertificationRequest certificationRequest = parseCsr(requestDTO.getCsrPem());
        PublicKey sPublicKey = extractPublicKey(certificationRequest);
        X500Name subjectName = certificationRequest.getSubject();
        CertificateEntity issuerEntity = certificateRepository.findById(requestDTO.getIssuerId()).orElseThrow(() -> new RuntimeException("issuer not found"));
        validateIssuerForEE(issuerEntity, requestDTO.getStartDate(), requestDTO.getEndDate());
        Organization organization = issuerEntity.getOrganization();
        Issuer issuer = certificateService.buildIssuerFromEntity(issuerEntity, organization);
        Subject subject = new Subject(sPublicKey, null, subjectName);
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        X509Certificate x509 = certificateGenerator.generateCertificateFromSubjectPublicKey(
                subject, issuer, requestDTO.getStartDate(), requestDTO.getEndDate(), serialNumber, false,
                false, true, false, false
        );
        String pem = certificateService.certificateToPem(x509);
        CertificateEntity entity = buildEntity(
                serialNumber, subjectName.toString(), issuerEntity,
                sPublicKey, organization, pem, null,
                requestDTO.getStartDate(), requestDTO.getEndDate()
        );
        setOwner(entity);
        certificateRepository.save(entity);
        return entity.getId();
    }

    public AutogenerateResponseDTO autogenerate(AutogenerateRequestDTO requestDTO) {
        CertificateEntity issuerEntity = certificateRepository
                .findById(requestDTO.getIssuerId())
                .orElseThrow(() -> new RuntimeException("issuer not found"));

        validateIssuerForEE(issuerEntity, requestDTO.getStartDate(), requestDTO.getEndDate());

        Organization organization = issuerEntity.getOrganization();
        Issuer issuer = certificateService.buildIssuerFromEntity(issuerEntity, organization);
        KeyPair keyPair = certificateService.generateKeyPair();
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, requestDTO.getCommonName());
        if (requestDTO.getName() != null) builder.addRDN(BCStyle.GIVENNAME, requestDTO.getName());
        if (requestDTO.getLastName() != null) builder.addRDN(BCStyle.SURNAME, requestDTO.getLastName());
        if (requestDTO.getOrganization() != null) builder.addRDN(BCStyle.O, requestDTO.getOrganization());
        if (requestDTO.getOrganizationalUnit() != null) builder.addRDN(BCStyle.OU, requestDTO.getOrganizationalUnit());
        builder.addRDN(BCStyle.C, requestDTO.getCountry());
        if (requestDTO.getEmail() != null) builder.addRDN(BCStyle.E, requestDTO.getEmail());
        X500Name subjectName = builder.build();

        Subject subject = new Subject(keyPair.getPublic(), keyPair.getPrivate(), subjectName);
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        X509Certificate x509 = certificateGenerator.generateCertificateFromSubjectPublicKey(
                subject, issuer, requestDTO.getStartDate(), requestDTO.getEndDate(), serialNumber, false,
                false, true, false, false
        );
        String pem = certificateService.certificateToPem(x509);
        if (requestDTO.getPassword() == null || requestDTO.getPassword().isBlank()) {
            throw new RuntimeException("Password is required for PKCS12 generation");
        }

        String pkcs12Base64;
        try {
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);

            keyStore.setKeyEntry(
                    "certificate",
                    keyPair.getPrivate(),
                    requestDTO.getPassword().toCharArray(),
                    new java.security.cert.Certificate[]{x509}
            );

            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            keyStore.store(outputStream, requestDTO.getPassword().toCharArray());
            byte[] pkcs12Bytes = outputStream.toByteArray();
            pkcs12Base64 = Base64.getEncoder().encodeToString(pkcs12Bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PKCS12 store: " + e.getMessage(), e);
        }
        CertificateEntity entity = buildEntity(
                serialNumber, subjectName.toString(), issuerEntity,
                keyPair.getPublic(), organization, pem, null,
                requestDTO.getStartDate(), requestDTO.getEndDate()
        );

        setOwner(entity);
        certificateRepository.save(entity);
        return new AutogenerateResponseDTO(entity.getId(), pem, pkcs12Base64);
    }

    private PKCS10CertificationRequest parseCsr(String csrPem) {
        try (PEMParser parser = new PEMParser(new StringReader(csrPem))) {
            Object obj = parser.readObject();
            if (obj instanceof PKCS10CertificationRequest) {
                return (PKCS10CertificationRequest) obj;
            }
            throw new RuntimeException("file is not valid csr");
        } catch (Exception e) {
            throw new RuntimeException("error: " + e.getMessage(), e);
        }
    }

    private PublicKey extractPublicKey(PKCS10CertificationRequest csr) {
        try {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPublicKey(csr.getSubjectPublicKeyInfo());
        } catch (Exception ex) {
            throw new RuntimeException("cannot get public key", ex);
        }
    }

    private void validateIssuerForEE(CertificateEntity issuer, Date start, Date end) {
        if (issuer.getStatus() == CertificateStatus.REVOKED)
            throw new RuntimeException("issuer certificate is revoked");
        if (!issuer.isCA() || !issuer.isKeyCertSign())
            throw new RuntimeException("certificate not ca");
        Date now = new Date();
        if (issuer.getStartDate().after(now) || issuer.getEndDate().before(now))
            throw new RuntimeException("issuer certificate is not valid");
        if (end.after(issuer.getEndDate()))
            throw new RuntimeException("the duration of certificate should not exceed the issuer one");
        if (end.before(start))
            throw new RuntimeException("end date should be after start date");
    }

    private CertificateEntity buildEntity(BigInteger serialNumber, String subjectName,
                                          CertificateEntity issuerEntity, PublicKey publicKey,
                                          Organization organization, String pem, String encryptedPrivKey,
                                          Date start, Date end) {
        CertificateEntity e = new CertificateEntity(
                serialNumber.toString(),
                subjectName,
                issuerEntity,
                CertificateType.END_ENTITY,
                start,
                end,
                false,
                false,
                true,
                false,
                false,
                pem,
                encryptedPrivKey
        );
        e.setSubjectPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        e.setIssuerName(issuerEntity.getSubjectName());
        e.setIssuerPublicKey(issuerEntity.getSubjectPublicKey());
        e.setOrganization(organization);
        return e;
    }
    
    private void setOwner(CertificateEntity entity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) userDetailsService.loadUserByUsername(auth.getName());
            entity.setOwner(user);
            entity.setCreatedBy(user);
        }
    }
}
