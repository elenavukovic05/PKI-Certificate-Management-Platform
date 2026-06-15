package pki_project.pki.dto;

import pki_project.pki.model.enums.RevocationReason;

public class RevokeCertificateRequestDTO {
    RevocationReason reason;

    public RevokeCertificateRequestDTO(RevocationReason reason) {
        this.reason = reason;
    }

    public RevocationReason getReason() {
        return reason;
    }

    public void setReason(RevocationReason reason) {
        this.reason = reason;
    }
}
