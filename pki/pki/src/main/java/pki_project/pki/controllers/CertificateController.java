package pki_project.pki.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pki_project.pki.dto.CACertificatePreviewDTO;
import pki_project.pki.dto.CertificatePreviewDTO;
import pki_project.pki.dto.CertificateRequestDTO;
import pki_project.pki.model.CertificateEntity;
import pki_project.pki.services.CertificateService;

import java.util.List;

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
    @GetMapping("/all")
    public ResponseEntity<List<CertificatePreviewDTO>> getAllCertificates(){
        return ResponseEntity.ok(certificateService.getAllCertificates());
    }

    @GetMapping("/ca-certificates")
    public ResponseEntity<List<CACertificatePreviewDTO>> getAllCACertificates(){
        return ResponseEntity.ok(certificateService.getAllCACertificates());
    }
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CertificatePreviewDTO>> getMyCertificates() {
        return ResponseEntity.ok(certificateService.getMyCertificates());
    }

}
