package pki_project.pki.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true)
    private String tokenString;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String familyId; //uuid

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date expireAt;

    private boolean isUsed;

    private boolean isRevoked;

    public RefreshTokenEntity(){

    }

    public RefreshTokenEntity(String tokenString, User user, String familyId, Date createdAt, Date expireAt, boolean isUsed, boolean isRevoked) {
        this.tokenString = tokenString;
        this.user = user;
        this.familyId = familyId;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.isUsed = isUsed;
        this.isRevoked = isRevoked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenString() {
        return tokenString;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }
}