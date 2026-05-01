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
  organizationId: number | null;
  type: string;
}