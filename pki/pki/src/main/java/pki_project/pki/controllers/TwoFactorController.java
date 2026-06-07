package pki_project.pki.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import pki_project.pki.dto.LoginResponseDTO;
import pki_project.pki.dto.TwoFactorSetUpDTO;
import pki_project.pki.dto.TwoFactorVerifyDTO;
import pki_project.pki.model.User;
import pki_project.pki.repositories.UserRepository;
import pki_project.pki.security.JwtUtil;
import pki_project.pki.services.AuthService;
import pki_project.pki.services.RefreshTokenService;
import pki_project.pki.services.TwoFactorService;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/2fa")
public class TwoFactorController {

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/setup")
    public ResponseEntity<TwoFactorSetUpDTO> setup2FA() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userDetailsService.loadUserByUsername(username);

        String secret = twoFactorService.generateSecret();
        String qrUrl = twoFactorService.generateQrUrl(user.getUsername(), secret);

        return ResponseEntity.ok(new TwoFactorSetUpDTO(secret, qrUrl));
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enable2FA(@RequestBody Map<String, Object> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userDetailsService.loadUserByUsername(username);

        String secret = (String) body.get("secret");
        int code = ((Number) body.get("code")).intValue();

        if (!twoFactorService.verifyCode(secret, code)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "invalid code, please scan QR code again");
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("message", "2FA successfully enabled");
        successResponse.put("success", true);
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify2FA(@RequestBody TwoFactorVerifyDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userDetailsService.loadUserByUsername(username);

        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "2FA is not enabled for this user");
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), dto.getCode())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "invalid 2FA code");
            errorResponse.put("success", false);
            return ResponseEntity.status(401).body(errorResponse);
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = refreshTokenService.generateToken(user, UUID.randomUUID().toString());
        
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("accessToken", token);
        successResponse.put("requires2FA", false);
        successResponse.put("success", true);
        successResponse.put("refreshToken", refreshToken);
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable2FA(@RequestBody Map<String, Object> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userDetailsService.loadUserByUsername(username);

        if (!user.isTwoFactorEnabled()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "2FA is not enabled");
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        int code = ((Number) body.get("code")).intValue();
        if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "invalid 2FA code");
            errorResponse.put("success", false);
            return ResponseEntity.status(401).body(errorResponse);
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("message", "2FA disabled");
        successResponse.put("success", true);
        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> get2FAStatus() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) userDetailsService.loadUserByUsername(username);
        return ResponseEntity.ok(Map.of("enabled", user.isTwoFactorEnabled()));
    }
}