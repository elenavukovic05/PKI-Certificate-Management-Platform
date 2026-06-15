package pki_project.pki.repositories;

import java.util.List;

import org.bouncycastle.asn1.ocsp.CertID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pki_project.pki.model.CertificateEntity;
import pki_project.pki.model.enums.CertificateStatus;
import pki_project.pki.model.enums.CertificateType;

@Repository
public interface CertificateRepository extends JpaRepository<CertificateEntity, Long> {
    List<CertificateEntity> findByOwnerIdAndType(Long ownerId, CertificateType type);
    List<CertificateEntity> findByOrganization_Id(Long organizationId);
    List<CertificateEntity> findByTypeIn(List<CertificateType> types);
    List<CertificateEntity> findByIssuerCertificate(CertificateEntity issuer);
    List<CertificateEntity> findByIssuerCertificateAndStatus(CertificateEntity issuer, CertificateStatus status);
    CertificateEntity findBySerialNumber(String serialNumber);
}
