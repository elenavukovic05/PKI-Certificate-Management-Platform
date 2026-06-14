package pki_project.pki.dto;

public class VerifyPasswordRecoveryDTO {
    String username;
    String decryptedActivationChallenge ;

    public VerifyPasswordRecoveryDTO(String username, String decryptedActivationChallenge) {
        this.username = username;
        this.decryptedActivationChallenge = decryptedActivationChallenge;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDecryptedActivationChallenge() {
        return decryptedActivationChallenge;
    }

    public void setDecryptedActivationChallenge(String decryptedActivationChallenge) {
        this.decryptedActivationChallenge = decryptedActivationChallenge;
    }
}

