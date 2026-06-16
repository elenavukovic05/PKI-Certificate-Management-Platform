package pki_project.pki.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pki_project.pki.model.Organization;
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByName(String name);
}