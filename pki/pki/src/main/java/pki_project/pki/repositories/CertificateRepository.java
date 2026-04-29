package pki_project.pki.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pki_project.pki.model.CertificateEntity;
import pki_project.pki.model.enums.CertificateType;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<CertificateEntity, Long> {
    List<CertificateEntity> findByTypeIn(List<CertificateType> types);
}
