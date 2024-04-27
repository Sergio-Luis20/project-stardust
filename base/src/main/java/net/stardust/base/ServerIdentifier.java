package net.stardust.base;

import java.io.ObjectStreamException;
import java.io.Serial;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.stardust.base.model.Identifier;

@Getter
@EqualsAndHashCode
public final class ServerIdentifier implements Identifier<String> {

    public static final ServerIdentifier INSTANCE = new ServerIdentifier();

    private final String id = "[SERVER]";

    private ServerIdentifier() {}

    @Override
    public String getStringName() {
        return id;
    }

    @Override
    public Component getComponentName() {
        return Component.text(id);
    }

    @Serial
    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }

}
