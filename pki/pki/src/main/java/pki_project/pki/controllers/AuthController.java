package pki_project.pki.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import pki_project.pki.dto.LoginRequestDTO;
import pki_project.pki.dto.LoginResponseDTO;
import pki_project.pki.dto.RefreshRequestDTO;
import pki_project.pki.services.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request){
        try{
            LoginResponseDTO response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> login (@Valid @RequestBody RefreshRequestDTO request){
        try{
            LoginResponseDTO response = authService.refreshTokens(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout (@Valid @RequestBody RefreshRequestDTO request) {
        try{
            authService.logout(request);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Radi!");
    }
}
