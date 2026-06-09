package pki_project.pki.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pki_project.pki.dto.ActivateCAUserDTO;
import pki_project.pki.dto.CAUserDTO;
import pki_project.pki.services.KeyManagementService;
import pki_project.pki.services.OrganizationService;
import pki_project.pki.services.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/admin")
public class AdminController {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    UserService userService;
    @Autowired
    KeyManagementService keyManagementService;


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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/rotate-master-key")
    public ResponseEntity<String> rotateMasterKey(){
        try {
            String newMasterKey = keyManagementService.rotateMasterKey();
            return ResponseEntity.ok(newMasterKey);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rotation-status")
    public ResponseEntity<?> getRotationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("canRotate", keyManagementService.canRotate());
        status.put("daysUntilNextRotation", keyManagementService.getDaysUntilNextRotation());
        return ResponseEntity.ok(status);
    }

}
