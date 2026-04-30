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