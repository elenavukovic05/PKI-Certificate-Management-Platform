package pki_project.pki.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pki_project.pki.model.RefreshTokenEntity;
import pki_project.pki.model.User;
import pki_project.pki.repositories.RefreshTokenRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${refresh-token-expiration}")
    private Long expiration;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateToken(User user, String familyId){
        UUID refreshTokenUUID = UUID.randomUUID();
        String refreshToken = refreshTokenUUID.toString();
        Date createdAt = new Date(System.currentTimeMillis());
        Date expireAt = new Date(System.currentTimeMillis() + expiration);
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(
                refreshToken,
                user,
                familyId,
                createdAt,
                expireAt,
                false,
                false
        );

        refreshTokenRepository.save(refreshTokenEntity);
        return refreshToken;
    }

    public RefreshTokenEntity findByToken(String refreshToken){
        return refreshTokenRepository
                .findByTokenString(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token entity not found"));
    }

    public boolean validateRefreshToken(RefreshTokenEntity refreshTokenEntity) {
        if (refreshTokenEntity.isUsed()){
            setFamilyInvalid(refreshTokenEntity);
            throw new RuntimeException("Refresh token reuse detected!");
        }

        if (refreshTokenEntity.isRevoked()){
            throw new RuntimeException("Token revoked");
        }

        if (refreshTokenEntity.getExpireAt().getTime() <= System.currentTimeMillis()){
            throw new RuntimeException("Token expired");
        }

        return true;
    }

    public void setInvalid(RefreshTokenEntity refreshTokenEntity) {
        refreshTokenEntity.setUsed(true);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    public void setFamilyInvalid(RefreshTokenEntity refreshTokenEntity){
        List<RefreshTokenEntity> family = refreshTokenRepository.findAllByFamilyId(refreshTokenEntity.getFamilyId());
        for(RefreshTokenEntity entity : family){
            entity.setRevoked(true);
        }
        refreshTokenRepository.saveAll(family);
    }
}
