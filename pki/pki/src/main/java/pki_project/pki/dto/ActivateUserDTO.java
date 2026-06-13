package pki_project.pki.dto;

public class ActivateUserDTO {
    String activationToken;
    String decryptedActivationChallenge;

    public ActivateUserDTO(String activationToken, String decryptedActivationChallenge) {
        this.activationToken = activationToken;
        this.decryptedActivationChallenge = decryptedActivationChallenge;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public String getDecryptedActivationChallenge() {
        return decryptedActivationChallenge;
    }

    public void setDecryptedActivationChallenge(String decryptedActivationChallenge) {
        this.decryptedActivationChallenge = decryptedActivationChallenge;
    }
}
