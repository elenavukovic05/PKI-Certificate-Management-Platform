package pki_project.pki.services;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.CACertificatePreviewDTO;
import pki_project.pki.dto.CertificatePreviewDTO;
import pki_project.pki.dto.CertificateRequestDTO;
import pki_project.pki.dto.RevokeCertificateRequestDTO;
import pki_project.pki.model.*;
import pki_project.pki.model.enums.CertificateStatus;
import pki_project.pki.model.enums.CertificateType;
import pki_project.pki.repositories.CRLRepository;
import pki_project.pki.repositories.CertificateRepository;
import pki_project.pki.repositories.OrganizationRepository;
import pki_project.pki.repositories.UserRepository;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CertificateService {
    @Autowired
    CertificateGenerator certificateGenerator;

    @Autowired
    CertificateRepository certificateRepository;

    @Autowired
    KeyManagementService keyManagementService;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CRLService crlService;

    @Autowired
    private CRLRepository crlRepository;

    @Autowired
    private UserRepository userRepository;

    public void createCertificate(CertificateRequestDTO certificateRequest) {
        validateCertificateRequest(certificateRequest);
        Subject subject = generateSubject(certificateRequest);
        Issuer issuer = null;
        CertificateEntity issuerEntity = null;
        Organization organization = null;
        if(certificateRequest.getType() == CertificateType.INTERMEDIATE ||
                certificateRequest.getType() == CertificateType.END_ENTITY){
            issuerEntity = certificateRepository
                    .findById(certificateRequest.getIssuerId())
                    .orElseThrow(() -> new RuntimeException("Issuer not found"));

            boolean isChainValid = validateCertificateChain(issuerEntity, certificateRequest.getEndDate());
            if(!isChainValid){
                throw new RuntimeException("Certificate chain of trust is not valid");
            }
            organization = issuerEntity.getOrganization();
            if (organization == null) {
                throw new RuntimeException("issuer certificate has no associated organization");
            }
            //dekripcija privatnog kljuca izdavaoca
            issuer = buildIssuerFromEntity(issuerEntity, organization);
        }
        else{
            organization = organizationRepository.findById(certificateRequest.getOrganizationId())
            .orElseThrow(() -> new RuntimeException("organization not found for root certificate"));
            issuer = new Issuer(subject.getPrivateKey(), subject.getX500Name());
        }

        SecureRandom random = new SecureRandom();
        BigInteger serialNumber = new BigInteger(64, random);

        //issuer certificate serial number is needed for distribution point extension
        String crlIssuerSerialNumber = issuerEntity != null ? issuerEntity.getSerialNumber() : serialNumber.toString();
        X509Certificate x509Certificate = certificateGenerator.generateCertificate(subject,issuer,certificateRequest,
                serialNumber, crlIssuerSerialNumber);
        String encryptedPrivateKey = keyManagementService.encryptPrivateKey(subject.getPrivateKey(), organization);
        String subjectName = subject.getX500Name().toString();
        String pem = certificateToPem(x509Certificate);
        // ###################
        CertificateEntity certificateEntity = new CertificateEntity(
                serialNumber.toString(),
                subjectName,
                issuerEntity,
                certificateRequest.getType(),
                certificateRequest.getStartDate(),
                certificateRequest.getEndDate(),
                certificateRequest.isCA(),
                certificateRequest.isDigitalSignature(),
                certificateRequest.isKeyEncipherment(),
                certificateRequest.isKeyCertSign(),
                certificateRequest.isCrlSign(),
                certificateRequest.getPathLength(),
                pem,
                encryptedPrivateKey
        );

        certificateEntity.setSubjectAlternativeNames(certificateRequest.getSubjectAlternativeNames());
        certificateEntity.setSubjectPublicKey(
            java.util.Base64.getEncoder().encodeToString(subject.getPublicKey().getEncoded())
        );
        certificateEntity.setIssuerName(issuer.getX500Name().toString());
        if (issuerEntity != null) {
            certificateEntity.setIssuerPublicKey(issuerEntity.getIssuerPublicKey());
        } else {
            // for root , issuer public key je = subject public key
            certificateEntity.setIssuerPublicKey(
                java.util.Base64.getEncoder().encodeToString(subject.getPublicKey().getEncoded())
            );
        }
        certificateEntity.setOrganization(organization);
        certificateEntity.setCanDownloadPkcs12(encryptedPrivateKey != null && certificateEntity.getType() == CertificateType.END_ENTITY);
        // ##############################

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            User currentUser = (User) userDetailsService.loadUserByUsername(username);
            User owner = null;
            if(certificateRequest.getType() != CertificateType.END_ENTITY){
                owner = currentUser;
            }else{
                if (certificateRequest.getOwnerId() != null){
                    owner = userRepository
                            .findById(certificateRequest.getOwnerId())
                            .orElseThrow(() -> new RuntimeException("Owner not found"));
                }
            }

            certificateEntity.setOwner(owner);
            certificateEntity.setCreatedBy(currentUser);
        }
        
        certificateRepository.save(certificateEntity);
    }

    void validateCertificateRequest(CertificateRequestDTO certificateRequest){
        if (certificateRequest.getStartDate() == null || certificateRequest.getEndDate() == null) {
            throw new RuntimeException("Start date and end date are required");
        }
        if (certificateRequest.getEndDate().before(certificateRequest.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        String email = certificateRequest.getEmail();
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("Invalid email format");
        }

        if (certificateRequest.getCommonName() == null || certificateRequest.getCommonName().isBlank()) {
            throw new RuntimeException("Common name is required");
        }
        if (certificateRequest.getCountry() == null || certificateRequest.getCountry().isBlank()) {
            throw new RuntimeException("Country is required");
        }

        if (certificateRequest.getType() == CertificateType.INTERMEDIATE ||
                certificateRequest.getType() == CertificateType.ROOT) {
            if (!certificateRequest.isCA() || !certificateRequest.isKeyCertSign()) {
                throw new RuntimeException("Root/Intermediate certificate must have isCA and keyCertSign set to true");
            }
        }
        if(certificateRequest.getPathLength()!= null) {
            if(certificateRequest.getType() == CertificateType.END_ENTITY ||
                    certificateRequest.getType() == CertificateType.ROOT )
                throw new RuntimeException("End entity and root certificate can not have path length extension!");
            else{
                //provjera da li je path length <= roditeljskom
                CertificateEntity issuerEntity = certificateRepository
                        .findById(certificateRequest.getIssuerId())
                        .orElseThrow(() -> new RuntimeException("Issuer not found"));
                if(issuerEntity.getPathLength() != null){
                    if(certificateRequest.getPathLength() < 0)
                        throw new RuntimeException("Path length can not be negative!");
                    if(issuerEntity.getPathLength() == 0)
                        throw new RuntimeException("This issuer can issue only end entity certificates!");
                    if(certificateRequest.getPathLength() > issuerEntity.getPathLength() - 1)
                        throw new RuntimeException("Child CA path length exceeds issuer path length constraint!");

                }

            }
        }

        if (certificateRequest.getType() == CertificateType.END_ENTITY) {
            if (certificateRequest.isCA()) {
                throw new RuntimeException("End entity certificate cannot have isCA set to true");
            }
        }

    }

    public void revokeDownwards(CertificateEntity issuer, String reason){
        List<CertificateEntity> issuedCertificates = certificateRepository.findByIssuerCertificate(issuer);
        for(CertificateEntity certificate : issuedCertificates){
            if (certificate.getStatus() != CertificateStatus.REVOKED) {
                certificate.setStatus(CertificateStatus.REVOKED);
                certificate.setRevocationReason(reason);
                certificateRepository.save(certificate);
            }

            revokeDownwards(certificate, reason);
        }
    }

    public CRL getCRL(String caCertificateSerialNumber){
        System.out.println("SERIAL = " + caCertificateSerialNumber);
        CertificateEntity certificate = certificateRepository.findBySerialNumber(caCertificateSerialNumber);

        if(certificate == null)
            throw new RuntimeException("Certificate not found");

        CRL crl = crlRepository.findByCaCertificate(certificate);
        System.out.println("CRL = " + crl);
        if(crl == null)
            throw new RuntimeException("CRL not found");
        return crl;
    }

    public void revokeCertificate(Long certificateId, RevokeCertificateRequestDTO dto){
        CertificateEntity certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        certificate.setStatus(CertificateStatus.REVOKED);
        certificate.setRevocationReason(dto.getReason().toString());
        certificateRepository.save(certificate);
        if(certificate.getType() == CertificateType.INTERMEDIATE || certificate.getType() == CertificateType.ROOT)
            //revoke all certificates which are issued by this certificate
            revokeDownwards(certificate, dto.getReason().toString());

        CertificateEntity caCertificate =
                certificate.getIssuerCertificate() == null
                        ? certificate
                        : certificate.getIssuerCertificate();

        List<CertificateEntity> revokedByThisCa =
                certificateRepository.findByIssuerCertificateAndStatus(
                        caCertificate,
                        CertificateStatus.REVOKED
                );

        crlService.createOrUpdateCRL(caCertificate, revokedByThisCa);
    }
    public boolean validateCertificateChain(CertificateEntity issuer, Date certificateEndDate){
        try{
            if (issuer == null) {
                return false;
            }
            if(issuer.getStartDate().after(Date.from(Instant.now()))
                    || issuer.getEndDate().before(Date.from(Instant.now()))){
                return false;
            }

            if (certificateEndDate.after(issuer.getEndDate())) {
                return false;
            }
            if (!issuer.isCA()) {
                return false;
            }
            if (!issuer.isKeyCertSign()){
                return false;
            }
            if(issuer.getStatus() == CertificateStatus.REVOKED){
                return false;
            }

            boolean validDigitalSign = validateDigitalSign(issuer);
            if(!validDigitalSign){
                return false;
            }

            //end of recursion
            if (issuer.getType() == CertificateType.ROOT)
                return true;
            //validate issuer's issuer
            return validateCertificateChain(
                    issuer.getIssuerCertificate(),
                    issuer.getEndDate()
            );
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
            CertificateEntity parentCertificateEntity = issuer.getIssuerCertificate();
            if(parentCertificateEntity == null){
                return false;
            }
            X509Certificate parentCertificate = pemToX509Certificate(parentCertificateEntity.getCertificatePem());
            issuerCertificate.verify(parentCertificate.getPublicKey());
            return true;
        }
    }
    public String certificateToPem(X509Certificate certificate) {
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

    public Issuer buildIssuerFromEntity(CertificateEntity issuerEntity, Organization organization){
        PrivateKey issuerPrivateKey = keyManagementService.decryptPrivateKey(issuerEntity.getEncryptedPrivateKey(), organization);
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

    public List<CertificatePreviewDTO> getAllCertificates(){
        List<CertificateEntity> certificates = certificateRepository.findAll();
        List<CertificatePreviewDTO> dto = new ArrayList<>();
        for (var certificate : certificates){
            dto.add(new CertificatePreviewDTO(certificate.getId(), certificate.getSubjectName(),certificate.getIssuerName(),
                    certificate.getType(), certificate.getStatus(), certificate.getStartDate(), certificate.getEndDate(),
                    certificate.isCA(), certificate.isDigitalSignature(), certificate.isKeyEncipherment(),
                    certificate.isKeyCertSign(), certificate.isCrlSign(), certificate.getCertificatePem(), certificate.getOrganization().getName(), certificate.isCanDownloadPkcs12()));
        }
        return dto;
    }

    public List<CACertificatePreviewDTO> getAllCACertificates(){
        List<CertificateEntity> certificates = certificateRepository.findByTypeIn(
                List.of(CertificateType.ROOT, CertificateType.INTERMEDIATE)
        );
        List<CACertificatePreviewDTO> dto = new ArrayList<>();
        for (var certificate : certificates){
            dto.add(new CACertificatePreviewDTO(certificate.getId(),certificate.getSubjectName(),
                    certificate.getType(), certificate.getStartDate(), certificate.getEndDate(),
                    certificate.isCA(), certificate.isDigitalSignature(),
                    certificate.isKeyEncipherment(), certificate.isKeyCertSign(),certificate.isCrlSign(),
                    certificate.getPathLength(),
                    certificate.getOrganization().getName()));
        }
        return dto;
    }

    public List<CertificatePreviewDTO> getMyCertificates() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("user not authintificated");
        }
        
        String username = authentication.getName(); 
        
        User currentUser = (User) userDetailsService.loadUserByUsername(username);
        
        List<CertificateEntity> certificates = certificateRepository
                .findByOwnerIdAndType(currentUser.getId(), CertificateType.END_ENTITY);
        
        List<CertificatePreviewDTO> dtoList = new ArrayList<>();
        for (CertificateEntity cert : certificates) {
            dtoList.add(new CertificatePreviewDTO(
                    cert.getId(),
                    cert.getSubjectName(),
                    cert.getIssuerName(),
                    cert.getType(),
                    cert.getStatus(),
                    cert.getStartDate(),
                    cert.getEndDate(),
                    cert.isCA(),
                    cert.isDigitalSignature(),
                    cert.isKeyEncipherment(),
                    cert.isKeyCertSign(),
                    cert.isCrlSign(),
                    cert.getCertificatePem(),
                    cert.getOrganization() != null ? cert.getOrganization().getName() : null,
                    cert.isCanDownloadPkcs12()
            ));
        }
        return dtoList;
    }

    public void createCertificateAsCAUser(CertificateRequestDTO certificateRequest, Long caUserOrgId) {
        if (certificateRequest.getType() == CertificateType.ROOT) {
            throw new RuntimeException("CA user cannot issue root certificates");
        }

        CertificateEntity issuerEntity = certificateRepository
                .findById(certificateRequest.getIssuerId())
                .orElseThrow(() -> new RuntimeException("Issuer not found"));

        if (!issuerEntity.getOrganization().getId().equals(caUserOrgId)) {
            throw new RuntimeException("CA user can only use issuers from their own organization");
        }

        createCertificate(certificateRequest);
    }

    public List<CertificatePreviewDTO> getCertificatesByOrganization(Long organizationId) {
        List<CertificateEntity> certificates = certificateRepository.findByOrganization_Id(organizationId);
        List<CertificatePreviewDTO> dtoList = new ArrayList<>();
        for (CertificateEntity cert : certificates) {
            dtoList.add(new CertificatePreviewDTO(
                    cert.getId(),
                    cert.getSubjectName(),
                    cert.getIssuerName(),
                    cert.getType(),
                    cert.getStatus(),
                    cert.getStartDate(),
                    cert.getEndDate(),
                    cert.isCA(),
                    cert.isDigitalSignature(),
                    cert.isKeyEncipherment(),
                    cert.isKeyCertSign(),
                    cert.isCrlSign(),
                    cert.getCertificatePem(),
                    cert.getOrganization() != null ? cert.getOrganization().getName() : null,
                    cert.isCanDownloadPkcs12()
            ));
        }
        return dtoList;
    }
}
