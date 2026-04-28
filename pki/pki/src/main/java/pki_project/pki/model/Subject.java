package pki_project.pki.model;

import org.bouncycastle.asn1.x500.X500Name;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Subject {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private X500Name x500Name;

    public Subject(PublicKey publicKey, PrivateKey privateKey, X500Name x500Name) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.x500Name = x500Name;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public X500Name getX500Name() {
        return x500Name;
    }

    public void setX500Name(X500Name x500Name) {
        this.x500Name = x500Name;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}