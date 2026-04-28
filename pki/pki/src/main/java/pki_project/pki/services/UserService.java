package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.CAUserDTO;
import pki_project.pki.model.Organization;
import pki_project.pki.model.enums.Role;
import pki_project.pki.model.User;
import pki_project.pki.repositories.OrganizationRepository;
import pki_project.pki.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    UserRepository userRepository;

    public void createCAUser(CAUserDTO caUser){
        Organization organization = organizationRepository.findById(caUser.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));;
        User newUser = new User(caUser.getUsername(),caUser.getPassword(),caUser.getFirstName(),
                caUser.getLastName(),null, Role.CA_USER, organization);
        userRepository.save(newUser);

    }
}
