package pki_project.pki.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "master_key_rotation")
public class MasterKeyRotation {
    @Id
    private Long id = 1L; //uvijek samo jedan red u tabeli

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedKey;

    @Column
    private Date lastRotatedAt;

    @Column
    private int rotationIntervalDays; // 180 = 6 mjeseci

    public MasterKeyRotation(){}

    public MasterKeyRotation(Date lastRotatedAt, int rotationIntervalDays){
        this.id = 1L;
        this.lastRotatedAt = lastRotatedAt;
        this.rotationIntervalDays = rotationIntervalDays;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastRotatedAt() {
        return lastRotatedAt;
    }

    public void setLastRotatedAt(Date lastRotatedAt) {
        this.lastRotatedAt = lastRotatedAt;
    }

    public int getRotationIntervalDays() {
        return rotationIntervalDays;
    }

    public void setRotationIntervalDays(int rotationIntervalDays) {
        this.rotationIntervalDays = rotationIntervalDays;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }
}
