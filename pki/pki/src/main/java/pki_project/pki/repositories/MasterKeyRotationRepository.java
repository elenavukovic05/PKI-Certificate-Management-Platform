package pki_project.pki.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pki_project.pki.model.MasterKeyRotation;

@Repository
public interface MasterKeyRotationRepository extends JpaRepository<MasterKeyRotation, Long> {
}
