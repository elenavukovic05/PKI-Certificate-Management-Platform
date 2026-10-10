# PKI Certificate Management Platform

> A public key infrastructure platform with a certificate authority hierarchy, role-based certificate issuance, token-based authentication and envelope-encrypted private key storage.

## Overview

This project was developed as a team project (team of 3) as part of a university course.

The platform lets administrators create organizations and CA users, lets CA users issue certificates within their organization, and lets end users view and download their own certificates. The backend is built with Spring Boot and the frontend with Angular.

---

# Architecture

Certificate hierarchy:

```
        ROOT CA
     (self-signed)
           │
           ▼
   INTERMEDIATE CA
 (signed by a CA above)
           │
           ▼
   END-ENTITY certificate
 (issued to a user)
```

Key protection uses **envelope encryption** (AES-256-GCM):

```
          Base key
     (environment variable)
              │
              ▼
   Master ("rotating") key
         (database)
              │
              ▼
       Organization keys
         (database)
              │
              ▼
 Private keys of the organization's
          certificates
```

Rotating the master key only requires re-encrypting the organization keys, not every private key.

---

# Technologies

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- BouncyCastle
- Angular (TypeScript)
- BCrypt
- AES-256-GCM

---

# Project Features

## Roles

- **Admin**: creates organizations and CA users, issues any certificate, rotates the master key
- **CA user**: issues certificates within their own organization
- **User**: views and downloads their own end-entity certificates, can submit a CSR

## Certificates

- Issuing ROOT, INTERMEDIATE and END_ENTITY X.509 certificates with key usage and basic constraints
- Issuer validation: validity period, CA flag, revocation status and signature chain
- CSR upload or auto-generated key pair
- Download as PEM, DER (`.cer`) and password-protected PKCS#12 (`.p12`)
- Organization-scoped certificate views

## Authentication and Security

- BCrypt password hashing
- Stateless JWT access tokens validated by a filter on every request
- Refresh tokens stored in the database with rotation and reuse detection
- Optional two-factor authentication (TOTP)
- Role-based access control on every endpoint

---

# My Contribution

This is a fork of a team project. Below are the parts I implemented on both the backend and the Angular frontend.

- **JWT authentication and Spring Security configuration**: JWT filter, role-based access control, CORS, BCrypt password encoding
- **Refresh token rotation**: every refresh invalidates the old token and issues a new one in the same token family. Reusing an old token revokes the whole family, and logout revokes it as well
- **Master key rotation**: re-encrypts organization keys under a new master key in a single transaction, with a configurable interval, a force option for administrators and a rotation status endpoint
- **Certificate issuance for CA users**: restricted to the user's own organization, with request validation
- **PKCS#12 export**: role-based access check, private key decryption and a password-protected keystore

---

# Known Limitations

- Refresh tokens are stored unhashed in the database

---

# Learning Outcomes

This project demonstrates practical experience with:

- Public key infrastructure and X.509 certificates
- Authentication and authorization with JWT
- Refresh token rotation and token reuse detection
- Envelope encryption and key rotation
- Secure handling of secrets
- Full-stack development with Spring Boot and Angular

---
