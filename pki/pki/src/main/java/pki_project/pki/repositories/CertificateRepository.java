package pki_project.pki.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pki_project.pki.model.CertificateEntity;

@Repository
public interface CertificateRepository extends JpaRepository<CertificateEntity, Long> {

}
