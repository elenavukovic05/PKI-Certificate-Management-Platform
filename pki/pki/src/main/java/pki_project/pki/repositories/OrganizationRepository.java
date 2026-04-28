package pki_project.pki.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import pki_project.pki.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}