export interface CertificatePreviewDTO {
  id: number;
  subjectName: string;
  issuerName: string;
  type: string;
  status: string;
  startDate: Date;
  endDate: Date;
  isCA: boolean;
  digitalSignature: boolean;
  keyEncipherment: boolean;
  keyCertSign: boolean;
  crlSign: boolean;
  certificatePem: string;
  organizationName: string;
  canDownloadPkcs12: boolean
}
export interface CACertificatePreviewDTO{
    id : number;
    subjectName : string;
    type : string;
    startDate : Date;
    endDate : Date;
    isCA : boolean;
    digitalSignature : boolean ;
    keyEncipherment : boolean;
    keyCertSign : boolean ;
    crlSign :boolean ;
    pathLength : number;
    organizationName : string;
}
export interface CertificateRequestDTO {
  commonName: string;
  name: string;
  lastName: string;
  organization: string;
  email: string;
  organizationalUnit: string;
  country: string;
  startDate: string;
  endDate: string;
  issuerId: number | null;
  isCA: boolean;
  digitalSignature: boolean;
  keyEncipherment: boolean;
  keyCertSign: boolean;
  crlSign: boolean;
  pathLength : number | null;
  subjectAlternativeNames : string[];
  organizationId: number | null;
  type: string;
  ownerId?: number | null;
}

export interface Pkcs12RequestDTO {
  password: string
}
export interface CsrRequest {
  csrPem: string;
  issuerId: number;
  startDate: string;
  endDate: string;
}

export interface AutogenerateRequest {
  commonName: string;
  name: string;
  lastName: string;
  organization: string;
  organizationalUnit: string;
  country: string;
  email: string;
  issuerId: number;
  startDate: string;
  endDate: string;
  password?: string;
}

export interface AutogenerateResponse {
  certificateId: number;
  certificatePem: string;
  pkcs12Base64: string;
}