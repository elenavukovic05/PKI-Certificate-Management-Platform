package pki_project.pki.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pki_project.pki.model.CRL;
import pki_project.pki.model.CertificateEntity;
import pki_project.pki.model.User;

@Repository
public interface CRLRepository extends JpaRepository<CRL, Long> {
    CRL findByCaCertificate(CertificateEntity caCertificate);

}
