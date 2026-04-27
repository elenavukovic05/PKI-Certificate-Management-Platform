package pki_project.pki.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.bouncycastle.asn1.x500.X500Name;

@Converter(autoApply = false)
public class X500NameConverter implements AttributeConverter<X500Name, String> {

    @Override
    public String convertToDatabaseColumn(X500Name attribute) {
        if (attribute == null) return null;
        return attribute.toString(); // npr "CN=Test, O=Firma, C=RS"
    }

    @Override
    public X500Name convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return new X500Name(dbData);
    }
}
