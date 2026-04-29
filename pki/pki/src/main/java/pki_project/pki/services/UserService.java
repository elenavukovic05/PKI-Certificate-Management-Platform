package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    PasswordEncoder passwordEncoder;

    public void createCAUser(CAUserDTO caUser){
        Organization organization = organizationRepository.findById(caUser.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        String encodedPassword = passwordEncoder.encode(caUser.getPassword());
        if(userRepository.existsByUsername(caUser.getUsername())){
            throw new RuntimeException("User already exists");
        }
        User newUser = new User(caUser.getUsername(),encodedPassword,caUser.getFirstName(),
                caUser.getLastName(),null, Role.CA_USER, organization);
        userRepository.save(newUser);

    }
}
