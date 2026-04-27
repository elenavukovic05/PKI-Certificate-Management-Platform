package pki_project.pki.model;

import jakarta.persistence.*;
import org.bouncycastle.asn1.x500.X500Name;
import pki_project.pki.utils.X500NameConverter;

import java.security.PublicKey;

@Entity
@Table(name="subjects")
public class Subject {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private PublicKey publicKey;
    @Convert(converter = X500NameConverter.class)
    private X500Name x500Name;
}
