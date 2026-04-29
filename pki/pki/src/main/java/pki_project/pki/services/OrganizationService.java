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
}
