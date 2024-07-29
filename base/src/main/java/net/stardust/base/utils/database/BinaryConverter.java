package net.stardust.base.utils.database;

import java.io.IOException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.stardust.base.utils.Serializer;
import net.stardust.base.utils.Throwables;

@Converter
public class BinaryConverter implements AttributeConverter<Object, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(Object attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return Serializer.serialize(attribute);
        } catch (IOException e) {
            Throwables.sendAndThrow(e);
            return null;
        }
    }

    @Override
    public Object convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return Serializer.deserialize(dbData);
        } catch (ClassNotFoundException | IOException e) {
            Throwables.sendAndThrow(e);
            return null;
        }
    }

}
