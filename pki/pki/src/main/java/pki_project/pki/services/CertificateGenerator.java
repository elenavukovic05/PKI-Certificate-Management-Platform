package pki_project.pki.services;

import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.springframework.stereotype.Component;
import pki_project.pki.dto.CertificateRequestDTO;
import pki_project.pki.model.Issuer;
import pki_project.pki.model.Subject;

import java.math.BigInteger;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Component
public class CertificateGenerator {
    public CertificateGenerator() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate generateCertificate(Subject subject, Issuer issuer,
                                                      CertificateRequestDTO certificateRequest, BigInteger serialNumber) {
        try {

            JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
            builder = builder.setProvider("BC");
            ContentSigner contentSigner = builder.build(issuer.getPrivateKey());


            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(issuer.getX500Name(),
                    serialNumber,
                    certificateRequest.getStartDate(),
                    certificateRequest.getEndDate(),
                    subject.getX500Name(),
                    subject.getPublicKey());


            certGen.addExtension(
                    Extension.basicConstraints,
                    true,
                    new BasicConstraints(certificateRequest.isCA())
            );

            int keyUsage = 0;

            if (certificateRequest.isDigitalSignature()) {
                keyUsage |= KeyUsage.digitalSignature;
            }

            if (certificateRequest.isKeyEncipherment()) {
                keyUsage |= KeyUsage.keyEncipherment;
            }

            if (certificateRequest.isKeyCertSign()) {
                keyUsage |= KeyUsage.keyCertSign;
            }

            if (certificateRequest.isCrlSign()) {
                keyUsage |= KeyUsage.cRLSign;
            }

            certGen.addExtension(
                    Extension.keyUsage,
                    true,
                    new KeyUsage(keyUsage)
            );
            X509CertificateHolder certHolder = certGen.build(contentSigner);
            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
            certConverter = certConverter.setProvider("BC");
            return certConverter.getCertificate(certHolder);

        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (CertIOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
