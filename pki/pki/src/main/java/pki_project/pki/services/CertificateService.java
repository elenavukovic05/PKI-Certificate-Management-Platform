package pki_project.pki.services;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.CertificateRequestDTO;
import pki_project.pki.model.CertificateEntity;
import pki_project.pki.model.Issuer;
import pki_project.pki.model.Subject;
import pki_project.pki.model.enums.CertificateType;
import pki_project.pki.repositories.CertificateRepository;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

@Service
public class CertificateService {
    @Autowired
    CertificateGenerator certificateGenerator;

    @Autowired
    CertificateRepository certificateRepository;

    @Autowired
    PrivateKeyEncryptionService privateKeyEncryptionService;

    public void createCertificate(CertificateRequestDTO certificateRequest) {
        Subject subject = generateSubject(certificateRequest);
        Issuer issuer = null;
        CertificateEntity issuerEntity = null;
        if(certificateRequest.getType() == CertificateType.INTERMEDIATE ||
                certificateRequest.getType() == CertificateType.END_ENTITY){
            issuerEntity = certificateRepository
                    .findById(certificateRequest.getIssuerId())
                    .orElseThrow(() -> new RuntimeException("Issuer not found"));

            boolean isIssuerValid = validateIssuer(issuerEntity, certificateRequest);
            if(!isIssuerValid){
                throw new RuntimeException("Issuer certificate is not valid");
            }
            //dekripcija privatnog kljuca izdavaoca
            issuer = buildIssuerFromEntity(issuerEntity);
        }
        else{
            issuer = new Issuer(
                    subject.getPrivateKey(),
                    subject.getX500Name()
            );
        }

        SecureRandom random = new SecureRandom();
        BigInteger serialNumber = new BigInteger(64, random);
        X509Certificate x509Certificate = certificateGenerator.generateCertificate(subject,issuer,certificateRequest,
                serialNumber);

        /*Certificate certificate = new Certificate(subject, issuer, serialNumber.toString(),
                certificateRequest.getStartDate(), certificateRequest.getEndDate(),x509Certificate );*/

        String encryptedPrivateKey = privateKeyEncryptionService.encryptPrivateKey(subject.getPrivateKey());
        String subjectName = subject.getX500Name().toString();
        /*if (certificateRequest.getType() == CertificateType.ROOT){
            issuerEntity = null;
        }*/
        String pem = certificateToPem(x509Certificate);
        CertificateEntity certificateEntity = new CertificateEntity(serialNumber.toString(),subjectName,issuerEntity,
                certificateRequest.getType(),certificateRequest.getStartDate(),certificateRequest.getEndDate(),
                false,certificateRequest.isCA(),certificateRequest.isDigitalSignature(),
                certificateRequest.isKeyEncipherment(), certificateRequest.isKeyCertSign(),
                certificateRequest.isCrlSign(),pem,encryptedPrivateKey);
        certificateRepository.save(certificateEntity);
    }
    boolean validateIssuer(CertificateEntity issuer, CertificateRequestDTO certificateRequest){
        try{
            if(issuer.getStartDate().after(Date.from(Instant.now()))
                    || issuer.getEndDate().before(Date.from(Instant.now())))
                return false;
            if (certificateRequest.getEndDate().after(issuer.getEndDate())) {
                return false;
            }
            if (!issuer.isCA()) return false;
            if (!issuer.isKeyCertSign()) return false;
            if(issuer.isRevoked())
                return false;
            boolean validDigitalSign = validateDigitalSign(issuer);
            if(!validDigitalSign)
                return false;
            return true;
        }catch (Exception e) {
            return false;
        }

    }
    public boolean validateDigitalSign(CertificateEntity issuer) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        X509Certificate issuerCertificate = pemToX509Certificate(issuer.getCertificatePem());
        if(issuer.getType() == CertificateType.ROOT){
            issuerCertificate.verify(issuerCertificate.getPublicKey());
            return true;
        }else{
            CertificateEntity parentCertificateEntity = issuer.getIssuer();
            if(parentCertificateEntity == null){
                return false;
            }
            X509Certificate parentCertificate = pemToX509Certificate(parentCertificateEntity.getCertificatePem());
            issuerCertificate.verify(parentCertificate.getPublicKey());
            return true;
        }
    }
    private String certificateToPem(X509Certificate certificate) {
        try {
            String base64 = java.util.Base64.getMimeEncoder(64, "\n".getBytes())
                    .encodeToString(certificate.getEncoded());

            return "-----BEGIN CERTIFICATE-----\n" +
                    base64 +
                    "\n-----END CERTIFICATE-----";
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert certificate to PEM", e);
        }
    }
    private X509Certificate pemToX509Certificate(String certificatePem) {
        try {
            String cleanPem = certificatePem
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = java.util.Base64.getDecoder().decode(cleanPem);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            return (X509Certificate) certificateFactory.generateCertificate(
                    new java.io.ByteArrayInputStream(decoded)
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse certificate PEM", e);
        }
    }
    public Issuer buildIssuerFromEntity(CertificateEntity issuerEntity){
        PrivateKey issuerPrivateKey = privateKeyEncryptionService.decryptPrivateKey(issuerEntity.getEncryptedPrivateKeyPem());
        X500Name x500Name = new X500Name(issuerEntity.getSubjectName());
        return new Issuer(issuerPrivateKey, x500Name);
    }
    public Subject generateSubject(CertificateRequestDTO certificateRequest) {
        KeyPair keyPairSubject = generateKeyPair();
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.CN, certificateRequest.getCommonName() );
        builder.addRDN(BCStyle.SURNAME, certificateRequest.getLastName());
        builder.addRDN(BCStyle.GIVENNAME, certificateRequest.getName());
        builder.addRDN(BCStyle.O, certificateRequest.getOrganization());
        builder.addRDN(BCStyle.OU, certificateRequest.getOrganizationalUnit());
        builder.addRDN(BCStyle.C, certificateRequest.getCountry());
        builder.addRDN(BCStyle.E, certificateRequest.getEmail());

        return new Subject(keyPairSubject.getPublic(),keyPairSubject.getPrivate(), builder.build());
    }
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }
}
