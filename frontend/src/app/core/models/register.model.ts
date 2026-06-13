export interface RegisterDTO{
    username : string;
    password : string;
    confirmedPassword : string;
    name : string;
    lastName : string;
    organizationId : number | null;
    publicKey : string
}