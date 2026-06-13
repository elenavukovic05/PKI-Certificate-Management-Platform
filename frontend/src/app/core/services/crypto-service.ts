import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CryptoService {

  async generateKeyPair() : Promise<string> {
  
    const keyPair = await window.crypto.subtle.generateKey(
      {
        name: 'RSA-OAEP',
        modulusLength: 2048,
        publicExponent: new Uint8Array([1, 0, 1]),  //standard RSA public exponent
        hash: 'SHA-256',
      },
      true,
      ['encrypt', 'decrypt']
    );

    const publicKeyPem = await this.exportPublicKey(keyPair.publicKey);
    const privateKeyPem = await this.exportPrivateKey(keyPair.privateKey);
    this.downloadPrivateKey(privateKeyPem);
    return publicKeyPem;
    
  }

  private async exportPublicKey(publicKey: CryptoKey): Promise<string> {
    const exported = await window.crypto.subtle.exportKey('spki', publicKey);
    return this.arrayBufferToPem(exported, 'PUBLIC KEY');
  }

  private async exportPrivateKey(privateKey: CryptoKey): Promise<string> {
    const exported = await window.crypto.subtle.exportKey('pkcs8', privateKey);
    return this.arrayBufferToPem(exported, 'PRIVATE KEY');
  }

  private arrayBufferToPem(buffer: ArrayBuffer, label: string): string {
    const bytes = new Uint8Array(buffer);
    let binary = '';

    for (const byte of bytes) {
      binary += String.fromCharCode(byte);
    }

    const base64 = btoa(binary);
    const formattedBase64 = base64.match(/.{1,64}/g)?.join('\n');

    return `-----BEGIN ${label}-----\n${formattedBase64}\n-----END ${label}-----`;
  }

  //browser automaticaly saves the private key in downloads
  private downloadPrivateKey(privateKeyPem: string) {
    //makes virtual file in browser memory
    const blob = new Blob([privateKeyPem], { type: 'application/x-pem-file' });
    //makes temporary url
    const url = window.URL.createObjectURL(blob);
    //creates link for download
    const a = document.createElement('a');
    a.href = url;
    a.download = 'private_key.pem';
    a.click();

    window.URL.revokeObjectURL(url);
  }
  private async importPrivateKey(privateKeyPem: string): Promise<CryptoKey> {

    const pemContents = privateKeyPem
      .replace('-----BEGIN PRIVATE KEY-----', '')
      .replace('-----END PRIVATE KEY-----', '')
      .replace(/\s/g, '');

    const binary = atob(pemContents);

    const bytes = Uint8Array.from(
      binary,
      c => c.charCodeAt(0)
    );

    return crypto.subtle.importKey(
      'pkcs8',
      bytes.buffer,
      {
        name: 'RSA-OAEP',
        hash: 'SHA-256'
      },
      false,
      ['decrypt']
    );
  }
  async decryptWithPrivateKey(privateKeyPem : string, encryptedChallenge : string ) : Promise<string>{
    const privateKey = await this.importPrivateKey(privateKeyPem);

    const encryptedBytes = Uint8Array.from(
      atob(encryptedChallenge),
      c => c.charCodeAt(0)
    );

    const decryptedBuffer = await crypto.subtle.decrypt(
      {
        name: 'RSA-OAEP'
      },
      privateKey,
      encryptedBytes
    );

    return new TextDecoder().decode(decryptedBuffer);
  }
}
