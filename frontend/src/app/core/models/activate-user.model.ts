export interface ActivateUserDTO{
    activationToken : string;
    decryptedActivationChallenge : string;
}