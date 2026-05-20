package pki_project.pki.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pki_project.pki.dto.ActivateCAUserDTO;
import pki_project.pki.dto.CAUserDTO;
import pki_project.pki.services.OrganizationService;
import pki_project.pki.services.UserService;

@RestController
@RequestMapping(value = "api/v1/admin")
public class AdminController {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    UserService userService;


    @PostMapping(value = "/add-organization", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createOrganization(@RequestBody String name){
        organizationService.createOrganization(name);
        return ResponseEntity.ok("Organization created successfuly");
    }
    @PostMapping(value = "/add-ca-user", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createCAUser(@RequestBody CAUserDTO dto){
        userService.createCAUser(dto);
        return ResponseEntity.ok("CA user created successfuly");
    }
    @PostMapping(value = "/activate-ca-user", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> activateCAUser(@RequestBody ActivateCAUserDTO dto) {
        userService.activateCAUser(dto);
        return ResponseEntity.ok("CA user activated successfully");
    }

}
