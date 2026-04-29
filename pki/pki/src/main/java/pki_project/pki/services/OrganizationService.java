package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.OrganizationDTO;
import pki_project.pki.model.Organization;
import pki_project.pki.repositories.OrganizationRepository;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrganizationService {

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    KeyManagementService keyManagementService;

    public List<OrganizationDTO> getAllOrganizations(){
        List<Organization> organizations  = organizationRepository.findAll();
        List<OrganizationDTO> dto = new ArrayList<>();
        for(var organization : organizations){
            dto.add(new OrganizationDTO(organization.getId(),organization.getName()));
        }
        return dto;
    }

    public OrganizationDTO createOrganization(String name) {
        SecretKey orgKey = keyManagementService.generateOrganizationKey();
        String encryptedOrgKey = keyManagementService.encryptOrganizationKey(orgKey);

        Organization organization = new Organization();
        organization.setName(name);
        organization.setEncryptedOrgKey(encryptedOrgKey);

        Organization saved = organizationRepository.save(organization);
        return new OrganizationDTO(saved.getId(), saved.getName());
    }
}
