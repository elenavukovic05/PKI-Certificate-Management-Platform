package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pki_project.pki.dto.ActivateCAUserDTO;
import pki_project.pki.dto.CAUserDTO;
import pki_project.pki.dto.ChangePasswordDTO;
import pki_project.pki.dto.UserDTO;
import pki_project.pki.model.Organization;
import pki_project.pki.model.User;
import pki_project.pki.model.enums.Role;
import pki_project.pki.repositories.OrganizationRepository;
import pki_project.pki.repositories.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    EmailService emailService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public void createCAUser(CAUserDTO caUser) {
        Organization organization = organizationRepository.findById(caUser.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        String encodedPassword = passwordEncoder.encode(caUser.getPassword());
        if (userRepository.existsByUsername(caUser.getUsername())) {
            throw new RuntimeException("User already exists");
        }
        User newUser = new User(caUser.getUsername(), encodedPassword, caUser.getFirstName(),
                caUser.getLastName(), null, Role.CA_USER, organization);
        String token = UUID.randomUUID().toString();
        newUser.setEnabled(false);
        newUser.setActivationToken(token);
        newUser.setActivationTokenExpiresAt(
                Date.from(Instant.now().plus(24, ChronoUnit.HOURS))
        );

        userRepository.save(newUser);

        emailService.sendCAUserActivationEmail(newUser.getUsername(), token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User with that email not found")
                );
    }

    public void changePassword(ChangePasswordDTO dto){
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(()-> new RuntimeException("User not found"));
        if(dto.getNewPassword() == null)
            throw new RuntimeException("New password is null");
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }
    public List<UserDTO> getRegularUsers() {
        List<User> users = userRepository.findByRole(Role.USER);

        List<UserDTO> result = new ArrayList<>();

        for (User user : users) {
            result.add(new UserDTO(
                    user.getId(),
                    user.getUsername(), // email
                    user.getFirstName(),
                    user.getLastName()
            ));
        }

        return result;
    }

    public void activateCAUser(ActivateCAUserDTO dto) {
        User user = userRepository.findByActivationToken(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        if (user.getActivationTokenExpiresAt().before(new Date())) {
            throw new RuntimeException("Activation token expired");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setEnabled(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiresAt(null);

        userRepository.save(user);
    }
}
