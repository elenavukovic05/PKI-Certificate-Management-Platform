package pki_project.pki.services;

import org.springframework.stereotype.Service;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import java.util.concurrent.TimeUnit;

@Service
public class TwoFactorService {
    private final GoogleAuthenticator gAuth;
    private static final String ISSUER = "PKI System";
    
    public TwoFactorService() {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
            .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))
            .setWindowSize(2)   // 60s extually
            .build();
        this.gAuth = new GoogleAuthenticator(config);
    }
    
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    // otpauth:// URL for QR code
    public String generateQrUrl(String userEmail, String secret) {
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(secret).build();
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(ISSUER, userEmail, key);
    }

    public boolean verifyCode(String secret, int code) {
        System.out.println("=== 2FA Verification Debug ===");
        System.out.println("Secret: " + secret);
        System.out.println("Code received: " + code);
        
        // long timeWindow = System.currentTimeMillis() / 1000 / 30;
        // System.out.println("Current time window: " + timeWindow);
        
        boolean result = gAuth.authorize(secret, code);
        System.out.println("Authorization result: " + result);
        
        return result;
    }
}