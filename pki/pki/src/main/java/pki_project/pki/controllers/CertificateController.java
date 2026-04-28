package pki_project.pki.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pki_project.pki.dto.CertificateRequestDTO;
import pki_project.pki.services.CertificateService;

@RestController
@RequestMapping("api/v1/certificates")
public class CertificateController {

    @Autowired
    CertificateService certificateService;

    @PostMapping(value = "/create-certificate", consumes = MediaType.APPLICATION_JSON_VALUE)
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createCertificate(@RequestBody CertificateRequestDTO dto){
        try{
            certificateService.createCertificate(dto);
            return ResponseEntity.ok("Certificate successfully created!");
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
