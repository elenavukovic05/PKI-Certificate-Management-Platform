export interface VerifyPasswordRecoveryDTO{
    username : string;
    decryptedActivationChallenge : string;
}