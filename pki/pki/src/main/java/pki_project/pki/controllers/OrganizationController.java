package pki_project.pki.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pki_project.pki.dto.OrganizationDTO;
import pki_project.pki.model.Organization;
import pki_project.pki.services.OrganizationService;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/organizations")
public class OrganizationController {

    @Autowired
    OrganizationService organizationService;

    @GetMapping(value = "/all")
    public ResponseEntity<List<OrganizationDTO>> getAllOrganizations(){
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationDTO> createOrganization(@RequestParam String name) {
        return ResponseEntity.ok(organizationService.createOrganization(name));
    }
}
