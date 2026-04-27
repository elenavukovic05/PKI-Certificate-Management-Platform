package pki_project.pki.model;

import jakarta.persistence.*;
import pki_project.pki.utils.X500NameConverter;

import java.util.Date;

@Entity
@Table(name = "certificates")
public class Certificate {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "subject_id")
    @Convert(converter = X500NameConverter.class)
    private Subject subject;
    @ManyToOne
    @JoinColumn(name = "issuer_id")
    @Convert(converter = X500NameConverter.class)
    private Issuer issuer;
    private String serialNumber;
    private Date startDate;
    private Date endDate;
}
