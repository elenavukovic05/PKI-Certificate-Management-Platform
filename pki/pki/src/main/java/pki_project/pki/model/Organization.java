package pki_project.pki.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedOrgKey;

    public Organization() {
    }

    public Organization(String name, String encryptedOrgKey) {
        this.name = name;
        this.encryptedOrgKey = encryptedOrgKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncryptedOrgKey() {
        return encryptedOrgKey;
    }

    public void setEncryptedOrgKey(String encryptedOrgKey) {
        this.encryptedOrgKey = encryptedOrgKey;
    }

    
}